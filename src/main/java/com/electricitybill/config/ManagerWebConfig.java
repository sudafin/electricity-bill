package com.electricitybill.config;

import com.electricitybill.filter.CxmRequestValidFilter;
import com.electricitybill.handler.GlobalExceptionHandler;
import com.electricitybill.interceptor.RoleInterceptor;
import com.electricitybill.interceptor.UserInfoInterceptor;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
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

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

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
            "/admin/captcha",
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

//全局异常处理
    @Bean
    public GlobalExceptionHandler commonExceptionAdvice(){
        return new GlobalExceptionHandler();
    }



    /**
     * Jackson-序列化与反序列的配置文件,在序列化和反序列的时候做如下调正:
     *      将 LocalDateTime 序列化为 yyyy-MM-dd HH:mm:ss 格式。
     *      将 LocalDate 序列化为 yyyy-MM-dd 格式。
     *      将 LocalTime 序列化为 HH:mm:ss 格式。
     *      将 Long 和 BigInteger 类型序列化为字符串。
     */

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> {
            // 序列化
            builder.serializerByType(Long.class, ToStringSerializer.instance);
            builder.serializerByType(BigInteger.class, ToStringSerializer.instance);
            builder.serializerByType(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT)));
            builder.serializerByType(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)));
            builder.serializerByType(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT)));

            // 反序列化
            builder.deserializerByType(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT)));
            builder.deserializerByType(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)));
            builder.deserializerByType(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT)));
        };
    }


}