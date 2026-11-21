package com.qingxiang.config;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.qingxiang.aspect.permission.PermissionAspect;
import com.qingxiang.interceptor.ApiWriteAuthInterceptor;
import com.qingxiang.interceptor.CsrfInterceptor;
import com.qingxiang.interceptor.LoginInterceptor;
import com.qingxiang.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.CacheControl;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.util.List;

/**
 * 子容器：Controller / 拦截器 / 异常出口 / 校验 / 视图 / 权限切面。
 */
@Configuration
@EnableWebMvc
@EnableAspectJAutoProxy
@ComponentScan(basePackages = {"com.qingxiang.controller", "com.qingxiang.interceptor",
        "com.qingxiang.exception"})
public class SpringMvcConfig implements WebMvcConfigurer {

    private final UserService userService;

    public SpringMvcConfig(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/",
                        "/login",
                        "/register",
                        "/static/**",
                        "/error",
                        "/favicon.ico",
                        "/api/v1/auth/login",
                        "/api/v1/auth/register",
                        "/api/v1/articles/**",
                        "/api/v1/categories/**",
                        "/api/v1/files/**");

        registry.addInterceptor(apiWriteAuthInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/v1/auth/login",
                        "/api/v1/auth/register");

        registry.addInterceptor(csrfInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/login",
                        "/register",
                        "/static/**",
                        "/error",
                        "/favicon.ico",
                        "/api/v1/auth/login",
                        "/api/v1/auth/register");
    }

    /** 纯 Spring 下 @Valid 默认不生效，必须显式提供 JSR-303 校验器。 */
    @Override
    public Validator getValidator() {
        return new LocalValidatorFactoryBean();
    }

    /** 只给 JSON API 开跨域；来源显式列出，不用 * + credentials。 */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://127.0.0.1:18080", "http://localhost:18080")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Content-Type", "X-CSRF-Token")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /** 关闭 LocalDateTime 的时间戳数组输出，API 返回 ISO-8601。 */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof MappingJackson2HttpMessageConverter jackson) {
                jackson.getObjectMapper().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            }
        }
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("/static/")
                .setCacheControl(CacheControl.noCache());
    }

    @Bean
    public LoginInterceptor loginInterceptor() {
        return new LoginInterceptor(userService);
    }

    @Bean
    public CsrfInterceptor csrfInterceptor() {
        return new CsrfInterceptor();
    }

    @Bean
    public ApiWriteAuthInterceptor apiWriteAuthInterceptor() {
        return new ApiWriteAuthInterceptor();
    }

    /** 权限切面切 Controller，必须注册在子容器；放父容器会静默失效。 */
    @Bean
    public PermissionAspect permissionAspect() {
        return new PermissionAspect();
    }

    @Bean
    public ViewResolver viewResolver() {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("/WEB-INF/views/");
        resolver.setSuffix(".jsp");
        return resolver;
    }
}
