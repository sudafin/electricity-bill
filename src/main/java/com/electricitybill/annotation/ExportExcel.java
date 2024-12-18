package com.electricitybill.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//目标是方法
@Target(ElementType.METHOD)
//表示在运行时
@Retention(RetentionPolicy.RUNTIME)
public @interface ExportExcel {
}