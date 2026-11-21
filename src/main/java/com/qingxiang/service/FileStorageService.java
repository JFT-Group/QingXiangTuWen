package com.qingxiang.service;

import com.qingxiang.exception.BusinessException;
import com.qingxiang.vo.UploadResultVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 头像与媒体存储：大小/扩展名/magic bytes/随机名/路径穿越校验；不存 BLOB。
 */
@Service
public class FileStorageService {

    private static final long MAX_AVATAR = 2L * 1024 * 1024;
    private static final long MAX_IMAGE = 5L * 1024 * 1024;
    private static final long MAX_VIDEO = 20L * 1024 * 1024;

    private static final Set<String> IMAGE_EXT = Set.of("jpg", "jpeg", "png", "webp");
    private static final Set<String> VIDEO_EXT = Set.of("mp4", "webm");
    private static final Set<String> MEDIA_EXT = Set.of("jpg", "jpeg", "png", "webp", "mp4", "webm");

    private static final Pattern YEAR = Pattern.compile("\\d{4}");
    private static final Pattern MONTH = Pattern.compile("0[1-9]|1[0-2]");
    private static final Pattern NAME = Pattern.compile(
            "[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}\\.(jpg|jpeg|png|webp|mp4|webm)");

    private final Path root;

    public FileStorageService(@Value("${qingxiang.upload.root:}") String configured) {
        String base = configured;
        if (base == null || base.isBlank()) {
            base = System.getProperty("qingxiang.upload.root",
                    System.getProperty("user.home") + "/qingxiang-upload");
        }
        this.root = Paths.get(base).toAbsolutePath().normalize();
    }

    public UploadResultVO storeAvatar(MultipartFile file) {
        return store(file, "avatar", MAX_AVATAR, true,
                "头像不能超过 2MB", "请选择图片", "只支持 jpg、png、webp 图片");
    }

    /**
     * kind = image：1–9 张图片，单张 ≤5MB；kind = video：1 个视频，≤20MB。
     */
    public UploadResultVO storeMedia(MultipartFile file, String kind) {
        boolean video = "video".equalsIgnoreCase(kind);
        return store(file, video ? "video" : "image",
                video ? MAX_VIDEO : MAX_IMAGE,
                !video,
                video ? "视频不能超过 20MB" : "图片不能超过 5MB",
                video ? "请选择视频" : "请选择图片",
                video ? "只支持 mp4、webm 视频" : "只支持 jpg、png、webp 图片");
    }

    public Resource load(String year, String month, String name) {
        if (year == null || month == null || name == null
                || !YEAR.matcher(year).matches()
                || !MONTH.matcher(month).matches()
                || !NAME.matcher(name).matches()) {
            throw new BusinessException(404, "文件不存在");
        }
        Path target = root.resolve(year).resolve(month).resolve(name).normalize();
        if (!target.startsWith(root) || !Files.isRegularFile(target)) {
            throw new BusinessException(404, "文件不存在");
        }
        return new FileSystemResource(target);
    }

    public String contentType(String name) {
        String lower = name == null ? "" : name.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        if (lower.endsWith(".webp")) {
            return "image/webp";
        }
        if (lower.endsWith(".mp4")) {
            return "video/mp4";
        }
        if (lower.endsWith(".webm")) {
            return "video/webm";
        }
        return "image/jpeg";
    }

    private UploadResultVO store(MultipartFile file, String kind, long maxSize, boolean image,
                                 String sizeMessage, String emptyMessage, String typeMessage) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, emptyMessage);
        }
        if (file.getSize() > maxSize) {
            throw new BusinessException(413, sizeMessage);
        }
        String ext = extension(file.getOriginalFilename());
        byte[] head = readHead(file);
        String magic = image ? detectImage(head) : detectVideo(head);
        if (magic == null) {
            throw new BusinessException(400, typeMessage);
        }
        if (ext == null) {
            ext = magic;
        }
        Set<String> allowed = image ? IMAGE_EXT : VIDEO_EXT;
        if (!allowed.contains(ext) || !compatible(ext, magic)) {
            throw new BusinessException(400, "文件格式与扩展名不匹配");
        }

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Shanghai"));
        String year = String.valueOf(today.getYear());
        String month = String.format("%02d", today.getMonthValue());
        String filename = UUID.randomUUID() + "." + ext;

        try {
            Path dir = root.resolve(year).resolve(month).normalize();
            if (!dir.startsWith(root)) {
                throw new BusinessException(400, "上传路径不合法");
            }
            Files.createDirectories(dir);
            Path target = dir.resolve(filename).normalize();
            if (!target.startsWith(root)) {
                throw new BusinessException(400, "上传路径不合法");
            }
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }

            UploadResultVO vo = new UploadResultVO();
            vo.setUrl("/api/v1/files/" + year + "/" + month + "/" + filename);
            vo.setSize(Files.size(target));
            vo.setKind(kind);
            if (image) {
                BufferedImage buffered = ImageIO.read(target.toFile());
                if (buffered != null) {
                    vo.setWidth(buffered.getWidth());
                    vo.setHeight(buffered.getHeight());
                }
            }
            return vo;
        } catch (IOException e) {
            throw new BusinessException(500, "文件保存失败，请稍后重试");
        }
    }

    private String extension(String original) {
        if (original == null) {
            return null;
        }
        int dot = original.lastIndexOf('.');
        if (dot < 0 || dot == original.length() - 1) {
            return null;
        }
        String ext = original.substring(dot + 1).toLowerCase(Locale.ROOT);
        return MEDIA_EXT.contains(ext) ? ext : null;
    }

    private byte[] readHead(MultipartFile file) {
        byte[] head = new byte[16];
        try (InputStream in = file.getInputStream()) {
            int read = in.read(head);
            if (read < 4) {
                return new byte[0];
            }
        } catch (IOException e) {
            throw new BusinessException(400, "文件读取失败");
        }
        return head;
    }

    private String detectImage(byte[] b) {
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return "jpg";
        }
        if (b.length >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G'
                && b[4] == 0x0D && b[5] == 0x0A && b[6] == 0x1A && b[7] == 0x0A) {
            return "png";
        }
        if (b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
            return "webp";
        }
        return null;
    }

    private String detectVideo(byte[] b) {
        // MP4: 4 字节 size + 'ftyp'
        if (b.length >= 12 && b[4] == 'f' && b[5] == 't' && b[6] == 'y' && b[7] == 'p') {
            return "mp4";
        }
        // WebM: EBML 1A 45 DF A3
        if (b.length >= 4 && (b[0] & 0xFF) == 0x1A && (b[1] & 0xFF) == 0x45
                && (b[2] & 0xFF) == 0xDF && (b[3] & 0xFF) == 0xA3) {
            return "webm";
        }
        return null;
    }

    private boolean compatible(String ext, String magic) {
        return "jpeg".equals(ext) ? "jpg".equals(magic) : ext.equals(magic);
    }
}
