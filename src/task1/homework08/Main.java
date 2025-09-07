package task1.homework08;

import java.util.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.

public class Main {
        public static void main(String[] args) {
            ArrayList<Integer> listDuplicates = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 4, 3, 6, 7, 45, 5674, 432, 45, 45, 6, 7));
            System.out.println("Список" + listDuplicates);
            Set<Integer> uniqueNumbers = ElementsFinder.getUniqueElements(listDuplicates);
            System.out.println("Список уникальных элементов" + uniqueNumbers);
        }
    }
