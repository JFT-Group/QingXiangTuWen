package com.qingxiang.util;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsrfUtilTest {

    @Test
    void rotateCreates64HexCharsAndStoresInSession() {
        MockHttpSession session = new MockHttpSession();
        String token = CsrfUtil.rotate(session);

        assertEquals(64, token.length());
        assertTrue(token.matches("[0-9a-f]{64}"));
        assertEquals(token, session.getAttribute(CsrfUtil.SESSION_TOKEN));
    }

    @Test
    void ensureKeepsExistingToken() {
        MockHttpSession session = new MockHttpSession();
        String first = CsrfUtil.ensure(session);
        String second = CsrfUtil.ensure(session);

        assertEquals(first, second);
    }

    @Test
    void consecutiveRotationsDiffer() {
        MockHttpSession session = new MockHttpSession();
        assertNotEquals(CsrfUtil.rotate(session), CsrfUtil.rotate(session));
    }

    @Test
    void validateRejectsMissingOrWrongToken() {
        assertTrue(CsrfUtil.validate("abc", "abc"));
        assertFalse(CsrfUtil.validate("abc", "abd"));
        assertFalse(CsrfUtil.validate(null, "abc"));
        assertFalse(CsrfUtil.validate("abc", ""));
    }
}
