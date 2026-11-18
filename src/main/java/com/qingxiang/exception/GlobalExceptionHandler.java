package com.qingxiang.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletResponse;

/**
 * 页面流程统一异常出口：渲染 error.jsp；堆栈只进日志。
 */
@ControllerAdvice(basePackages = "com.qingxiang.controller.page")
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public String handleBusiness(BusinessException e, HttpServletResponse response, Model model) {
        response.setStatus(e.getStatus());
        model.addAttribute("code", e.getStatus());
        model.addAttribute("message", e.getMessage());
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handleOther(Exception e, HttpServletResponse response, Model model) {
        log.error("页面未知异常", e);
        response.setStatus(500);
        model.addAttribute("code", 500);
        model.addAttribute("message", "服务器开小差了，请稍后再试");
        return "error";
    }
}
