package com.qingxiang.dto;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Getter;

import java.util.List;

/**
 * 分页返回：{@code list / total / page / size}，与接口文档一致。
 */
@Getter
public class PageResult<T> {

    private final List<T> list;
    private final long total;
    private final long page;
    private final long size;

    private PageResult(List<T> list, long total, long page, long size) {
        this.list = list;
        this.total = total;
        this.page = page;
        this.size = size;
    }

    public static <T> PageResult<T> of(IPage<T> page) {
        return new PageResult<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    public static <T> PageResult<T> of(List<T> list, long total, long page, long size) {
        return new PageResult<>(list, total, page, size);
    }

    public static <T> PageResult<T> empty(long page, long size) {
        return new PageResult<>(List.of(), 0, page, size);
    }
}
