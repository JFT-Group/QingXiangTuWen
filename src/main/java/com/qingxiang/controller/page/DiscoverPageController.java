package com.qingxiang.controller.page;

import com.qingxiang.dto.ArticleQuery;
import com.qingxiang.dto.LoginUser;
import com.qingxiang.service.ArticleService;
import com.qingxiang.service.CategoryService;
import com.qingxiang.util.CurrentUser;
import com.qingxiang.vo.ArticleDetailVO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

@Controller
public class DiscoverPageController {

    private final ArticleService articleService;
    private final CategoryService categoryService;

    public DiscoverPageController(ArticleService articleService, CategoryService categoryService) {
        this.articleService = articleService;
        this.categoryService = categoryService;
    }

    @GetMapping("/discover")
    public String discover(@RequestParam(required = false) String keyword,
                           @RequestParam(required = false) Long categoryId,
                           @RequestParam(required = false, defaultValue = "latest") String sort,
                           @RequestParam(required = false, defaultValue = "1") Integer page,
                           HttpServletRequest request,
                           Model model) {
        ArticleQuery query = new ArticleQuery();
        query.setKeyword(keyword);
        query.setCategoryId(categoryId);
        query.setSort(sort);
        query.setPage(page == null ? 1 : page);
        query.setSize(24);

        model.addAttribute("articles", articleService.feed(query));
        model.addAttribute("categories", categoryService.tree(false));
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("sort", sort);
        model.addAttribute("active", "discover");
        PageSupport.common(model, request);
        return "discover";
    }

    @GetMapping("/articles/{id}")
    public String detail(@PathVariable Long id, HttpServletRequest request, Model model) {
        LoginUser viewer = CurrentUser.get();
        ArticleDetailVO article = articleService.detail(id, viewer);
        boolean author = viewer != null && viewer.getId().equals(article.getAuthorId());
        model.addAttribute("article", article);
        model.addAttribute("author", author);
        model.addAttribute("canEdit", author && !"PENDING".equals(article.getStatus()));
        model.addAttribute("canReview", viewer != null && viewer.hasPermission("article:review"));
        model.addAttribute("active", "discover");
        PageSupport.common(model, request);
        return "article-detail";
    }
}
