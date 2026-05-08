package com.example.grade.common;

import lombok.Getter;

@Getter
public enum ErrorCode {
    SUCCESS(0, "操作成功"),
    SYSTEM_ERROR(9999, "系统内部异常"),
    PARAM_ERROR(1001, "参数错误"),
    UNAUTHORIZED(1002, "未登录或Token过期"),
    FORBIDDEN(1003, "无权限执行此操作"),
    NOT_FOUND(1004, "资源不存在"),
    USER_EXIST(2001, "用户已存在"),
    USER_NOT_EXIST(2002, "用户不存在"),
    PASSWORD_ERROR(2003, "用户名或密码错误"),
    ACCOUNT_DISABLED(2004, "该账号已被暂时禁用，请联系系统管理员");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
