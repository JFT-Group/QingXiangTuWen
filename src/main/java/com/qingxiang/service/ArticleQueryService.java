package com.qingxiang.service;

import com.qingxiang.dto.ArticleQuery;
import com.qingxiang.dto.PageResult;
import com.qingxiang.vo.ArticleCardVO;
import com.qingxiang.vo.ArticleDetailVO;

/**
 * B 向 C 暴露的只读查询契约：C 不直接依赖 B 的 Mapper / 实体。
 */
public interface ArticleQueryService {

    PageResult<ArticleCardVO> feed(ArticleQuery query);

    ArticleDetailVO publishedDetail(Long id);

    PageResult<ArticleCardVO> publishedByAuthor(Long authorId, int page, int size);

    boolean isPublished(Long id);
}
