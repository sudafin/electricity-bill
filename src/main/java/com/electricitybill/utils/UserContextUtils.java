package com.electricitybill.utils;

public class UserContextUtils {
    private static final ThreadLocal<Long> TL = new ThreadLocal<>();
    private static final ThreadLocal<Object> res = new ThreadLocal<>();
    private static final ThreadLocal<String> params = new ThreadLocal<>();

    /**
     * 保存用户信息
     * @param userId 用户id
     */
    public static void setUser(Long userId){
        TL.set(userId);
    }

    public static void setRes(Object obj){
        res.set(obj);
    }
    public static void setParams(String param){
        params.set(param);
    }

    /**
     * 获取用户
     * @return 用户id
     */
    public static Long getUser(){
        return TL.get();
    }

    public static Object getRes() {
        return res.get();
    }

    public static String getParams() {
        return params.get();
    }

    /**
     * 移除用户信息
     */
    public static void removeUser(){
        TL.remove();
    }
    public static void removeRes(){
        res.remove();
    }
    public static void removeParams(){
        params.remove();
    }
}
