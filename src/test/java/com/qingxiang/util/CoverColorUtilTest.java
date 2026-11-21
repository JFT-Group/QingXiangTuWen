package com.qingxiang.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoverColorUtilTest {

    @Test
    void sameTitleAlwaysGetsSameColor() {
        String first = CoverColorUtil.colorFor("周末探店");
        String second = CoverColorUtil.colorFor("周末探店");
        assertEquals(first, second);
        assertTrue(CoverColorUtil.palette().contains(first));
    }

    @Test
    void nullTitleFallsBackToStableColor() {
        assertEquals(CoverColorUtil.colorFor(""), CoverColorUtil.colorFor(null));
    }

    @Test
    void illegalColorFallsBack() {
        assertEquals(CoverColorUtil.FALLBACK, CoverColorUtil.normalize(null));
        assertEquals(CoverColorUtil.FALLBACK, CoverColorUtil.normalize("#FF2442"));
        assertEquals(CoverColorUtil.FALLBACK, CoverColorUtil.normalize("javascript:alert(1)"));
    }

    @Test
    void legalColorIsNormalizedToLowercase() {
        assertEquals("#15803d", CoverColorUtil.normalize("#15803D"));
    }

    @Test
    void paletteHasSixColors() {
        assertEquals(6, CoverColorUtil.palette().size());
    }
}
