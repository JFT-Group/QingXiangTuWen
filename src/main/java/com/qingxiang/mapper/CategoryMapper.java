package com.qingxiang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qingxiang.entity.Category;
import com.qingxiang.vo.CategoryTreeVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CategoryMapper extends BaseMapper<Category> {

    List<CategoryTreeVO> selectAdminRows();

    int countChildren(@Param("parentId") Long parentId);

    int countArticles(@Param("categoryId") Long categoryId);

    int updateSort(@Param("id") Long id, @Param("sort") int sort);
}
