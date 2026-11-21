package com.qingxiang.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.qingxiang.dto.CategorySaveRequest;
import com.qingxiang.entity.Category;
import com.qingxiang.exception.BusinessException;
import com.qingxiang.mapper.CategoryMapper;
import com.qingxiang.vo.CategoryOptionVO;
import com.qingxiang.vo.CategoryTreeVO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 多级分类：树构建、末级校验、删除/停用约束、排序。
 */
@Service
public class CategoryService {

    public static final int MAX_LEVEL = 5;

    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    public List<CategoryTreeVO> tree(boolean includeDisabled) {
        List<CategoryTreeVO> rows = categoryMapper.selectAdminRows();
        Map<Long, CategoryTreeVO> byId = new LinkedHashMap<>();
        for (CategoryTreeVO row : rows) {
            byId.put(row.getId(), row);
        }

        Set<Long> allowed = new HashSet<>();
        for (CategoryTreeVO row : rows) {
            if (includeDisabled || "ENABLED".equals(row.getStatus())) {
                if (row.getParentId() == 0 || allowed.contains(row.getParentId())) {
                    allowed.add(row.getId());
                }
            }
        }

        List<CategoryTreeVO> roots = new ArrayList<>();
        for (CategoryTreeVO row : rows) {
            if (!allowed.contains(row.getId())) {
                continue;
            }
            if (row.getParentId() == 0) {
                roots.add(row);
            } else {
                CategoryTreeVO parent = byId.get(row.getParentId());
                if (parent != null && allowed.contains(parent.getId())) {
                    parent.getChildren().add(row);
                }
            }
        }
        roots.sort((a, b) -> compareSort(a, b));
        return roots;
    }

    /** 编辑页只列启用的末级分类。 */
    public List<CategoryOptionVO> leafOptions() {
        List<CategoryTreeVO> rows = categoryMapper.selectAdminRows();
        Map<Long, CategoryTreeVO> byId = new HashMap<>();
        Set<Long> enabled = new HashSet<>();
        for (CategoryTreeVO row : rows) {
            byId.put(row.getId(), row);
            if ("ENABLED".equals(row.getStatus())) {
                enabled.add(row.getId());
            }
        }
        Set<Long> withChildren = new HashSet<>();
        for (CategoryTreeVO row : rows) {
            if ("ENABLED".equals(row.getStatus()) && row.getParentId() != 0) {
                withChildren.add(row.getParentId());
            }
        }

        List<CategoryOptionVO> options = new ArrayList<>();
        for (CategoryTreeVO row : rows) {
            if (!enabled.contains(row.getId()) || withChildren.contains(row.getId())) {
                continue;
            }
            if (!ancestorsEnabled(row, byId)) {
                continue;
            }
            CategoryOptionVO option = new CategoryOptionVO();
            option.setId(row.getId());
            option.setName(row.getName());
            option.setLevel(row.getLevel());
            option.setPath(pathName(row.getId(), byId));
            options.add(option);
        }
        options.sort((a, b) -> a.getPath().compareTo(b.getPath()));
        return options;
    }

    /** 分类及其所有启用后代 id；用于按父分类筛选信息流。 */
    public List<Long> descendantIds(Long rootId) {
        if (rootId == null) {
            return List.of();
        }
        List<CategoryTreeVO> rows = categoryMapper.selectAdminRows();
        Map<Long, List<Long>> children = new HashMap<>();
        Set<Long> enabled = new HashSet<>();
        for (CategoryTreeVO row : rows) {
            if ("ENABLED".equals(row.getStatus())) {
                enabled.add(row.getId());
                children.computeIfAbsent(row.getParentId(), k -> new ArrayList<>()).add(row.getId());
            }
        }
        if (!enabled.contains(rootId)) {
            return List.of();
        }
        List<Long> result = new ArrayList<>();
        Deque<Long> queue = new ArrayDeque<>();
        queue.add(rootId);
        while (!queue.isEmpty()) {
            Long id = queue.poll();
            result.add(id);
            for (Long child : children.getOrDefault(id, List.of())) {
                queue.add(child);
            }
        }
        return result;
    }

