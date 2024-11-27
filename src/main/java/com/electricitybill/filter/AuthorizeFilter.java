package com.electricitybill.filter;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.electricitybill.config.MyConfig;
import com.electricitybill.entity.dto.AdminDTO;
import com.electricitybill.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
@WebFilter(urlPatterns = "/*")  // Filter will be applied to all incoming requests
public class AuthorizeFilter extends OncePerRequestFilter {

    @Resource
    private MyConfig myConfig;

    @Resource
    private JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        log.info("请求路径:{}", request.getRequestURI());

        // 获取请求路径
        String path = request.getRequestURI();

        // 获取配置文件中的白名单路径
        String[] noAuthPaths = myConfig.getNoAuthPaths();

        // 查看请求路径是否在这个白名单中
        if (StrUtil.startWithAny(path, noAuthPaths)) {
            filterChain.doFilter(request, response);  // 白名单路径跳过过滤
            return;
        }

        // 获取请求头中的 token
        String token = request.getHeader("Authorization");

        if (StrUtil.isEmpty(token)) {
            // 如果没有 token，返回 401 未授权
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized: No token provided");
            return;
        }

        // 校验 token
        AdminDTO adminDTO = null;
        try {
            boolean checkToken = jwtUtils.checkToken(token);
            if (!checkToken) {
                // 如果 token 校验失败，返回 401 未授权
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Unauthorized: Invalid token");
                return;
            }
            // 如果校验成功，解析 token 获取用户信息
            adminDTO = jwtUtils.parseToken(token);
        } catch (Exception e) {
            log.error("令牌校验失败，token = {}, path = {}", token, path, e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized: Token validation failed");
            return;
        }

        // 如果用户信息为空，说明 token 失效或伪造
        if (ObjectUtil.isEmpty(adminDTO)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized: Token is invalid or expired");
            return;
        }

        // 将用户信息放入请求头中，供后续处理使用
        request.setAttribute("userInfo", JSONUtil.toJsonStr(adminDTO));

        // 可以在此做进一步的鉴权操作，决定是否允许继续访问
        // TODO: 自定义鉴权操作

        filterChain.doFilter(request, response);  // 继续过滤链
    }



    @Override
    public void destroy() {
    }
}
