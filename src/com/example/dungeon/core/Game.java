package com.example.dungeon.core;

import com.example.dungeon.model.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

public class Game {
    private final GameState state = new GameState();
    private final Map<String, Command> commands = new LinkedHashMap<>();

    static {
        WorldInfo.touch("Game");
    }

    public Game() {
        registerCommands();
        bootstrapWorld();
    }

    private void registerCommands() {
        commands.put("help", (ctx, a) -> System.out.println("Команды: " + String.join(", ", commands.keySet())));
        commands.put("gc-stats", (ctx, a) -> {
            Runtime rt = Runtime.getRuntime();
            long free = rt.freeMemory(), total = rt.totalMemory(), used = total - free;
            System.out.println("Память: used=" + used + " free=" + free + " total=" + total);
        });
        commands.put("look", (ctx, a) -> System.out.println(ctx.getCurrent().describe()));



        commands.put("move", (ctx, args) -> {
            if (args.isEmpty()) {
                throw new InvalidCommandException("Укажите направление (например, 'move north').");
            }
            String direction = args.getFirst().toLowerCase(Locale.ROOT);
            Room currentRoom = ctx.getCurrent();
            Room nextRoom = currentRoom.getNeighbors().get(direction);

            if (nextRoom == null) {
                throw new InvalidCommandException("Укажите направление (например, 'move north').");
            }

            ctx.setCurrent(nextRoom);
            System.out.println("Вы перешли в " + nextRoom.getName());
            System.out.println(nextRoom.describe());
        });
        commands.put("take", (ctx, args) -> {
                if (args.isEmpty()) {
                    throw new InvalidCommandException("Что вы хотите взять? (например, 'take Малое зелье').");
                }
        //        if (ctx.getCurrent().getMonster() != null) {
        //            throw new InvalidCommandException("Вы не можете брать предметы, пока в комнате монстр!");
         //       }
                String itemName = String.join(" ", args);
                Room currentRoom = ctx.getCurrent();

                Optional<Item> itemToTake = currentRoom.getItems().stream()
                        .filter(item -> item.getName().equalsIgnoreCase(itemName))
                        .findFirst();

                if (itemToTake.isPresent()) {
                    Item item = itemToTake.get();
                    currentRoom.getItems().remove(item);
                    ctx.getPlayer().getInventory().add(item);
                    System.out.println("Взято: " + item.getName());
                } else {
                    throw new InvalidCommandException("Здесь нет такого предмета: " + itemName);
                }
        });
        commands.put("inventory", (ctx, a) -> {
            List<Item> inventory = ctx.getPlayer().getInventory();
            if (inventory.isEmpty()) {
                System.out.println("Инвентарь пуст.");
                return;
            }
            // Группировка предметов по имени класса (Potion, Weapon и т.д.)
            Map<String, List<Item>> groupedItems = inventory.stream()
                    .collect(Collectors.groupingBy(item -> item.getClass().getSimpleName()));
            // Сортировка по имени класса и вывод
            groupedItems.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> {
                        String itemType = entry.getKey();
                        List<Item> items = entry.getValue();
                        // Для каждого типа предметов, собираем имена, сортируем и выводим
                        String itemNames = items.stream()
                                .map(Item::getName)
                                .sorted()
                                .collect(Collectors.joining(", "));
                        System.out.println("- " + itemType + " (" + items.size() + "): " + itemNames);
                    });
        });

        commands.put("use", (ctx, args) -> {
            if (args.isEmpty()) {
                throw new InvalidCommandException("Что вы хотите использовать?");
            }
            String itemName = String.join(" ", args);
            Player player = ctx.getPlayer();

            Optional<Item> itemToUse = player.getInventory().stream()
                    .filter(item -> item.getName().equalsIgnoreCase(itemName))
                    .findFirst();

            if (itemToUse.isPresent()) {
                Item item = itemToUse.get();
                // Полиморфизм: вызывается метод apply() конкретного класса (Potion, Weapon, etc.)
                item.apply(ctx);
                // Предполагаем, что все предметы одноразовые
                player.getInventory().remove(item);
            } else {
                throw new InvalidCommandException("У вас нет такого предмета: " + itemName);
            }
        });

