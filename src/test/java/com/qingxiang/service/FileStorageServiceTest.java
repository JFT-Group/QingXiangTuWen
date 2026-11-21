package com.qingxiang.service;

import com.qingxiang.exception.BusinessException;
import com.qingxiang.vo.UploadResultVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileStorageServiceTest {

    private static final byte[] PNG = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=");

    @TempDir
    Path temp;

    @Test
    void validPngIsStoredWithRandomName() {
        FileStorageService service = new FileStorageService(temp.toString());
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", PNG);

        UploadResultVO result = service.storeAvatar(file);

        assertTrue(result.getUrl().matches("^/api/v1/files/\\d{4}/\\d{2}/[a-f0-9-]+\\.png$"));
        assertEquals(1, result.getWidth());
        assertEquals(1, result.getHeight());
        String[] parts = result.getUrl().split("/");
        assertTrue(service.load(parts[4], parts[5], parts[6]).exists());
    }

    @Test
    void fakePngIsRejectedByMagicBytes() {
        FileStorageService service = new FileStorageService(temp.toString());
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png",
                "not an image".getBytes());

        BusinessException e = assertThrows(BusinessException.class, () -> service.storeAvatar(file));
        assertEquals(400, e.getStatus());
    }

    @Test
    void extensionMustMatchMagicBytes() {
        FileStorageService service = new FileStorageService(temp.toString());
        MockMultipartFile file = new MockMultipartFile("file", "avatar.jpg", "image/jpeg", PNG);

        BusinessException e = assertThrows(BusinessException.class, () -> service.storeAvatar(file));
        assertEquals(400, e.getStatus());
    }

    @Test
    void oversizeFileGets413() {
        FileStorageService service = new FileStorageService(temp.toString());
        byte[] big = new byte[2 * 1024 * 1024 + 1];
        MockMultipartFile file = new MockMultipartFile("file", "big.png", "image/png", big);

        BusinessException e = assertThrows(BusinessException.class, () -> service.storeAvatar(file));
        assertEquals(413, e.getStatus());
    }

    @Test
    void traversalNameIsRejected() {
        FileStorageService service = new FileStorageService(temp.toString());

        BusinessException e = assertThrows(BusinessException.class,
                () -> service.load("2026", "11", "../avatar.png"));
        assertEquals(404, e.getStatus());
    }

    @Test
    void emptyFileGets400() {
        FileStorageService service = new FileStorageService(temp.toString());
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", new byte[0]);

        BusinessException e = assertThrows(BusinessException.class, () -> service.storeAvatar(file));
        assertEquals(400, e.getStatus());
    }
}
