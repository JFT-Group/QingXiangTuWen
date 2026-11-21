package com.qingxiang.enums;

/**
 * 文章状态。数据库存 TINYINT，API 返回枚举名，页面用 label。
 */
public enum ArticleStatus {

    DRAFT(0, "草稿"),
    PENDING(1, "待审核"),
    PUBLISHED(2, "已发布"),
    REJECTED(3, "已驳回"),
    UNPUBLISHED(4, "已下架");

    private final int code;
    private final String label;

    ArticleStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int code() {
        return code;
    }

    public String label() {
        return label;
    }

    public static ArticleStatus of(Integer code) {
        if (code == null) {
            return DRAFT;
        }
        for (ArticleStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知文章状态: " + code);
    }

    /** QueryString -> 枚举；非法值返回 null，由服务层忽略。 */
    public static ArticleStatus parse(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        try {
            return valueOf(name.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
