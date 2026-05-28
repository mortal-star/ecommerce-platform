package com.example.ecommerce.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

public final class OrderNoUtil {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private OrderNoUtil() {
    }

    public static String generateOrderNo() {
        int random = ThreadLocalRandom.current().nextInt(100000, 999999);
        return "EC" + LocalDateTime.now().format(FORMATTER) + random;
    }
}
