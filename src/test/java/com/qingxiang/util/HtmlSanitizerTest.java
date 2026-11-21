package com.qingxiang.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HtmlSanitizerTest {

    @Test
    void scriptAndEventHandlersAreRemoved() {
        String clean = HtmlSanitizer.sanitize(
                "<p onclick=\"x()\">hi<script>alert(1)</script></p><img src=x onerror=alert(1)>");
        assertFalse(clean.contains("script"));
        assertFalse(clean.contains("onclick"));
        assertFalse(clean.contains("<img"));
        assertTrue(clean.contains("<p>hi</p>"));
    }

    @Test
    void javascriptLinksAreRemoved() {
        String clean = HtmlSanitizer.sanitize("<a href=\"javascript:alert(1)\">x</a>");
        assertFalse(clean.contains("javascript"));
        assertTrue(clean.contains("x"));
    }

    @Test
    void httpsLinksGetSafeRel() {
        String clean = HtmlSanitizer.sanitize("<a href=\"https://example.com\">x</a>");
        assertTrue(clean.contains("href=\"https://example.com\""));
        assertTrue(clean.contains("rel=\"nofollow noopener\""));
        assertTrue(clean.contains("target=\"_blank\""));
    }

    @Test
    void structuralTagsAreKept() {
        String clean = HtmlSanitizer.sanitize("<h2>标题</h2><ul><li>a</li></ul><blockquote>b</blockquote>");
        assertTrue(clean.contains("<h2>标题</h2>"));
        assertTrue(clean.contains("<ul><li>a</li></ul>"));
        assertTrue(clean.contains("<blockquote>b</blockquote>"));
    }

    @Test
    void textLengthIgnoresEmptyTags() {
        assertEquals(0, HtmlSanitizer.textLength("<p> </p><br>"));
        assertEquals(2, HtmlSanitizer.textLength("<p>你好</p>"));
        assertEquals(0, HtmlSanitizer.textLength(null));
    }

    @Test
    void blankReturnsEmpty() {
        assertEquals("", HtmlSanitizer.sanitize("  "));
    }
}
