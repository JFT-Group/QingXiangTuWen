package com.qingxiang.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordUtilTest {

    @Test
    void encodeProducesVerifiableBcryptHash() {
        String hash = PasswordUtil.encode("123456");
        assertTrue(hash.startsWith("$2a$10$"));
        assertTrue(PasswordUtil.matches("123456", hash));
        assertFalse(PasswordUtil.matches("1234567", hash));
    }

    @Test
    void samePasswordGetsDifferentSalt() {
        assertNotEquals(PasswordUtil.encode("123456"), PasswordUtil.encode("123456"));
    }

    @Test
    void blankInputsNeverMatch() {
        assertFalse(PasswordUtil.matches(null, "$2a$10$abcdefghijklmnopqrstuv"));
        assertFalse(PasswordUtil.matches("123456", null));
        assertFalse(PasswordUtil.matches("123456", ""));
    }
}
