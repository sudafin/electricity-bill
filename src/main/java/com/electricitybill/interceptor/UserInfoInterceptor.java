package com.electricitybill.interceptor;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.electricitybill.entity.dto.admin.AdminDTO;
import com.electricitybill.utils.BeanUtils;
import com.electricitybill.utils.ObjectUtils;
import com.electricitybill.utils.UserContextUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserInfoInterceptor implements HandlerInterceptor {
    //拦截器
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Object userinfo = request.getAttribute("userInfo");
        AdminDTO adminDTO = JSONUtil.parseObj(userinfo).toBean(AdminDTO.class);
        //转为Object对象转为adminDTO
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
