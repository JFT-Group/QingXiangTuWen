package com.qingxiang.enums;

/**
 * 审核动作。批量接口只接受 APPROVE / REJECT / UNPUBLISH / DELETE。
 */
public enum ReviewAction {

    APPROVE("通过"),
    REJECT("驳回"),
    UNPUBLISH("下架"),
    REPUBLISH("重新上架"),
    DELETE("删除");

    private final String label;

    ReviewAction(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static ReviewAction parse(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("缺少审核动作");
        }
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("不支持的审核动作: " + value);
        }
    }

    public boolean batchAllowed() {
        return this == APPROVE || this == REJECT || this == UNPUBLISH || this == DELETE;
    }
}
