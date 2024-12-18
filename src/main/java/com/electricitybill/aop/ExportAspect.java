package com.electricitybill.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

@Aspect
@Component
public class ExportAspect {

    //注解所在的方法上
    @Pointcut("@annotation(com.electricitybill.annotation.ExportExcel)")
    public void exportExcelPointcut() {
    }
    
    @AfterReturning(pointcut = "exportExcelPointcut()", returning = "filePath")
    public void afterExportExcel(JoinPoint joinPoint, String filePath) throws Exception {
        HttpServletResponse response = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getResponse();
        
        // 设置响应头
        Objects.requireNonNull(response).setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=report_data_with_chart.xlsx");


        // 拿到生成临时文件地址,将生成的文件写入响应流
        FileInputStream inputStream = new FileInputStream(filePath);
        OutputStream outputStream = response.getOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.flush();
        inputStream.close();
        
        // 删除临时文件
        Files.delete(Paths.get(filePath));
    }
}