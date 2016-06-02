package main.utils;

/**
 * Created by Krzysztof Baran
 * Klasa z narzędziami potrzebnymi do obsługi operacji liczbowych
 */
public class NumberUtils {
    /**
     * Funkcja, której zadaniem jest sprawdzenie, czy podany String jest liczbą typu Integer
     * @param value Analizowany napis
     * @return true jeśli podany ciąg znaków jest liczbą typu Integer, wpp false
     */
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
