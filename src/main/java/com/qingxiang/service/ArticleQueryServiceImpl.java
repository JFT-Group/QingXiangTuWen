package com.qingxiang.service;

import com.qingxiang.dto.ArticleQuery;
import com.qingxiang.dto.PageResult;
import com.qingxiang.vo.ArticleCardVO;
import com.qingxiang.vo.ArticleDetailVO;
import org.springframework.stereotype.Service;

/**
 * C 模块的只读入口。单独实现接口，避免 ArticleService 被 JDK 动态代理后
 * 无法按具体类型注入（A 的 @EnableAspectJAutoProxy 默认 JDK 代理）。
 */
@Service
public class ArticleQueryServiceImpl implements ArticleQueryService {

    private final ArticleService articleService;

    public ArticleQueryServiceImpl(ArticleService articleService) {
        this.articleService = articleService;
    }

    @Override
    public PageResult<ArticleCardVO> feed(ArticleQuery query) {
        return articleService.feed(query);
    }

    @Override
    public ArticleDetailVO publishedDetail(Long id) {
        return articleService.publishedDetail(id);
    }

    @Override
    public PageResult<ArticleCardVO> publishedByAuthor(Long authorId, int page, int size) {
        return articleService.publishedByAuthor(authorId, page, size);
    }

    @Override
    public boolean isPublished(Long id) {
        return articleService.isPublished(id);
    }
}