        commands.put("fight", (ctx, a) -> {
            Room currentRoom = ctx.getCurrent();
            Monster monster = currentRoom.getMonster();
            Player player = ctx.getPlayer();

            if (monster == null) {
                throw new InvalidCommandException("Здесь не с кем сражаться.");
            }

            System.out.println("Начался бой с " + monster.getName() + "!");

            while (player.isAlive() && monster.isAlive()) {
                // Ход игрока
                int playerDamage = player.getDamage();
                monster.takeDamage(playerDamage);
                System.out.println("Вы бьёте " + monster.getName() + " на " + playerDamage + ". HP монстра: " + monster.getHp());

                if (!monster.isAlive()) {
                    break; // Монстр побеждён
                }

                // Ход монстра
                int monsterDamage = monster.getDamage();
                player.takeDamage(monsterDamage);
                System.out.println(monster.getName() + " отвечает на " + monsterDamage + ". Ваше HP: " + player.getHp());
            }

            if (!player.isAlive()) {
                System.out.println("Вы были повержены... Игра окончена.");
                SaveLoad.writeScore(player.getName(), ctx.getScore());
                SaveLoad.printScores();
                System.exit(0);
            }

            if (!monster.isAlive()) {
                System.out.println("Вы победили монстра: " + monster.getName() + "!");
                ctx.addScore(10); // Бонусные очки за победу
                currentRoom.setMonster(null); // Монстр исчезает из комнаты (выпадение лута)
            }
        });
        commands.put("about", (ctx, a) -> {
            System.out.println("DungeonMini - текстовая RPG");
            System.out.println("Версия: 1.0"); // Example version number
        });
        commands.put("save", (ctx, a) -> SaveLoad.save(ctx));
        commands.put("load", (ctx, a) -> SaveLoad.load(ctx));
        commands.put("scores", (ctx, a) -> SaveLoad.printScores());
        commands.put("exit", (ctx, a) -> {
            System.out.println("Пока!");
            System.exit(0);
        });
    }

    private void bootstrapWorld() {
        Player hero = new Player("Герой", 20, 5,5);
        state.setPlayer(hero);

        Room square = new Room("Площадь", "Каменная площадь с фонтаном.");
        Room forest = new Room("Лес", "Шелест листвы и птичий щебет.");
        Room cave = new Room("Пещера", "Темно и сыро.");
        square.getNeighbors().put("north", forest);
        forest.getNeighbors().put("south", square);
        forest.getNeighbors().put("east", cave);
        cave.getNeighbors().put("west", forest);

        forest.getItems().add(new Potion("Малое зелье", 5));
        forest.setMonster(new Monster("Волк", 1, 8, 3));

        state.setCurrent(square);
    }

    public void run() {
        System.out.println("DungeonMini (TEMPLATE). 'help' — команды.");
        try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                System.out.print("> ");
                String line = in.readLine();
                if (line == null) break;
                line = line.trim();
                if (line.isEmpty()) continue;
                List<String> parts = Arrays.asList(line.split("\s+"));
                String cmd = parts.getFirst().toLowerCase(Locale.ROOT);
                List<String> args = parts.subList(1, parts.size());
                Command c = commands.get(cmd);
                try {
                    if (c == null) throw new InvalidCommandException("Неизвестная команда: " + cmd);
                    c.execute(state, args);
                    state.addScore(1);
                } catch (InvalidCommandException e) {
                    System.out.println("Ошибка: " + e.getMessage());
                } catch (Exception e) {
                    System.out.println("Непредвиденная ошибка: " + e.getClass().getSimpleName() + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Ошибка ввода/вывода: " + e.getMessage());
        }
    }
}
