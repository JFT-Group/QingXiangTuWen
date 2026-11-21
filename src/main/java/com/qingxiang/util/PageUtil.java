package com.qingxiang.util;

/**
 * 分页参数收敛：page >= 1，size 默认 10、最大 50。
 */
public final class PageUtil {

    private PageUtil() {
    }

    public static int page(Integer page) {
        return page == null || page < 1 ? 1 : page;
    }

    public static int size(Integer size) {
        if (size == null || size < 1) {
            return 10;
        }
        return Math.min(size, 50);
    }
}