    public Category requireEnabledLeaf(Long id) {
        if (id == null) {
            throw new BusinessException(400, "请选择分类");
        }
        Category category = categoryMapper.selectById(id);
        if (category == null || Integer.valueOf(1).equals(category.getDeleted())) {
            throw new BusinessException(400, "分类不存在");
        }
        if (!Integer.valueOf(1).equals(category.getStatus())) {
            throw new BusinessException(400, "该分类已停用，请选择其他分类");
        }
        if (categoryMapper.countChildren(id) > 0) {
            throw new BusinessException(400, "请选择末级分类");
        }
        List<CategoryTreeVO> rows = categoryMapper.selectAdminRows();
        Map<Long, CategoryTreeVO> byId = new HashMap<>();
        for (CategoryTreeVO row : rows) {
            byId.put(row.getId(), row);
        }
        CategoryTreeVO row = byId.get(id);
        if (row == null || !ancestorsEnabled(row, byId)) {
            throw new BusinessException(400, "父分类已停用，请选择其他分类");
        }
        return category;
    }

    public String pathName(Long id) {
        if (id == null) {
            return "未分类";
        }
        List<CategoryTreeVO> rows = categoryMapper.selectAdminRows();
        Map<Long, CategoryTreeVO> byId = new HashMap<>();
        for (CategoryTreeVO row : rows) {
            byId.put(row.getId(), row);
        }
        return pathName(id, byId);
    }

    @Transactional(rollbackFor = Exception.class)
    public CategoryTreeVO create(CategorySaveRequest request) {
        String name = normalizeName(request.getName());
        long parentId = request.getParentId() == null ? 0L : request.getParentId();
        int level = 1;
        if (parentId != 0) {
            Category parent = categoryMapper.selectById(parentId);
            if (parent == null || Integer.valueOf(1).equals(parent.getDeleted())) {
                throw new BusinessException(400, "父分类不存在");
            }
            if (!Integer.valueOf(1).equals(parent.getStatus())) {
                throw new BusinessException(400, "父分类已停用");
            }
            level = parent.getLevel() + 1;
        }
        if (level > MAX_LEVEL) {
            throw new BusinessException(400, "分类最多 " + MAX_LEVEL + " 层");
        }
        if (siblingExists(parentId, name, null)) {
            throw new BusinessException(409, "同级分类名称已存在");
        }

        Category category = new Category();
        category.setParentId(parentId);
        category.setName(name);
        category.setLevel(level);
        category.setSort(request.getSort() == null ? 0 : request.getSort());
        category.setStatus(1);
        category.setDeleted(0);
        try {
            categoryMapper.insert(category);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(409, "同级分类名称已存在");
        }
        return toVO(category, 0);
    }

    @Transactional(rollbackFor = Exception.class)
    public CategoryTreeVO update(Long id, CategorySaveRequest request) {
        Category category = requireCategory(id);
        String name = normalizeName(request.getName());
        if (siblingExists(category.getParentId(), name, id)) {
            throw new BusinessException(409, "同级分类名称已存在");
        }
        category.setName(name);
        category.setSort(request.getSort() == null ? category.getSort() : request.getSort());
        try {
            categoryMapper.updateById(category);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(409, "同级分类名称已存在");
        }
        return toVO(category, categoryMapper.countArticles(id));
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, String status) {
        Category category = requireCategory(id);
        boolean enable = "ENABLED".equalsIgnoreCase(status);
        if (!enable) {
            if (categoryMapper.countChildren(id) > 0) {
                throw new BusinessException(409, "请先处理子分类，再停用该分类");
            }
            if (categoryMapper.countArticles(id) > 0) {
                throw new BusinessException(409, "该分类下还有内容，不能停用");
            }
        }
        category.setStatus(enable ? 1 : 0);
        categoryMapper.updateById(category);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Category category = requireCategory(id);
        if (categoryMapper.countChildren(id) > 0) {
            throw new BusinessException(409, "该分类下还有子分类，不能删除");
        }
        if (categoryMapper.countArticles(id) > 0) {
            throw new BusinessException(409, "该分类下还有内容，不能删除");
        }
        category.setDeleted(1);
        categoryMapper.updateById(category);
    }

