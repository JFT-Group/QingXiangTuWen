package com.qingxiang.util;

import java.util.List;

/**
 * 自动封面：标题哈希 -> 6 色之一。创建时落库，编辑标题不改色。
 */
public final class CoverColorUtil {

    private static final List<String> PALETTE = List.of(
            "#15803d", "#1d4ed8", "#6d28d9", "#b91c1c", "#0f766e", "#a16207");

    public static final String FALLBACK = PALETTE.get(0);

    private CoverColorUtil() {
    }

    /**
     * 与前端 editor.js 使用同一算法（Java String.hashCode + 取模），
     * 让未保存时的封面预览与服务端落库一致。
     */
    public static String colorFor(String title) {
        String text = title == null ? "" : title;
        return PALETTE.get(Math.floorMod(text.hashCode(), PALETTE.size()));
    }

    /** 只接受色板内的值，防止前端注入非法颜色。 */
    public static String normalize(String color) {
        if (color == null) {
            return FALLBACK;
        }
        String normalized = color.trim().toLowerCase();
        return PALETTE.contains(normalized) ? normalized : FALLBACK;
    }

    public static List<String> palette() {
        return PALETTE;
    }
}
