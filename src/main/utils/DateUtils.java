package main.utils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * Created by Krzysztof Baran
 * Klasa narzędzi, która odpowiedzialna jest za operacje na datach
 */
public class DateUtils {
    /**
     * Funkcja obliczająca różnicę dat w sekundach.
     * Przelicza różnicę w sekundach za pomocą klasy Duration, a następnie konwertuje na sekundy
     * @param timeStart Data początkowa
     * @param timeEnd Data końcowa
     * @return Różnica dat w sekundach
     */
    public static long calculateInSeconds(LocalDateTime timeStart, LocalDateTime timeEnd) {
        if (timeStart == null || timeEnd == null) {
            return -1;
        }
        return TimeUnit.SECONDS.convert(Duration.between(timeStart, timeEnd).toNanos(), TimeUnit.NANOSECONDS);
    }

    /**
     * Funkcja obliczająca różnicę dat w mili sekundach.
     * Przelicza różnicę w mili sekundach za pomocą klasy Duration, a następnie konwertuje na sekundy
     * @param timeStart Data początkowa
     * @param timeEnd Data końcowa
     * @return Różnica dat w sekundach
     */
    public static double calculateInMilliSeconds(LocalDateTime timeStart, LocalDateTime timeEnd) {
        if (timeStart == null || timeEnd == null) {
            return -1;
        }
        return TimeUnit.MILLISECONDS.convert(Duration.between(timeStart, timeEnd).toNanos(), TimeUnit.NANOSECONDS);
    }
}
