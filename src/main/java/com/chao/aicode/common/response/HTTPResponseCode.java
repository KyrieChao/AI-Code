package com.chao.aicode.common.response;

import lombok.Getter;

/**
 * 错误码
 *
 * @author Chao
 */
@Getter
public enum HTTPResponseCode {

    // ========== 成功状态 ==========
    SUCCESS(200, "Success", "操作成功"),

    // ========== 客户端错误 4xx ==========
    // 参数错误 400xx
    PARAM_ERROR(40000, "Parameter Error", "请求参数错误"),
    PARAM_MISSING(40001, "Parameter Missing", "参数缺失"),
    PARAM_INVALID(40002, "Parameter Invalid", "参数无效"),

    // 认证错误 401xx
    UNAUTHORIZED(40100, "Unauthorized", "未登录"),
    TOKEN_EXPIRED(40101, "Token Expired", "访问令牌已过期"),
    TOKEN_INVALID(40102, "Token Invalid", "访问令牌无效"),
    TOKEN_REFUSED(40103, "Token Refused", "拒绝刷新令牌"),
    TOKEN_NOT_LATEST(40104, "Token Not Latest", "访问令牌不是最新"),
    TOKEN_STILL_VALID(40105, "Token Still Valid", "访问令牌仍然有效"),
    // 权限错误 403xx
    FORBIDDEN(40300, "Forbidden", "无权限访问"),
    PERMISSION_DENIED(40301, "Permission Denied", "权限不足"),
    // 权限错误
    ROLE_ERROR(40302, "Role Error", "角色错误"),
    // 数据错误 404xx
    DATA_NOT_FOUND(40400, "Data Not Found", "请求数据为空"),
    USER_NOT_FOUND(40401, "User Not Found", "用户不存在"),
    RESOURCE_NOT_FOUND(40402, "Resource Not Found", "请求资源不存在"),
    DATA_MAX(40403, "Data Max", "数据过大"),
    DATA_ALREADY_EXISTS(40405, "Data Already Exists", "数据已存在"),
    INTERNAL_SERVER_ERROR(40404, "Internal Server Error", "会话管理"),
    // 方法错误 405xx
    METHOD_NOT_ALLOWED(40500, "Method Not Allowed", "HTTP 方法不允许"),
    // 不支持类型
    UNSUPPORTED_TYPE(40501, "Unsupported Type", "操作不支持类型"),
    UNSUPPORTED_MEDIA_TYPE(41500, "Unsupported Media Type", "操作不支持类型"),
    // 频率限制 429xx
    TOO_MANY_REQUESTS(42900, "Too Many Requests", "请求过于频繁，请稍后再试"),

    // ========== 服务端错误 5xx ==========
    // 系统错误 500xx
    SYSTEM_ERROR(50000, "Internal Server Error", "系统内部异常"),
    SERVICE_UNAVAILABLE(50001, "Service Unavailable", "服务不可用"),
    DATABASE_ERROR(50002, "Database Error", "数据库错误"),
    REMOTE_CALL_ERROR(50003, "Remote Call Error", "上游服务调用失败"),

    // 业务操作错误 501xx
    OPERATION_FAILED(50100, "Operation Failed", "操作失败"),
    CREATE_FAILED(50101, "Create Failed", "创建失败"),
    UPDATE_FAILED(50102, "Update Failed", "更新失败"),
    DELETE_FAILED(50103, "Delete Failed", "删除失败"),
    QUERY_FAILED(50104, "Query Failed", "查询失败"),

    // ========== 自定义错误 ==========
    USER_ERROR(70000, "User Operation Failed", "用户操作失败"),
    USER_PASSWORD_ERROR(70001, "User Password Error", "用户密码错误"),
    //系统繁忙，请稍后再试
    SYSTEM_BUSY(70002, "System Busy", "系统繁忙，请稍后再试");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 状态码信息
     */
    private final String message;

    /**
     * 状态码描述（详情）
     */
    private final String description;

    HTTPResponseCode(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }

}