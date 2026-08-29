package com.github.onblog.monitor.common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Create by Martin 2019/5/1 0001 20:19
 */
public class DateTimeUtil {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String now() {
        return LocalDateTime.now().format(formatter);
    }

    public static String toString(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return "";
        }
        return localDateTime.format(formatter);
    }

    public static LocalDateTime parse(String substring) {
        return LocalDateTime.parse(substring, formatter);
    }
}
