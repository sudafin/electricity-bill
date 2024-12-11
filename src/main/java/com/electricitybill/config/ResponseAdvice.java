package com.electricitybill.config;

import com.electricitybill.entity.R;
import com.electricitybill.utils.UserContextUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.servlet.http.HttpServletRequest;

import java.io.IOException;

import java.util.Map;
import java.util.Objects;


//统一返回值
@ControllerAdvice
@Slf4j
//拿到返回的数据设置
public class ResponseAdvice implements ResponseBodyAdvice<Object> {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public boolean supports(MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> aClass) {
        return true;
    }

    /**
     * @Description: 该方法是拦截到返回值（即response中的数据），然后操作返回值，并返回
     *
     **/
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter methodParameter, MediaType mediaType, Class<? extends HttpMessageConverter<?>> aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        ServletServerHttpResponse responseTemp = (ServletServerHttpResponse) serverHttpResponse;
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        Map<String, String[]> parameterMap = request.getParameterMap();
        try {
            String jsonString = objectMapper.writeValueAsString(parameterMap);
              UserContextUtils.setParams(jsonString);
        } catch (IOException e) {
            // 处理 JSON 转换异常
            UserContextUtils.setParams("");
        }
        if (body instanceof R) {
            R result = (R) body;
            //拿到返回值好设置在ThreadLocal中
            UserContextUtils.setRes(result);
        }else
            UserContextUtils.setRes(body);
        return body;
    }

}
