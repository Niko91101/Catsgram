package ru.yandex.practicum.catsgram.service;

public enum SortOrder {
    ASCENDING,
    DESCENDING;

    public static SortOrder from(String order) {
        return switch (order.toLowerCase()) {
            case "ascending", "asc" -> ASCENDING;
            case "descending", "desc" -> DESCENDING;
            default -> null;
        };
    }
}
