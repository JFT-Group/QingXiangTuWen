package com.qingxiang.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CategoryTreeVO {

    private Long id;
    private Long parentId;
    private String name;
    /** 1..5。 */
    private Integer level;
    private Integer sort;
    /** ENABLED / DISABLED。 */
    private String status;
    private Integer articleCount = 0;
    private List<CategoryTreeVO> children = new ArrayList<>();
}
