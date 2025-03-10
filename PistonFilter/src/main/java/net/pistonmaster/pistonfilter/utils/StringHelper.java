package net.pistonmaster.pistonfilter.utils;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class StringHelper {
    public static String revertLeet(String str) {
        str = str.toLowerCase();

        str = str.replace("0", "o");
        str = str.replace("1", "i");
        str = str.replace("2", "z");
        str = str.replace("3", "e");
        str = str.replace("4", "a");
        str = str.replace("5", "s");
        str = str.replace("6", "g");
        str = str.replace("7", "t");
        str = str.replace("8", "b");
        str = str.replace("9", "g");
        str = str.replace("&", "a");
        str = str.replace("@", "a");
        str = str.replace("(", "c");
        str = str.replace("#", "h");
        str = str.replace("!", "i");
        str = str.replace("]", "i");
        str = str.replace("|", "i");
        str = str.replace("}", "i");
        str = str.replace("?", "o");
        str = str.replace("$", "s");
        str = stripAccents(str);

        return str;
    }

    public static boolean containsDigit(String s) {
        for (char c : s.toCharArray()) {
            if (Character.isDigit(c)) {
                return true;
            }
        }

        return false;
    }

    private static String stripAccents(final String input) {
        StringBuilder decomposed = new StringBuilder(Normalizer.normalize(input, Normalizer.Form.NFD));
        for (int i = 0; i < decomposed.length(); i++) {
            if (decomposed.charAt(i) == '\u0141') {
                decomposed.setCharAt(i, 'L');
            } else if (decomposed.charAt(i) == '\u0142') {
                decomposed.setCharAt(i, 'l');
            } else if (decomposed.charAt(i) == '\u00D8') {
                decomposed.setCharAt(i, 'O');
            } else if (decomposed.charAt(i) == '\u00F8') {
                decomposed.setCharAt(i, 'o');
            }
        }
        return Pattern.compile("\\p{InCombiningDiacriticalMarks}+").matcher(decomposed).replaceAll("");
    }
}
