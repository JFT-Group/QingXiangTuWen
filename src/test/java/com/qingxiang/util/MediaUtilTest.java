package com.qingxiang.util;

import com.qingxiang.enums.MediaType;
import com.qingxiang.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MediaUtilTest {

    private static final String IMAGE = "/api/v1/files/2026/11/11111111-1111-1111-1111-111111111111.png";
    private static final String VIDEO = "/api/v1/files/2026/11/22222222-2222-2222-2222-222222222222.mp4";

    @Test
    void jsonRoundTripKeepsOrder() {
        List<String> urls = List.of(IMAGE, VIDEO);
        assertEquals(urls, MediaUtil.fromJson(MediaUtil.toJson(urls)));
    }

    @Test
    void textAndImageRulesAreEnforced() {
        assertEquals(List.of(), MediaUtil.validate(MediaType.TEXT, List.of()));
        assertEquals(List.of(IMAGE), MediaUtil.validate(MediaType.IMAGE, List.of(IMAGE)));
        assertThrows(BusinessException.class,
                () -> MediaUtil.validate(MediaType.TEXT, List.of(IMAGE)));
        assertThrows(BusinessException.class,
                () -> MediaUtil.validate(MediaType.IMAGE, List.of()));
    }

    @Test
    void imagePostAllowsAtMostNineFiles() {
        List<String> nine = IntStream.range(0, 9)
                .mapToObj(i -> "/api/v1/files/2026/11/0000000" + i
                        + "-1111-1111-1111-111111111111.png")
                .toList();
        assertEquals(9, MediaUtil.validate(MediaType.IMAGE, nine).size());

        List<String> ten = IntStream.range(0, 10)
                .mapToObj(i -> "/api/v1/files/2026/11/0000000" + i
                        + "-1111-1111-1111-111111111111.png")
                .toList();
        assertThrows(BusinessException.class,
                () -> MediaUtil.validate(MediaType.IMAGE, ten));
    }

    @Test
    void videoPostRequiresExactlyOneVideo() {
        assertEquals(List.of(VIDEO), MediaUtil.validate(MediaType.VIDEO, List.of(VIDEO)));
        assertThrows(BusinessException.class,
                () -> MediaUtil.validate(MediaType.VIDEO, List.of(IMAGE)));
        assertThrows(BusinessException.class,
                () -> MediaUtil.validate(MediaType.VIDEO, List.of(VIDEO, VIDEO)));
    }
}
