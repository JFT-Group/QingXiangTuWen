package com.qingxiang.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qingxiang.enums.MediaType;
import com.qingxiang.exception.BusinessException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 媒体 URL 的 JSON 存储与校验。只接受本站 /api/v1/files/ 生成的随机文件名。
 */
public final class MediaUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Pattern FILE_URL = Pattern.compile(
            "^/api/v1/files/\\d{4}/\\d{2}/[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}\\.(jpg|jpeg|png|webp|mp4|webm)$");

    private MediaUtil() {
    }

    public static String toJson(List<String> urls) {
        if (urls == null || urls.isEmpty()) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(urls);
        } catch (Exception e) {
            throw new BusinessException(400, "媒体地址格式错误");
        }
    }

    public static List<String> fromJson(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            List<String> urls = MAPPER.readValue(json, new TypeReference<List<String>>() {
            });
            return urls == null ? List.of() : urls;
        } catch (Exception e) {
            return List.of();
        }
    }

    public static List<String> validate(MediaType type, List<String> urls) {
        MediaType mediaType = type == null ? MediaType.TEXT : type;
        List<String> clean = new ArrayList<>();
        if (urls != null) {
            for (String url : urls) {
                if (url == null || url.isBlank()) {
                    continue;
                }
                String value = url.trim();
                if (!FILE_URL.matcher(value).matches()) {
                    throw new BusinessException(400, "媒体地址不合法");
                }
                clean.add(value);
            }
        }

        if (mediaType == MediaType.TEXT) {
            if (!clean.isEmpty()) {
                throw new BusinessException(400, "文本帖不能携带媒体");
            }
            return List.of();
        }
        if (mediaType == MediaType.IMAGE) {
            if (clean.isEmpty() || clean.size() > 9) {
                throw new BusinessException(400, "图片帖需要 1–9 张图片");
            }
            for (String url : clean) {
                if (!isImage(url)) {
                    throw new BusinessException(400, "图片帖只能包含图片文件");
                }
            }
            return List.copyOf(clean);
        }
        if (clean.size() != 1) {
            throw new BusinessException(400, "视频帖需要且只能有 1 个视频");
        }
        if (!isVideo(clean.get(0))) {
            throw new BusinessException(400, "视频帖只能包含视频文件");
        }
        return List.copyOf(clean);
    }

    public static boolean isImage(String url) {
        String lower = url == null ? "" : url.toLowerCase();
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg")
                || lower.endsWith(".png") || lower.endsWith(".webp");
    }

    public static boolean isVideo(String url) {
        String lower = url == null ? "" : url.toLowerCase();
        return lower.endsWith(".mp4") || lower.endsWith(".webm");
    }
}
