package com.electricitybill.utils;

public class AdminContextUtils {
    private static final ThreadLocal<Long> TL = new ThreadLocal<>();
    private static final ThreadLocal<Object> res = new ThreadLocal<>();
    private static final ThreadLocal<String> params = new ThreadLocal<>();

    /**
     * 保存用户信息
     * @param adminId 管理员id
     */
    public static void setAdmin(Long adminId){
        TL.set(adminId);
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
    public static Long getAdminId(){
        //TODO
        return TL.get() == null ? 1L : TL.get();
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
    public static void removeAdmin(){
        TL.remove();
    }
    public static void removeRes(){
        res.remove();
    }
    public static void removeParams(){
        params.remove();
    }
}
