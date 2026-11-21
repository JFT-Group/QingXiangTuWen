package com.qingxiang.controller.api;

import com.qingxiang.dto.Result;
import com.qingxiang.dto.LoginUser;
import com.qingxiang.exception.BusinessException;
import com.qingxiang.service.FileStorageService;
import com.qingxiang.util.CurrentUser;
import com.qingxiang.vo.UploadResultVO;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/files")
public class FileApiController {

    private final FileStorageService fileStorageService;

    public FileApiController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/avatar")
    public Result<UploadResultVO> avatar(@RequestParam("file") MultipartFile file) {
        LoginUser user = CurrentUser.get();
        if (user == null) {
            throw new BusinessException(401, "未登录");
        }
        return Result.ok(fileStorageService.storeAvatar(file));
    }

    @PostMapping("/media")
    public Result<UploadResultVO> media(@RequestParam("kind") String kind,
                                       @RequestParam("file") MultipartFile file) {
        LoginUser user = CurrentUser.get();
        if (user == null) {
            throw new BusinessException(401, "未登录");
        }
        return Result.ok(fileStorageService.storeMedia(file, kind));
    }

    @GetMapping("/{year}/{month}/{name:.+}")
    public ResponseEntity<Resource> file(@PathVariable String year,
                                         @PathVariable String month,
                                         @PathVariable String name) {
        Resource resource = fileStorageService.load(year, month, name);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileStorageService.contentType(name)))
                .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS).cachePublic())
                .body(resource);
    }
}
