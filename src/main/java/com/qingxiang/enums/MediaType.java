package com.qingxiang.enums;

public enum MediaType {

    TEXT,
    IMAGE,
    VIDEO;

    public static MediaType parse(String value) {
        if (value == null || value.isBlank()) {
            return TEXT;
        }
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return TEXT;
        }
    }
}
