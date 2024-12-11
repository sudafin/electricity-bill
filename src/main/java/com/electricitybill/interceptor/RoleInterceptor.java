package com.electricitybill.interceptor;

import cn.hutool.json.JSONUtil;
import com.electricitybill.entity.R;
import com.electricitybill.entity.dto.log.LogDTO;
import com.electricitybill.service.IEbPermissionService;
import com.electricitybill.service.IEbRoleService;
import com.electricitybill.service.IEbSystemLogService;
import com.electricitybill.utils.UserContextUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.Map;

@Slf4j
public class RoleInterceptor implements HandlerInterceptor {
    @Resource
    private  IEbSystemLogService ebSystemLogService;

    @Resource
    private IEbPermissionService ebPermissionService;
    private final LogDTO logDTO = new LogDTO();
    private Boolean isChecked =  false;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
         isChecked = ebPermissionService.roleCheck(request);
        if(isChecked) {
            String requestBody = getRequestBody(request);
            logDTO.setRequestBody(requestBody);
            return true;
        }else{
            response.setStatus(403);
            return false;
        }

    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //拿到返回值
        try {
            logDTO.setMethod(request.getMethod());
            logDTO.setPath(request.getRequestURI());
            logDTO.setRequestParamMap(UserContextUtils.getParams());
            logDTO.setUserAgent(request.getHeader("User-Agent"));
            logDTO.setIp(request.getRemoteAddr());
            Object res = UserContextUtils.getRes();
            if(res!= null){
                logDTO.setResponseBody(JSONUtil.toJsonStr(res));
                logDTO.setStatus("success");
            }else{
                if(!isChecked){
                    logDTO.setErrorMsg("权限未通过");
                }
                else logDTO.setErrorMsg(ex.getMessage());
                logDTO.setStatus("error");
            }
        }finally {
            ebSystemLogService.saveLog(logDTO);
            UserContextUtils.removeRes();
            UserContextUtils.removeParams();
        }
    }

    public String getRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;
        try {
            InputStream inputStream = request.getInputStream();
            if (inputStream != null) {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                char[] charBuffer = new char[128];
                int bytesRead = -1;
                while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                    stringBuilder.append(charBuffer, 0, bytesRead);
                }
            }
        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        }
        return stringBuilder.toString();
    }
}
