package com.qingxiang.vo;

import lombok.Data;

@Data
public class UploadResultVO {

    private String url;
    /** image / video / avatar。 */
    private String kind;

    /** webp 在 JDK 默认 ImageIO 下可能无法读尺寸，此时为 null。 */
    private Integer width;
    private Integer height;
    private long size;
}
