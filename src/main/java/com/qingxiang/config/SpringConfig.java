package com.qingxiang.config;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.zaxxer.hikari.HikariDataSource;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * 父容器：数据源、MyBatis-Plus、Mapper 扫描、声明式事务。
 *
 * <p>只扫 service；Controller/拦截器/异常出口/权限切面属于子容器，
 * 两边不重叠，避免 Bean 注册两份、AOP 双重代理。</p>
 */
@Configuration
@PropertySource(value = "classpath:common/db.properties", encoding = "UTF-8")
@ComponentScan(basePackages = "com.qingxiang.service")
@EnableAspectJAutoProxy
@EnableTransactionManagement
@MapperScan("com.qingxiang.mapper")
public class SpringConfig {

    @Bean(destroyMethod = "close")
    public HikariDataSource dataSource(@Value("${jdbc.driver}") String driver,
                                       @Value("${jdbc.url}") String url,
                                       @Value("${jdbc.username}") String username,
                                       @Value("${jdbc.password}") String password) {
        HikariDataSource ds = new HikariDataSource();
        ds.setDriverClassName(driver);
        ds.setJdbcUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setMaximumPoolSize(10);
        ds.setMinimumIdle(2);
        ds.setConnectionTimeout(10000);
        return ds;
    }

    /** 分页 + 乐观锁；缺 jsqlparser 分页会静默失效。 */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        PaginationInnerInterceptor page = new PaginationInnerInterceptor();
        page.setMaxLimit(100L);
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(page);
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }

    /**
     * 必须用 MP 的 MybatisConfiguration：BaseMapper 语句注入只在它的
     * MapperRegistry 链路里发生，plain Configuration 会报 Invalid bound statement。
     */
    @Bean
    public MybatisSqlSessionFactoryBean sqlSessionFactory(DataSource dataSource,
                                                          MybatisPlusInterceptor mybatisPlusInterceptor) {
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);

        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setMetaObjectHandler(new MybatisMetaObjectHandler());

        MybatisSqlSessionFactoryBean factory = new MybatisSqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setConfiguration(configuration);
        factory.setGlobalConfig(globalConfig);
        factory.setTypeAliasesPackage("com.qingxiang.entity");
        factory.setPlugins(mybatisPlusInterceptor);
        return factory;
    }

    @Bean
    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
