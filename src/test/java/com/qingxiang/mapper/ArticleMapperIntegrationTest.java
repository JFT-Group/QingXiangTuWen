package com.qingxiang.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingxiang.config.SpringConfig;
import com.qingxiang.dto.ArticleQuery;
import com.qingxiang.vo.ArticleCardVO;
import com.qingxiang.vo.ArticleDetailVO;
import com.qingxiang.vo.ArticleEditVO;
import com.qingxiang.vo.MineCountVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringJUnitConfig(SpringConfig.class)
@Transactional
class ArticleMapperIntegrationTest {

    @Autowired
    private ArticleMapper articleMapper;

    @Test
    void feedJoinsAuthorAndCategoryInOneQuery() {
        ArticleQuery query = new ArticleQuery();
        query.setSort("latest");
        Page<ArticleCardVO> page = new Page<>(1, 5);

        articleMapper.selectFeed(page, query, null);

        assertFalse(page.getRecords().isEmpty());
        ArticleCardVO first = page.getRecords().get(0);
        assertEquals("PUBLISHED", first.getStatus());
        assertNotNull(first.getAuthorName());
        assertNotNull(first.getCoverColor());
    }

    @Test
    void detailReturnsAuthorCategoryAndReviewFields() {
        ArticleDetailVO detail = articleMapper.selectDetail(1L);

        assertNotNull(detail);
        assertEquals("周末探店：藏在巷子里的面包房", detail.getTitle());
        assertEquals("创作者小享", detail.getAuthorName());
        assertEquals("探店", detail.getCategoryName());
        assertEquals("PUBLISHED", detail.getStatus());
    }

    @Test
    void editViewReturnsDraft() {
        ArticleEditVO edit = articleMapper.selectEdit(5L);

        assertNotNull(edit);
        assertEquals("DRAFT", edit.getStatus());
        assertEquals("未命名", edit.getTitle());
    }

    @Test
    void mineCountsGroupByStatus() {
        List<MineCountVO> counts = articleMapper.countMine(2L);

        assertFalse(counts.isEmpty());
        assertFalse(counts.stream().allMatch(c -> c.getCount() == 0));
    }
}