    /** 同级上移/下移；返回是否真的移动。 */
    @Transactional(rollbackFor = Exception.class)
    public boolean move(Long id, String direction) {
        Category current = requireCategory(id);
        List<Category> siblings = categoryMapper.selectList(Wrappers.<Category>lambdaQuery()
                .eq(Category::getParentId, current.getParentId())
                .eq(Category::getDeleted, 0)
                .orderByAsc(Category::getSort)
                .orderByAsc(Category::getId));
        int index = -1;
        for (int i = 0; i < siblings.size(); i++) {
            if (siblings.get(i).getId().equals(id)) {
                index = i;
                break;
            }
        }
        if (index < 0) {
            return false;
        }
        int target = "down".equalsIgnoreCase(direction) ? index + 1 : index - 1;
        if (target < 0 || target >= siblings.size()) {
            return false;
        }
        Category other = siblings.get(target);
        int currentSort = current.getSort();
        int otherSort = other.getSort();
        if (currentSort == otherSort) {
            currentSort = index;
            otherSort = target;
        }
        categoryMapper.updateSort(current.getId(), otherSort);
        categoryMapper.updateSort(other.getId(), currentSort);
        return true;
    }

    private Category requireCategory(Long id) {
        if (id == null) {
            throw new BusinessException(404, "分类不存在");
        }
        Category category = categoryMapper.selectById(id);
        if (category == null || Integer.valueOf(1).equals(category.getDeleted())) {
            throw new BusinessException(404, "分类不存在");
        }
        return category;
    }

    private boolean siblingExists(long parentId, String name, Long excludeId) {
        var query = Wrappers.<Category>lambdaQuery()
                .eq(Category::getParentId, parentId)
                .eq(Category::getName, name)
                .eq(Category::getDeleted, 0);
        if (excludeId != null) {
            query.ne(Category::getId, excludeId);
        }
        Long count = categoryMapper.selectCount(query);
        return count != null && count > 0;
    }

    private String normalizeName(String raw) {
        String name = raw == null ? "" : raw.trim();
        if (name.isEmpty()) {
            throw new BusinessException(400, "请输入分类名称");
        }
        if (name.length() > 30) {
            throw new BusinessException(400, "分类名称最多 30 字");
        }
        return name;
    }

    private boolean ancestorsEnabled(CategoryTreeVO row, Map<Long, CategoryTreeVO> byId) {
        Long parentId = row.getParentId();
        while (parentId != null && parentId != 0) {
            CategoryTreeVO parent = byId.get(parentId);
            if (parent == null || !"ENABLED".equals(parent.getStatus())) {
                return false;
            }
            parentId = parent.getParentId();
        }
        return true;
    }

    private String pathName(Long id, Map<Long, CategoryTreeVO> byId) {
        List<String> names = new ArrayList<>();
        Long cursor = id;
        int guard = 0;
        while (cursor != null && cursor != 0 && guard++ < MAX_LEVEL + 1) {
            CategoryTreeVO row = byId.get(cursor);
            if (row == null) {
                break;
            }
            names.add(0, row.getName());
            cursor = row.getParentId();
        }
        return names.isEmpty() ? "未分类" : String.join(" / ", names);
    }

    private int compareSort(CategoryTreeVO a, CategoryTreeVO b) {
        int sort = Integer.compare(a.getSort() == null ? 0 : a.getSort(),
                b.getSort() == null ? 0 : b.getSort());
        return sort != 0 ? sort : Long.compare(a.getId(), b.getId());
    }

    private CategoryTreeVO toVO(Category category, int articleCount) {
        CategoryTreeVO vo = new CategoryTreeVO();
        vo.setId(category.getId());
        vo.setParentId(category.getParentId());
        vo.setName(category.getName());
        vo.setLevel(category.getLevel());
        vo.setSort(category.getSort());
        vo.setStatus(Integer.valueOf(1).equals(category.getStatus()) ? "ENABLED" : "DISABLED");
        vo.setArticleCount(articleCount);
        return vo;
    }
}
