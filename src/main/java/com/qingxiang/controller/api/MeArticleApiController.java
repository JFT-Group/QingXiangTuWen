package com.qingxiang.controller.api;

import com.qingxiang.dto.ArticleQuery;
import com.qingxiang.dto.LoginUser;
import com.qingxiang.dto.PageResult;
import com.qingxiang.dto.Result;
import com.qingxiang.exception.BusinessException;
import com.qingxiang.service.ArticleService;
import com.qingxiang.util.CurrentUser;
import com.qingxiang.vo.ArticleCardVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me/articles")
public class MeArticleApiController {

    private final ArticleService articleService;

    public MeArticleApiController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping
    public Result<PageResult<ArticleCardVO>> mine(
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        LoginUser user = CurrentUser.get();
        if (user == null) {
            throw new BusinessException(401, "未登录");
        }
        ArticleQuery query = new ArticleQuery();
        query.setStatus(status);
        query.setPage(page == null ? 1 : page);
        query.setSize(size == null ? 10 : size);
        return Result.ok(articleService.mine(user.getId(), query));
    }
}
