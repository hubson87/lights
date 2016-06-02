package main.utils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

public class DateUtils {
    public static long calculateInSeconds(LocalDateTime timeStart, LocalDateTime timeEnd) {
        if (timeStart == null || timeEnd == null) {
            return -1;
        }
        return TimeUnit.SECONDS.convert(Duration.between(timeStart, timeEnd).toNanos(), TimeUnit.NANOSECONDS);
    }

    public static double calculateInMilliSeconds(LocalDateTime timeStart, LocalDateTime timeEnd) {
        if (timeStart == null || timeEnd == null) {
            return -1;
        }
        return TimeUnit.MILLISECONDS.convert(Duration.between(timeStart, timeEnd).toNanos(), TimeUnit.NANOSECONDS);
    }
}
