package com.qingxiang.service;

import com.qingxiang.dto.CategorySaveRequest;
import com.qingxiang.entity.Category;
import com.qingxiang.exception.BusinessException;
import com.qingxiang.mapper.CategoryMapper;
import com.qingxiang.vo.CategoryOptionVO;
import com.qingxiang.vo.CategoryTreeVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CategoryServiceTest {

    private CategoryMapper mapper;
    private CategoryService service;

    @BeforeEach
    void setUp() {
        mapper = mock(CategoryMapper.class);
        service = new CategoryService(mapper);
    }

    @Test
    void publicTreeHidesDisabledBranches() {
        when(mapper.selectAdminRows()).thenReturn(List.of(
                vo(1, 0, "美食", 1, 1, "ENABLED", 0),
                vo(5, 1, "探店", 2, 1, "ENABLED", 3),
                vo(6, 1, "家常菜", 2, 2, "DISABLED", 2),
                vo(2, 0, "旅行", 1, 2, "DISABLED", 0),
                vo(7, 2, "城市漫步", 2, 1, "ENABLED", 1)));

        List<CategoryTreeVO> tree = service.tree(false);

        assertEquals(1, tree.size());
        assertEquals("美食", tree.get(0).getName());
        assertEquals(1, tree.get(0).getChildren().size());
        assertEquals("探店", tree.get(0).getChildren().get(0).getName());
    }

    @Test
    void adminTreeIncludesDisabled() {
        when(mapper.selectAdminRows()).thenReturn(List.of(
                vo(1, 0, "美食", 1, 1, "ENABLED", 0),
                vo(6, 1, "家常菜", 2, 2, "DISABLED", 2),
                vo(2, 0, "旅行", 1, 2, "DISABLED", 0)));

        assertEquals(2, service.tree(true).size());
    }

    @Test
    void leafOptionsExcludeParentsAndDisabled() {
        when(mapper.selectAdminRows()).thenReturn(List.of(
                vo(1, 0, "美食", 1, 1, "ENABLED", 0),
                vo(5, 1, "探店", 2, 1, "ENABLED", 3),
                vo(6, 1, "家常菜", 2, 2, "DISABLED", 2)));

        List<CategoryOptionVO> options = service.leafOptions();

        assertEquals(1, options.size());
        assertEquals(5L, options.get(0).getId());
        assertEquals("美食 / 探店", options.get(0).getPath());
    }

    @Test
    void descendantIdsOnlyWalkEnabledNodes() {
        when(mapper.selectAdminRows()).thenReturn(List.of(
                vo(1, 0, "美食", 1, 1, "ENABLED", 0),
                vo(5, 1, "探店", 2, 1, "ENABLED", 3),
                vo(6, 1, "家常菜", 2, 2, "DISABLED", 2)));

        assertEquals(List.of(1L, 5L), service.descendantIds(1L));
        assertTrue(service.descendantIds(2L).isEmpty());
    }

    @Test
    void duplicateSiblingGets409() {
        when(mapper.selectCount(any())).thenReturn(1L);

        CategorySaveRequest request = new CategorySaveRequest();
        request.setParentId(0L);
        request.setName("美食");
        request.setSort(1);

        BusinessException e = assertThrows(BusinessException.class, () -> service.create(request));
        assertEquals(409, e.getStatus());
    }

    @Test
    void levelBeyondFiveGets400() {
        Category parent = new Category();
        parent.setId(9L);
        parent.setLevel(5);
        parent.setStatus(1);
        parent.setDeleted(0);
        when(mapper.selectById(9L)).thenReturn(parent);

        CategorySaveRequest request = new CategorySaveRequest();
        request.setParentId(9L);
        request.setName("第六层");

        BusinessException e = assertThrows(BusinessException.class, () -> service.create(request));
        assertEquals(400, e.getStatus());
    }

    @Test
    void categoryWithChildrenCannotBeDeleted() {
        when(mapper.selectById(1L)).thenReturn(category(1L));
        when(mapper.countChildren(1L)).thenReturn(1);

        BusinessException e = assertThrows(BusinessException.class, () -> service.delete(1L));
        assertEquals(409, e.getStatus());
    }

    @Test
    void categoryWithArticlesCannotBeDeleted() {
        when(mapper.selectById(1L)).thenReturn(category(1L));
        when(mapper.countChildren(1L)).thenReturn(0);
        when(mapper.countArticles(1L)).thenReturn(3);

        BusinessException e = assertThrows(BusinessException.class, () -> service.delete(1L));
        assertEquals(409, e.getStatus());
    }

    @Test
    void emptyCategoryCanBeDeleted() {
        Category category = category(1L);
        when(mapper.selectById(1L)).thenReturn(category);
        when(mapper.countChildren(1L)).thenReturn(0);
        when(mapper.countArticles(1L)).thenReturn(0);

        service.delete(1L);

        assertEquals(1, category.getDeleted());
        verify(mapper).updateById(category);
    }

    @Test
    void disablingCategoryWithChildrenGets409() {
        when(mapper.selectById(1L)).thenReturn(category(1L));
        when(mapper.countChildren(1L)).thenReturn(2);

        BusinessException e = assertThrows(BusinessException.class,
                () -> service.updateStatus(1L, "DISABLED"));
        assertEquals(409, e.getStatus());
    }

    private CategoryTreeVO vo(long id, long parent, String name, int level, int sort,
                              String status, int articleCount) {
        CategoryTreeVO vo = new CategoryTreeVO();
        vo.setId(id);
        vo.setParentId(parent);
        vo.setName(name);
        vo.setLevel(level);
        vo.setSort(sort);
        vo.setStatus(status);
        vo.setArticleCount(articleCount);
        return vo;
    }

    private Category category(long id) {
        Category category = new Category();
        category.setId(id);
        category.setParentId(0L);
        category.setName("分类");
        category.setLevel(1);
        category.setSort(0);
        category.setStatus(1);
        category.setDeleted(0);
        return category;
    }
}
