package com.electricitybill.utils;

import cn.hutool.core.util.ObjectUtil;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Object操作工具
 **/
public class ObjectUtils extends ObjectUtil {

    /**
     * 为object设置默认值，对target中的基本类型进行默认值初始化,
     * 为null的对象不操作
     *
     * @param target 需要初始化的对象
     */
    public static void setDefault(Object target) {
        if (target == null) {
            return;
        }
        Class<?> clazz = target.getClass();
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            setDefault(field, target);
        }

    }

    /**
     * 给某个字段设置为默认值
     *
     * @param field
     * @param target
     */
    private static void setDefault(Field field, Object target) {
        field.setAccessible(true);
        try {
            Object value = field.get(target);
            if (value != null) {
                return;
            }
            String type = field.getGenericType().toString();
            Object defaultValue;
            switch (type) {
                case "class java.lang.String":
                case "class java.lang.Character":
                    defaultValue = "";
                    break;
                case "class java.lang.Double":
                    defaultValue = 0.0d;
                    break;
                case "class java.lang.Long":
                    defaultValue = 0L;
                    break;
                case "class java.lang.Short":
                    defaultValue = (short) 0;
                    break;
                case "class java.lang.Integer":
                    defaultValue = 0;
                    break;
                case "class java.lang.Float":
                    defaultValue = 0f;
                    break;
                case "class java.lang.Byte":
                    defaultValue = (byte) 0;
                    break;
                case "class java.math.BigDecimal":
                    defaultValue = BigDecimal.ZERO;
                    break;
                case "class java.lang.Boolean":
                    defaultValue = Boolean.FALSE;
                    break;
                default:
                    defaultValue = null;

            }
            field.set(target, defaultValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 如果值不为空，则调用setter方法赋值
     *
     * @param target  要赋值的对象
     * @param value   要赋值的值
     * @param setter  参数类型是函数方法BitConsumer 能传两个泛型对象,无返回值,分辨Consumer它只能传一个参数
     *                有返回值 的标准接口是 Function<T,R>一个参数返回一个R,Supplier<R>无参返回一个R,BiFunction<T, U, R>两个参数返回一个R
     *
     */
    public static <T, V> void assignIfNotNull(T target, V value, BiConsumer<T, V> setter) {
        if (value != null) {
            //执行传过来的方法,如传过来EbMeter::setModel这个set方法那么就会转为(EbMeter target, String value) -> target.setModel(value)
            setter.accept(target, value);
        }
    }
}
