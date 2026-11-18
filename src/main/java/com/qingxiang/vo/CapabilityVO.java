package com.qingxiang.vo;

import lombok.Getter;

/**
 * 账号页的能力卡片：把权限码翻译成用户能看懂的一句话。
 */
@Getter
public class CapabilityVO {

    private final String title;

    private final String description;

    public CapabilityVO(String title, String description) {
        this.title = title;
        this.description = description;
    }
}
