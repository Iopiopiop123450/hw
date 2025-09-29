package com.example.dungeon.model;

public class Monster extends Entity {
    private int level;

    public Monster(String name, int level, int hp, int damage) {
        super(name, hp, damage);
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
