package com.qingxiang.controller.api;

import com.qingxiang.aspect.permission.RequirePermission;
import com.qingxiang.dto.ArticleQuery;
import com.qingxiang.dto.ArticleSaveRequest;
import com.qingxiang.dto.LoginUser;
import com.qingxiang.dto.PageResult;
import com.qingxiang.dto.Result;
import com.qingxiang.exception.BusinessException;
import com.qingxiang.service.ArticleService;
import com.qingxiang.util.CurrentUser;
import com.qingxiang.vo.ArticleCardVO;
import com.qingxiang.vo.ArticleDetailVO;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Map;

/**
 * 文章 API：信息流、详情、草稿、编辑、提交/撤回、删除。
 * 社交子路径（like/favorite/comments）归 C，不在这里实现。
 */
@RestController
@RequestMapping("/api/v1/articles")
public class ArticleApiController {

    private final ArticleService articleService;

    public ArticleApiController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping
    public Result<PageResult<ArticleCardVO>> feed(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false, defaultValue = "latest") String sort,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        ArticleQuery query = new ArticleQuery();
        query.setKeyword(keyword);
        query.setCategoryId(categoryId);
        query.setAuthorId(authorId);
        query.setSort(sort);
        query.setPage(page == null ? 1 : page);
        query.setSize(size == null ? 10 : size);
        return Result.ok(articleService.feed(query));
    }

    @GetMapping("/{id}")
    public Result<ArticleDetailVO> detail(@PathVariable Long id) {
        return Result.ok(articleService.detail(id, CurrentUser.get()));
    }

    @PostMapping
    @RequirePermission("article:create")
    public Result<ArticleDetailVO> create(@Valid @RequestBody ArticleSaveRequest request) {
        return Result.ok(articleService.create(requireLogin(), request));
    }

    @PutMapping("/{id}")
    @RequirePermission("article:update")
    public Result<ArticleDetailVO> update(@PathVariable Long id,
                                          @Valid @RequestBody ArticleSaveRequest request) {
        return Result.ok(articleService.update(id, requireLogin(), request));
    }

    @DeleteMapping("/{id}")
    @RequirePermission("article:delete")
    public Result<Void> delete(@PathVariable Long id) {
        articleService.delete(id, requireLogin());
        return Result.ok();
    }

    @PostMapping("/{id}/submit")
    @RequirePermission("article:submit")
    public Result<Map<String, Object>> submit(@PathVariable Long id) {
        return Result.ok(articleService.submit(id, requireLogin()));
    }

    @PostMapping("/{id}/withdraw")
    @RequirePermission("article:submit")
    public Result<Map<String, Object>> withdraw(@PathVariable Long id) {
        return Result.ok(articleService.withdraw(id, requireLogin()));
    }

    private LoginUser requireLogin() {
        LoginUser user = CurrentUser.get();
        if (user == null) {
            throw new BusinessException(401, "未登录");
        }
        return user;
    }
}
