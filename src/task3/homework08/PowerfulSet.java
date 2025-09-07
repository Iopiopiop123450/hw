package task3.homework08;

import java.util.HashSet;
import java.util.Set;

public class PowerfulSet {

    /**
     * Возвращает пересечение двух множеств.
     *
     * @param set1 Первое множество.
     * @param set2 Второе множество.
     * @param <T>  Тип элементов множеств.
     * @return Новое множество, содержащее элементы, общие для обоих входных множеств.
     */
    public <T> Set<T> intersection(Set<T> set1, Set<T> set2) {
        Set<T> result = new HashSet<>(set1); // Создаем копию set1, чтобы не изменять оригинал
        result.retainAll(set2); // Удаляем из копии все элементы, которых нет в set2
        return result;
    }

    /**
     * Возвращает объединение двух множеств.
     *
     * @param set1 Первое множество.
     * @param set2 Второе множество.
     * @param <T>  Тип элементов множеств.
     * @return Новое множество, содержащее все элементы из обоих входных множеств.
     */
    public <T> Set<T> union(Set<T> set1, Set<T> set2) {
        Set<T> result = new HashSet<>(set1); // Создаем копию set1
        result.addAll(set2); // Добавляем в копию все элементы из set2
        return result;
    }

    /**
     * Возвращает относительное дополнение множества set1 к множеству set2 (set1 \ set2).
     * Содержит элементы из set1, которых нет в set2.
     *
     * @param set1 Первое множество.
     * @param set2 Второе множество.
     * @param <T>  Тип элементов множеств.
     * @return Новое множество, содержащее элементы, которые есть в set1, но отсутствуют в set2.
     */
    public <T> Set<T> relativeComplement(Set<T> set1, Set<T> set2) {
        Set<T> result = new HashSet<>(set1); // Создаем копию set1
        result.removeAll(set2); // Удаляем из копии все элементы, которые есть в set2
        return result;
    }


    public static void main(String[] args) {
        PowerfulSet powerfulSet = new PowerfulSet();

        Set<Integer> set1 = new HashSet<>();
        set1.add(1);
        set1.add(2);
        set1.add(3);

        Set<Integer> set2 = new HashSet<>();
        set2.add(0);
        set2.add(1);
        set2.add(2);
        set2.add(4);

        System.out.println("Set 1: " + set1);
        System.out.println("Set 2: " + set2);

        System.out.println("Intersection: " + powerfulSet.intersection(set1, set2)); // Output: [1, 2]
        System.out.println("Union: " + powerfulSet.union(set1, set2)); // Output: [0, 1, 2, 3, 4]
        System.out.println("Relative Complement (set1 - set2): " + powerfulSet.relativeComplement(set1, set2)); // Output: [3]
        System.out.println("Relative Complement (set2 - set1): " + powerfulSet.relativeComplement(set2, set1)); // Output: [0, 4]
    }
}
