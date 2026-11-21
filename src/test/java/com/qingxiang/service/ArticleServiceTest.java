package com.qingxiang.service;

import com.qingxiang.dto.ArticleQuery;
import com.qingxiang.dto.ArticleSaveRequest;
import com.qingxiang.dto.LoginUser;
import com.qingxiang.entity.Article;
import com.qingxiang.entity.Category;
import com.qingxiang.enums.ArticleStatus;
import com.qingxiang.exception.BusinessException;
import com.qingxiang.mapper.ArticleMapper;
import com.qingxiang.vo.ArticleDetailVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ArticleServiceTest {

    private ArticleMapper mapper;
    private CategoryService categoryService;
    private OperationLogService logService;
    private ArticleService service;

    private final LoginUser author = new LoginUser(2L, "creator", "创作者", 2L, "CREATOR", 0,
            Set.of("article:create", "article:update", "article:delete", "article:submit"));

    @BeforeEach
    void setUp() {
        mapper = mock(ArticleMapper.class);
        categoryService = mock(CategoryService.class);
        logService = mock(OperationLogService.class);
        service = new ArticleService(mapper, categoryService, logService);
    }

    @Test
    void blankTitleFallsBackToUnnamedWhenCreatingDraft() {
        when(mapper.insert(any(Article.class))).thenAnswer(invocation -> {
            invocation.getArgument(0, Article.class).setId(9L);
            return 1;
        });
        when(mapper.selectDetail(9L)).thenReturn(detail(9L, ArticleStatus.DRAFT));

        ArticleSaveRequest request = new ArticleSaveRequest();
        request.setTitle("   ");
        request.setContent("<p>正文</p>");

        service.create(author, request);

        verify(mapper).insert(org.mockito.ArgumentMatchers.argThat(a ->
                "未命名".equals(a.getTitle())
                        && a.getStatus() == ArticleStatus.DRAFT.code()
                        && a.getCoverColor() != null
                        && a.getAuthorId().equals(2L)));
    }

    @Test
    void blankContentGets400() {
        ArticleSaveRequest request = new ArticleSaveRequest();
        request.setTitle("标题");
        request.setContent("<p>  </p>");

        BusinessException e = assertThrows(BusinessException.class,
                () -> service.create(author, request));
        assertEquals(400, e.getStatus());
    }

    @Test
    void editingPublishedGoesBackToPending() {
        Article article = article(1L, ArticleStatus.PUBLISHED, 3);
        when(mapper.selectById(1L)).thenReturn(article);
        when(categoryService.requireEnabledLeaf(5L)).thenReturn(category(5L));
        when(mapper.updateContent(eq(1L), eq(2L), anyString(), anyString(), eq(5L),
                eq(ArticleStatus.PENDING.code()), eq(ArticleStatus.PUBLISHED.code()),
                eq("TEXT"), isNull(), isNull(), eq(3))).thenReturn(1);
        when(mapper.selectDetail(1L)).thenReturn(detail(1L, ArticleStatus.PENDING));

        ArticleSaveRequest request = new ArticleSaveRequest();
        request.setTitle("新标题");
        request.setContent("<p>新正文</p>");
        request.setCategoryId(5L);

        service.update(1L, author, request);

        verify(mapper).updateContent(eq(1L), eq(2L), eq("新标题"), anyString(), eq(5L),
                eq(ArticleStatus.PENDING.code()), eq(ArticleStatus.PUBLISHED.code()),
                eq("TEXT"), isNull(), isNull(), eq(3));
    }

    @Test
    void pendingCannotBeEdited() {
        when(mapper.selectById(1L)).thenReturn(article(1L, ArticleStatus.PENDING, 1));

        ArticleSaveRequest request = new ArticleSaveRequest();
        request.setTitle("标题");
        request.setContent("<p>正文</p>");

        BusinessException e = assertThrows(BusinessException.class,
                () -> service.update(1L, author, request));
        assertEquals(409, e.getStatus());
    }

    @Test
    void submitWithoutCategoryGets400() {
        when(mapper.selectById(1L)).thenReturn(article(1L, ArticleStatus.DRAFT, 0));

        BusinessException e = assertThrows(BusinessException.class,
                () -> service.submit(1L, author));
        assertEquals(400, e.getStatus());
    }

    @Test
    void submitMovesDraftToPending() {
        Article article = article(1L, ArticleStatus.DRAFT, 0);
        article.setCategoryId(5L);
        when(mapper.selectById(1L)).thenReturn(article);
        when(categoryService.requireEnabledLeaf(5L)).thenReturn(category(5L));
        when(mapper.updateStatus(1L, ArticleStatus.DRAFT.code(), ArticleStatus.PENDING.code(),
                null, null)).thenReturn(1);

        var result = service.submit(1L, author);

        assertEquals("PENDING", result.get("status"));
    }

    @Test
    void pendingCannotBeDeleted() {
        when(mapper.selectById(1L)).thenReturn(article(1L, ArticleStatus.PENDING, 0));

        BusinessException e = assertThrows(BusinessException.class,
                () -> service.delete(1L, author));
        assertEquals(409, e.getStatus());
    }

    @Test
    void authorCanDeleteDraft() {
        when(mapper.selectById(1L)).thenReturn(article(1L, ArticleStatus.DRAFT, 0));
        when(mapper.softDeleteByAuthor(1L, 2L, ArticleStatus.DRAFT.code())).thenReturn(1);

        service.delete(1L, author);

        verify(mapper).softDeleteByAuthor(1L, 2L, ArticleStatus.DRAFT.code());
        verify(logService).record(eq(author), eq("ARTICLE_DELETE"), eq("ARTICLE"), eq(1L),
                eq("SUCCESS"), any(), any());
    }

    @Test
    void nonPublicDraftIsHiddenFromOtherUsers() {
        when(mapper.selectDetail(1L)).thenReturn(detail(1L, ArticleStatus.DRAFT));
        LoginUser reader = new LoginUser(3L, "reader", "读者", 1L, "READER", 0, Set.of());

        BusinessException e = assertThrows(BusinessException.class,
                () -> service.detail(1L, reader));
        assertEquals(404, e.getStatus());
    }

    @Test
    void authorCanSeeOwnDraft() {
        when(mapper.selectDetail(1L)).thenReturn(detail(1L, ArticleStatus.DRAFT));

        ArticleDetailVO detail = service.detail(1L, author);

        assertNotNull(detail);
        assertEquals("草稿", detail.getStatusLabel());
    }

    @Test
    void invalidMineStatusGets400() {
        ArticleQuery query = new ArticleQuery();
        query.setStatus("BOGUS");

        BusinessException e = assertThrows(BusinessException.class,
                () -> service.mine(2L, query));
        assertEquals(400, e.getStatus());
    }

    private Article article(long id, ArticleStatus status, int version) {
        Article article = new Article();
        article.setId(id);
        article.setAuthorId(2L);
        article.setTitle("标题");
        article.setContent("<p>正文</p>");
        article.setStatus(status.code());
        article.setVersion(version);
        article.setDeleted(0);
        return article;
    }

    private ArticleDetailVO detail(long id, ArticleStatus status) {
        ArticleDetailVO detail = new ArticleDetailVO();
        detail.setId(id);
        detail.setAuthorId(2L);
        detail.setAuthorName("创作者");
        detail.setTitle("标题");
        detail.setContent("<p>正文</p>");
        detail.setStatus(status.name());
        detail.setCoverColor("#15803d");
        detail.setCategoryId(null);
        detail.setLikeCount(0);
        detail.setFavoriteCount(0);
        detail.setCommentCount(0);
        return detail;
    }

    private Category category(long id) {
        Category category = new Category();
        category.setId(id);
        category.setName("分类");
        category.setStatus(1);
        category.setDeleted(0);
        return category;
    }
}
