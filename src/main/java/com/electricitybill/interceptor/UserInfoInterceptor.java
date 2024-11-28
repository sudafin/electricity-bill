package com.electricitybill.interceptor;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.electricitybill.entity.dto.AdminDTO;
import com.electricitybill.utils.UserContextUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserInfoInterceptor implements HandlerInterceptor {
    //拦截器
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String userInfo = request.getHeader("userInfo");
        AdminDTO adminDTO = JSONUtil.toBean(userInfo, AdminDTO.class);
        if(ObjectUtil.isEmpty(adminDTO)){
            response.setStatus(401);
            return false;
        }
        UserContextUtils.setUser(adminDTO.getId());
        return true;
    }
    //拦截后
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContextUtils.removeUser();
    }
}
