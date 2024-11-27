package com.electricitybill.handler;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.electricitybill.expcetions.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

//处理异常的全局处理器,使他们能够异常后抛Result错误给前端,如果直接返回ResponseEntity就不需要额外用Result包装处理
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    //参数校验 @Validated 会给出的异常
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Object> handle(ValidationException exception){
        List<String> errors = null;
        if (exception instanceof ConstraintViolationException) {
            ConstraintViolationException exs = (ConstraintViolationException) exception;
            Set<ConstraintViolation<?>> violations = exs.getConstraintViolations();
            errors = violations.stream()
                    .map(ConstraintViolation::getMessage).collect(Collectors.toList());
        }
        if (ObjectUtil.isNotEmpty(exception.getCause())) {
            log.error("参数校验失败异常 -> ", exception);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(MapUtil.<String, Object>builder()
                        .put("code", HttpStatus.BAD_REQUEST.value())
                        .put("msg", errors)
                        .build());
    }

    //我们自定义的异常主要是处理业务处理错误如保存失败,修改失败
    @ExceptionHandler(BizIllegalException.class)
    public ResponseEntity<Object> handle(BizIllegalException exception) {
        if (ObjectUtil.isNotEmpty(exception.getCause())) {
            log.error("自定义异常处理 -> ", exception);
        }
        return ResponseEntity.status(exception.getStatus())
                .body(MapUtil.<String, Object>builder()
                        .put("code", exception.getCode())
                        .put("msg", exception.getMessage())
                        .build());
    }
    //我们自定义的异常要主要是前端参数错误,
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Object> handle(BadRequestException exception) {
        if (ObjectUtil.isNotEmpty(exception.getCause())) {
            log.error("自定义异常处理 -> ", exception);
        }
        JSONObject jsonObject = JSONUtil.parseObj(exception);
        return ResponseEntity.ok(MapUtil.<String, Object>builder()
                .put("code", exception.getCode())
                .put("msg", exception.getMessage())
                .build());
    }
    //我们自定义的异常要主要是权限错误
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Object> handle(ForbiddenException exception) {
        if (ObjectUtil.isNotEmpty(exception.getCause())) {
            log.error("自定义异常处理 -> ", exception);
        }
        JSONObject jsonObject = JSONUtil.parseObj(exception);
        return ResponseEntity.ok(MapUtil.<String, Object>builder()
                .put("code", exception.getCode())
                .put("msg", exception.getMessage())
                .build());
    }
    //我们自定义的异常要主要是登录错误
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Object> handle(UnauthorizedException exception) {
        if (ObjectUtil.isNotEmpty(exception.getCause())) {
            log.error("自定义异常处理 -> ", exception);
        }
        JSONObject jsonObject = JSONUtil.parseObj(exception);
        return ResponseEntity.ok(MapUtil.<String, Object>builder()
                .put("code", exception.getCode())
                .put("msg", exception.getMessage())
                .build());
    }
    //我们自定义的异常要主要是数据库异常
    @ExceptionHandler(DbException.class)
    public ResponseEntity<Object> handle(DbException exception) {
        if (ObjectUtil.isNotEmpty(exception.getCause())) {
            log.error("自定义异常处理 -> ", exception);
        }
        JSONObject jsonObject = JSONUtil.parseObj(exception);
        return ResponseEntity.ok(MapUtil.<String, Object>builder()
                .put("code", exception.getCode())
                .put("msg", exception.getMessage())
                .build());
    }
}
