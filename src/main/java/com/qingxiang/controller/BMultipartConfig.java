package com.qingxiang.controller;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

/**
 * B 模块自带的上传解析器。SpringMvcConfig 没有声明 multipartResolver，
 * 这里以 @Bean 方式补齐，不修改 A 的配置文件。
 */
@Configuration
public class BMultipartConfig {

    @Bean(name = "multipartResolver")
    public MultipartResolver multipartResolver() {
        StandardServletMultipartResolver resolver = new StandardServletMultipartResolver();
        resolver.setResolveLazily(true);
        return resolver;
    }
}
