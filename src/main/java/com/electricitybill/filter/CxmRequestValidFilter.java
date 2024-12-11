package com.electricitybill.filter;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
//处理requestBody只能被获取一次的问题 靠CxmHttpServletRequestWrapper和CachedServletInputStream然后这个过滤器和MangerWebConfig配置过滤器
public class CxmRequestValidFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        HttpServletRequest requestWrapper = new CxmHttpServletRequestWrapper((HttpServletRequest) request);
        filterChain.doFilter(requestWrapper, response);
    }
}
