package main.utils;

public class NumberUtils {
    public static boolean isInteger(String value) {
        if (value == null) {
            return false;
        }
        try {
            new Integer(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
