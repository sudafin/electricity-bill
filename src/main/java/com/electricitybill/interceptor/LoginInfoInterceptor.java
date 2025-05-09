package com.electricitybill.interceptor;

import cn.hutool.core.util.ObjectUtil;

import cn.hutool.json.JSONUtil;
import com.electricitybill.entity.dto.admin.LoginDTO;
import com.electricitybill.utils.AdminContextUtils;
import com.electricitybill.utils.UserContextUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginInfoInterceptor implements HandlerInterceptor {
    //拦截器
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Object userinfo = request.getAttribute("loginInfo");
        LoginDTO loginDTO = JSONUtil.parseObj(userinfo).toBean(LoginDTO.class);
        //转为Object对象转为adminDTO
        if(ObjectUtil.isEmpty(loginDTO)){
            response.setStatus(401);
            return false;
        }
        if (loginDTO.getIsUser()) {
            UserContextUtils.setUserId(loginDTO.getId());
        }else {
            AdminContextUtils.setAdmin(loginDTO.getId());
        }
        return true;
    }
}
