package com.qingxiang.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingxiang.dto.ArticleQuery;
import com.qingxiang.dto.ArticleSaveRequest;
import com.qingxiang.dto.LoginUser;
import com.qingxiang.dto.PageResult;
import com.qingxiang.entity.Article;
import com.qingxiang.enums.ArticleStatus;
import com.qingxiang.enums.MediaType;
import com.qingxiang.exception.BusinessException;
import com.qingxiang.mapper.ArticleMapper;
import com.qingxiang.state.ArticleStateMachine;
import com.qingxiang.util.CoverColorUtil;
import com.qingxiang.util.HtmlSanitizer;
import com.qingxiang.util.MediaUtil;
import com.qingxiang.util.PageUtil;
import com.qingxiang.vo.ArticleCardVO;
import com.qingxiang.vo.ArticleDetailVO;
import com.qingxiang.vo.ArticleEditVO;
import com.qingxiang.vo.MineCountVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 文章本体：创建草稿、编辑、提交/撤回、删除、信息流与详情。
 * 审核动作在 ReviewActionService；状态并发由 Mapper 的条件更新兜底。
 */
@Service
public class ArticleService {

    private final ArticleMapper articleMapper;
    private final CategoryService categoryService;
    private final OperationLogService operationLogService;

    public ArticleService(ArticleMapper articleMapper,
                          CategoryService categoryService,
                          OperationLogService operationLogService) {
        this.articleMapper = articleMapper;
        this.categoryService = categoryService;
        this.operationLogService = operationLogService;
    }

    public PageResult<ArticleCardVO> feed(ArticleQuery query) {
        prepareQuery(query);
        List<Long> categoryIds = query.getCategoryId() == null
                ? null : categoryService.descendantIds(query.getCategoryId());
        if (query.getCategoryId() != null && categoryIds.isEmpty()) {
            return PageResult.empty(query.getPage(), query.getSize());
        }
        Page<ArticleCardVO> page = new Page<>(query.getPage(), query.getSize());
        articleMapper.selectFeed(page, query, categoryIds);
        return PageResult.of(page);
    }

    public PageResult<ArticleCardVO> mine(Long authorId, ArticleQuery query) {
        prepareQuery(query);
        ArticleStatus status = ArticleStatus.parse(query.getStatus());
        if (query.getStatus() != null && !query.getStatus().isBlank() && status == null) {
            throw new BusinessException(400, "状态参数不合法");
        }
        query.setStatusCode(status == null ? null : status.code());
        query.setAuthorId(authorId);
        Page<ArticleCardVO> page = new Page<>(query.getPage(), query.getSize());
        articleMapper.selectMine(page, query);
        return PageResult.of(page);
    }

    public List<MineCountVO> mineCounts(Long authorId) {
        return articleMapper.countMine(authorId);
    }

