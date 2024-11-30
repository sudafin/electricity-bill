package com.electricitybill.constants;

public interface Constant {
    String REQUEST_PARAM_MISSING = "缺少必要的参数";
    String DB_SAVE_FAILURE = "数据库保存失败";
    String DB_QUERY_FAILURE = "数据库查询失败";
    String DB_UPDATE_FAILURE = "数据库更新失败";
    String DB_DELETE_FAILURE = "数据库删除失败";
    String DATA_NOT_MATCH = "数据不匹配";
    String DATA_NOT_EXIST = "数据不存在";
    String DATA_EXIST = "数据已存在";
    String DATA_SAVE_SUCCESS = "数据保存成功";
    String DATA_UPDATE_SUCCESS = "数据更新成功";
    String DATA_DELETE_SUCCESS = "数据删除成功";
    String DATA_QUERY_SUCCESS = "数据查询成功";
    String DATA_QUERY_FAILURE = "数据查询失败";
    String DATA_QUERY_EMPTY = "数据查询为空";
    /**
     * 手机正则表达式
     */
    String PHONE_PATTERN = "^1([38][0-9]|4[579]|5[0-3,5-9]|6[6]|7[0135678]|9[89])\\d{8$";
    /**
     * 邮箱正则
     */
    String EMAIL_PATTERN = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
    /**
     *密码正则,8-16位，包含数字、字母
     */
    String PASSWORD_PATTERN = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{8,16}$";

    String SERVER_INTER_ERROR = "服务器内部错误";
    String INVALID_USER_TYPE = "无效的用户类型";
    String INVALID_ROLE_TYPE = "无效的角色类型";
    String ACCOUNT_NOT_EXIST = "账号不存在";
    String ACCOUNT_DISABLED = "账号被禁用";
    String TOKEN_EXPIRED = "Token已过期";
    String INVALID_TOKEN = "无效的Token";
    String TOKEN_GENERATE_FAILED = "Token生成失败";
    String ACCOUNT_PASSWORD_ERROR = "账号或密码错误";
    String ROLE_NOT_EXIST = "角色不存在";;

    String DATA_FIELD_NAME_CREATE_TIME = "createTime";
    String REQUEST_ID_HEADER = "X-Request-ID";

    interface Code{
        int SUCCESS = 200;
        int FAIL = 500;
        int ERROR = 404;
        int PARAM_ERROR = 400;
        int NO_AUTH = 401;
        int NO_PERMISSION = 403;
        int NO_LOGIN = 405;
    }


}
