package com.qingxiang.controller.page;

import com.qingxiang.aspect.permission.RequirePermission;
import com.qingxiang.dto.ArticleQuery;
import com.qingxiang.dto.LoginUser;
import com.qingxiang.service.ArticleService;
import com.qingxiang.service.CategoryService;
import com.qingxiang.util.CurrentUser;
import com.qingxiang.vo.ArticleEditVO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

@Controller
public class CreatorPageController {

    private final ArticleService articleService;
    private final CategoryService categoryService;

    public CreatorPageController(ArticleService articleService, CategoryService categoryService) {
        this.articleService = articleService;
        this.categoryService = categoryService;
    }

    @GetMapping("/creator")
    public String index() {
        return "redirect:/creator/mine";
    }

    @GetMapping("/creator/articles/new")
    @RequirePermission("article:create")
    public String createPage(HttpServletRequest request, Model model) {
        model.addAttribute("mode", "new");
        model.addAttribute("article", null);
        model.addAttribute("categories", categoryService.leafOptions());
        model.addAttribute("active", "create");
        PageSupport.common(model, request);
        return "creator-editor";
    }

    @GetMapping("/creator/articles/{id}/edit")
    @RequirePermission("article:update")
    public String editPage(@PathVariable Long id, HttpServletRequest request, Model model) {
        LoginUser user = CurrentUser.get();
        ArticleEditVO article = articleService.editView(id, user);
        model.addAttribute("mode", "edit");
        model.addAttribute("article", article);
        model.addAttribute("categories", categoryService.leafOptions());
        model.addAttribute("active", "create");
        PageSupport.common(model, request);
        return "creator-editor";
    }

    @GetMapping("/creator/mine")
    @RequirePermission("article:create")
    public String mine(@RequestParam(required = false) String status,
                       @RequestParam(required = false, defaultValue = "1") Integer page,
                       HttpServletRequest request,
                       Model model) {
        LoginUser user = CurrentUser.get();
        ArticleQuery query = new ArticleQuery();
        query.setStatus(status);
        query.setPage(page == null ? 1 : page);
        query.setSize(50);
        model.addAttribute("articles", articleService.mine(user.getId(), query));
        model.addAttribute("counts", articleService.mineCountMap(user.getId()));
        model.addAttribute("status", status == null ? "" : status.toUpperCase());
        model.addAttribute("active", "mine");
        PageSupport.common(model, request);
        return "creator-mine";
    }
}