    public Map<String, Integer> mineCountMap(Long authorId) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("ALL", 0);
        for (ArticleStatus status : ArticleStatus.values()) {
            counts.put(status.name(), 0);
        }
        int total = 0;
        for (MineCountVO row : mineCounts(authorId)) {
            counts.put(row.getStatus(), row.getCount());
            total += row.getCount();
        }
        counts.put("ALL", total);
        return counts;
    }

    public ArticleDetailVO detail(Long id, LoginUser viewer) {
        ArticleDetailVO detail = articleMapper.selectDetail(id);
        if (detail == null) {
            throw new BusinessException(404, "内容不存在或已删除");
        }
        ArticleStatus status = ArticleStatus.parse(detail.getStatus());
        boolean author = viewer != null && viewer.getId().equals(detail.getAuthorId());
        boolean reviewer = viewer != null && viewer.hasPermission("article:review");
        if (status != ArticleStatus.PUBLISHED && !author && !reviewer) {
            throw new BusinessException(404, "内容不存在或已删除");
        }
        return normalizeDetail(detail, author || reviewer);
    }

    public ArticleDetailVO publishedDetail(Long id) {
        ArticleDetailVO detail = articleMapper.selectDetail(id);
        if (detail == null || !"PUBLISHED".equals(detail.getStatus())) {
            throw new BusinessException(404, "内容不存在或已删除");
        }
        return normalizeDetail(detail, false);
    }

    public PageResult<ArticleCardVO> publishedByAuthor(Long authorId, int page, int size) {
        ArticleQuery query = new ArticleQuery();
        query.setAuthorId(authorId);
        query.setPage(PageUtil.page(page));
        query.setSize(PageUtil.size(size));
        Page<ArticleCardVO> p = new Page<>(query.getPage(), query.getSize());
        articleMapper.selectFeed(p, query, null);
        return PageResult.of(p);
    }

    public boolean isPublished(Long id) {
        if (id == null) {
            return false;
        }
        Article article = articleMapper.selectById(id);
        return article != null && !Integer.valueOf(1).equals(article.getDeleted())
                && Integer.valueOf(ArticleStatus.PUBLISHED.code()).equals(article.getStatus());
    }

    public ArticleEditVO editView(Long id, LoginUser author) {
        ArticleEditVO view = articleMapper.selectEdit(id);
        if (view == null) {
            throw new BusinessException(404, "内容不存在或已删除");
        }
        Article article = requireOwned(id, author);
        if (ArticleStatus.PENDING == ArticleStatus.of(article.getStatus())) {
            throw new BusinessException(409, "审核中的内容不能编辑，请先撤回");
        }
        view.setMediaType(view.getMediaType() == null ? "TEXT" : view.getMediaType());
        view.setMediaUrls(MediaUtil.fromJson(view.getMediaUrlsRaw()));
        return view;
    }

    @Transactional(rollbackFor = Exception.class)
    public ArticleDetailVO create(LoginUser author, ArticleSaveRequest request) {
        String title = normalizeTitle(request.getTitle());
        MediaType mediaType = MediaType.parse(request.getMediaType());
        List<String> mediaUrls = MediaUtil.validate(mediaType, request.getMediaUrls());
        String content = normalizeContent(request.getContent(), mediaType == MediaType.TEXT);
        if (request.getCategoryId() != null) {
            categoryService.requireEnabledLeaf(request.getCategoryId());
        }

        Article article = new Article();
        article.setAuthorId(author.getId());
        article.setCategoryId(request.getCategoryId());
        article.setTitle(title);
        article.setContent(content);
        article.setContentType("HTML");
        article.setMediaType(mediaType.name());
        article.setMediaUrls(MediaUtil.toJson(mediaUrls));
        article.setCoverUrl(mediaType == MediaType.IMAGE ? mediaUrls.get(0) : null);
        article.setCoverColor(CoverColorUtil.colorFor(title));
        article.setStatus(ArticleStatus.DRAFT.code());
        article.setLikeCount(0);
        article.setFavoriteCount(0);
        article.setCommentCount(0);
        article.setVersion(0);
        article.setDeleted(0);
        articleMapper.insert(article);

        return detail(article.getId(), author);
    }

    @Transactional(rollbackFor = Exception.class)
    public ArticleDetailVO update(Long id, LoginUser author, ArticleSaveRequest request) {
        Article article = requireOwned(id, author);
        ArticleStatus current = ArticleStatus.of(article.getStatus());
        if (!ArticleStateMachine.canEdit(current)) {
            throw new BusinessException(409, "审核中的内容不能编辑，请先撤回");
        }

        String title = normalizeTitle(request.getTitle());
        MediaType mediaType = MediaType.parse(request.getMediaType());
        List<String> mediaUrls = MediaUtil.validate(mediaType, request.getMediaUrls());
        String content = normalizeContent(request.getContent(), mediaType == MediaType.TEXT);
        ArticleStatus next = ArticleStateMachine.statusAfterEdit(current);
        if (next == ArticleStatus.PENDING) {
            // 已发布/已下架编辑后要重新审核，分类必须齐全
            if (request.getCategoryId() == null) {
                throw new BusinessException(400, "请选择分类后再提交审核");
            }
            categoryService.requireEnabledLeaf(request.getCategoryId());
        } else if (request.getCategoryId() != null) {
            categoryService.requireEnabledLeaf(request.getCategoryId());
        }

        int version = article.getVersion() == null ? 0 : article.getVersion();
        String coverUrl = mediaType == MediaType.IMAGE ? mediaUrls.get(0) : null;
        int updated = articleMapper.updateContent(id, author.getId(), title, content,
                request.getCategoryId(), next.code(), current.code(),
                mediaType.name(), MediaUtil.toJson(mediaUrls), coverUrl, version);
        if (updated == 0) {
            throw new BusinessException(409, "内容已被修改，请刷新后重试");
        }
        return detail(id, author);
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> submit(Long id, LoginUser author) {
        Article article = requireOwned(id, author);
        ArticleStatus current = ArticleStatus.of(article.getStatus());
        if (!ArticleStateMachine.canSubmit(current)) {
            throw new BusinessException(409, "当前状态不能提交审核");
        }
        validateReadyForReview(article);
        int updated = articleMapper.updateStatus(id, current.code(),
                ArticleStatus.PENDING.code(), null, null);
        if (updated == 0) {
            throw new BusinessException(409, "该内容已被处理，请刷新后重试");
        }
        return Map.of("id", id, "status", ArticleStatus.PENDING.name());
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> withdraw(Long id, LoginUser author) {
        Article article = requireOwned(id, author);
        ArticleStatus current = ArticleStatus.of(article.getStatus());
        if (!ArticleStateMachine.canWithdraw(current)) {
            throw new BusinessException(409, "只有待审核的内容可以撤回");
        }
        int updated = articleMapper.updateStatus(id, current.code(),
                ArticleStatus.DRAFT.code(), null, null);
        if (updated == 0) {
            throw new BusinessException(409, "该内容已被处理，请刷新后重试");
        }
        return Map.of("id", id, "status", ArticleStatus.DRAFT.name());
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id, LoginUser author) {
        Article article = requireOwned(id, author);
        ArticleStatus current = ArticleStatus.of(article.getStatus());
        if (!ArticleStateMachine.canDeleteByAuthor(current)) {
            throw new BusinessException(409, "待审核的内容请先撤回再删除");
        }
        int updated = articleMapper.softDeleteByAuthor(id, author.getId(), current.code());
        if (updated == 0) {
            throw new BusinessException(409, "该内容已被处理，请刷新后重试");
        }
        operationLogService.record(author, "ARTICLE_DELETE", "ARTICLE", id,
                "SUCCESS", "作者删除", article.getTitle());
    }

    /** 审核详情复用：只读，不重新校验公开性。 */
    private Article requireOwned(Long id, LoginUser author) {
        Article article = articleMapper.selectById(id);
        if (article == null || Integer.valueOf(1).equals(article.getDeleted())) {
            throw new BusinessException(404, "内容不存在或已删除");
        }
        if (!article.getAuthorId().equals(author.getId())) {
            throw new BusinessException(403, "只能操作自己的内容");
        }
        return article;
    }

    private void validateReadyForReview(Article article) {
        String title = article.getTitle() == null ? "" : article.getTitle().trim();
        if (title.isEmpty()) {
            throw new BusinessException(400, "请填写标题");
        }
        MediaType type = MediaType.parse(article.getMediaType());
        List<String> mediaUrls = MediaUtil.fromJson(article.getMediaUrls());
        if (type == MediaType.TEXT) {
            if (HtmlSanitizer.textLength(article.getContent()) == 0) {
                throw new BusinessException(400, "正文不能为空");
            }
        } else if (mediaUrls.isEmpty()) {
            throw new BusinessException(400, "请上传图片或视频");
        }
        MediaUtil.validate(type, mediaUrls);
        if (article.getCategoryId() == null) {
            throw new BusinessException(400, "请选择分类");
        }
        categoryService.requireEnabledLeaf(article.getCategoryId());
    }

    private String normalizeTitle(String raw) {
        String title = raw == null ? "" : raw.trim();
        if (title.isEmpty()) {
            return "未命名";
        }
        if (title.length() > 120) {
            throw new BusinessException(400, "标题最多 120 字");
        }
        return title;
    }

    private String normalizeContent(String raw, boolean required) {
        String content = HtmlSanitizer.sanitize(raw);
        if (required && HtmlSanitizer.textLength(content) == 0) {
            throw new BusinessException(400, "正文不能为空");
        }
        return content;
    }

    private ArticleDetailVO normalizeDetail(ArticleDetailVO detail, boolean privileged) {
        ArticleStatus status = ArticleStatus.parse(detail.getStatus());
        detail.setMediaType(detail.getMediaType() == null ? "TEXT" : detail.getMediaType());
        detail.setMediaUrls(MediaUtil.fromJson(detail.getMediaUrlsRaw()));
        detail.setStatusLabel(status == null ? "草稿" : status.label());
        if (detail.getCategoryId() != null) {
            detail.setCategoryName(categoryService.pathName(detail.getCategoryId()));
        } else {
            detail.setCategoryName("未分类");
        }
        if (!privileged) {
            detail.setRejectReason(null);
            detail.setReviewerId(null);
            detail.setReviewerName(null);
            detail.setReviewTime(null);
            detail.setSubmitTime(null);
        }
        return detail;
    }

    private void prepareQuery(ArticleQuery query) {
        query.setPage(PageUtil.page(query.getPage()));
        query.setSize(PageUtil.size(query.getSize()));
        if (query.getKeyword() != null) {
            String keyword = query.getKeyword().trim();
            if (keyword.length() > 100) {
                keyword = keyword.substring(0, 100);
            }
            query.setKeyword(keyword);
        }
        if (query.getSort() == null || !"oldest".equalsIgnoreCase(query.getSort())) {
            query.setSort("latest");
        } else {
            query.setSort("oldest");
        }
    }
}
