package com.electricitybill.config;

import com.electricitybill.filter.CxmRequestValidFilter;
import com.electricitybill.interceptor.RoleInterceptor;
import com.electricitybill.interceptor.UserInfoInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * 配置拦截器
 * 包含用户信息拦截器和token拦截器
 */
@Configuration
@Slf4j
public class ManagerWebConfig implements WebMvcConfigurer {

    //拦截的时候过滤掉swagger相关路径和登录相关接口
    private static final String[] EXCLUDE_PATH_PATTERNS = new String[]{"/swagger-ui.html",
            "/webjars/**",
            "/swagger-resources",
            "/v2/api-docs",
            "/admin/**",
            "/doc.html",
            "/workspace/**"};
    //这里不能将UserInterceptor设为Component不然会出错,要么变为@bean,要么直接new
    @Bean
    public UserInfoInterceptor userInfoInterceptor(){
        return new UserInfoInterceptor();
    };
    @Bean
    public RoleInterceptor roleInterceptor(){
        return new RoleInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 用户信息拦截器
        registry.addInterceptor(userInfoInterceptor()).excludePathPatterns(EXCLUDE_PATH_PATTERNS).addPathPatterns("/**");
        // 角色拦截器
        registry.addInterceptor(roleInterceptor()).excludePathPatterns(EXCLUDE_PATH_PATTERNS).addPathPatterns("/**");
    }

    @Bean
    public FilterRegistrationBean<CxmRequestValidFilter> Filters() {
        FilterRegistrationBean<CxmRequestValidFilter> register = new FilterRegistrationBean<CxmRequestValidFilter>();
        register.setFilter(new CxmRequestValidFilter());
        register.addUrlPatterns("/*");
        // 初始化filter的参数
        register.addInitParameter("profile", "profile");
        register.setName("cxmRequestValidFilter");
        return register;
    }


}