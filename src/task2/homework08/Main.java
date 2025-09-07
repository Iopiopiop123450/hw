package task2.homework08;

import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите первую строку");
        String s = scanner.nextLine();
        s = s.toLowerCase();

        System.out.println("Введите вторую строку");
        String t = scanner.nextLine();
        t = t.toLowerCase();
        boolean isAnagram = isAnagram(s, t);
        System.out.println(isAnagram);
    }

    public static boolean isAnagram(String s, String t) {
        if (s.length() != t.length()) {
            return false;
        }
    char[] sChars = s.toCharArray();
    char[] tChars = t.toCharArray();

    Arrays.sort(sChars);
    Arrays.sort(tChars);

    return Arrays.equals(sChars, tChars);
    }
}
