package com.qingxiang.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.WebListener;

/**
 * 给 DispatcherServlet 补 multipart-config。
 *
 * <p>A 的初始化类没有调用 registration.setMultipartConfig；
 * 这里用 Servlet 3 的 @WebListener 在容器启动后补齐，B 的文件上传才能工作。</p>
 */
@WebListener
public class MultipartConfigListener implements ServletContextListener {

    private static final Logger log = LoggerFactory.getLogger(MultipartConfigListener.class);
    private static final long MAX_FILE = 20L * 1024 * 1024;
    private static final long MAX_REQUEST = 25L * 1024 * 1024;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletRegistration registration = sce.getServletContext().getServletRegistration("dispatcher");
        if (registration instanceof ServletRegistration.Dynamic dynamic) {
            try {
                dynamic.setMultipartConfig(new MultipartConfigElement("", MAX_FILE, MAX_REQUEST, 0));
            } catch (IllegalStateException alreadyConfigured) {
                log.debug("multipart-config 已存在，跳过");
            }
        }
    }
}
