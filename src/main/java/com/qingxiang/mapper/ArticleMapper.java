package com.qingxiang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qingxiang.dto.ArticleQuery;
import com.qingxiang.entity.Article;
import com.qingxiang.vo.ArticleCardVO;
import com.qingxiang.vo.ArticleDetailVO;
import com.qingxiang.vo.ArticleEditVO;
import com.qingxiang.vo.MineCountVO;
import com.qingxiang.vo.ReviewItemVO;
import com.qingxiang.vo.ReviewLogVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ArticleMapper extends BaseMapper<Article> {

    IPage<ArticleCardVO> selectFeed(IPage<ArticleCardVO> page,
                                    @Param("q") ArticleQuery query,
                                    @Param("categoryIds") List<Long> categoryIds);

    IPage<ArticleCardVO> selectMine(IPage<ArticleCardVO> page, @Param("q") ArticleQuery query);

    IPage<ReviewItemVO> selectReviewQueue(IPage<ReviewItemVO> page,
                                          @Param("q") ArticleQuery query,
                                          @Param("categoryIds") List<Long> categoryIds);

    ArticleDetailVO selectDetail(@Param("id") Long id);

    ArticleEditVO selectEdit(@Param("id") Long id);

    List<ReviewLogVO> selectReviewLogs(@Param("articleId") Long articleId);

    List<MineCountVO> countMine(@Param("authorId") Long authorId);

    /** 条件更新 + 乐观锁；影响行数 0 由服务层转 409。 */
    int updateContent(@Param("id") Long id,
                      @Param("authorId") Long authorId,
                      @Param("title") String title,
                      @Param("content") String content,
                      @Param("categoryId") Long categoryId,
                      @Param("newStatus") int newStatus,
                      @Param("expectedStatus") int expectedStatus,
                      @Param("mediaType") String mediaType,
                      @Param("mediaUrls") String mediaUrls,
                      @Param("coverUrl") String coverUrl,
                      @Param("version") int version);

    /** 状态机条件更新；fromStatus 是期望旧状态。 */
    int updateStatus(@Param("id") Long id,
                     @Param("fromStatus") int fromStatus,
                     @Param("toStatus") int toStatus,
                     @Param("reason") String reason,
                     @Param("reviewerId") Long reviewerId);

    int softDeleteByAuthor(@Param("id") Long id,
                           @Param("authorId") Long authorId,
                           @Param("expectedStatus") int expectedStatus);

    int adminDelete(@Param("id") Long id, @Param("expectedStatus") int expectedStatus);
}
