package com.parent.common.exception;

import lombok.Getter;

/**
 * ResultStatus
 *
 * @author Chensong
 * @date 2018/10/10
 */
@Getter
public enum ResultStatus {
    /**
     * 状态码
     */
    SUCCESS(0, "操作成功"),
    ERROR_ARGS(1000, "参数错误"),
    ERROR_SERVICE(999, "业务异常"),
    FAILURE(-1, "系统异常"),

    COMMON_MOBILE_EXIST(5001, "手机号码已被注册"),
    COMMON_VERIFY_CODE_NOT_EXIST(5002, "未发送验证码"),
    COMMON_VERIFY_CODE_EXPIRE(5003, "验证码过期"),
    COMMON_VERIFY_CODE_NOT_MATCH(5004, "验证码错误"),

    AUTH_MEMBER_NOT_EXIST(5901, "授权用户不存在"),
    AUTH_MEMBER_NO_MOBILE(5902, "未绑定手机号码"),
    AUTH_MEMBER_BIND_MOBILE_DUPLICATE(5903, "已绑定手机号码"),
    AUTH_MEMBER_NO_PASSWORD(5904, "未设置密码"),
    AUTH_MEMBER_SET_PASSWORD_DUPLICATE(5905, "已设置密码"),
    AUTH_MEMBER_LOGIN_TYPE_INVALID(5906, "第三方授权类型异常"),
    AUTH_MEMBER_LOGIN_NO_ID(5907, "未提供唯一标识"),
    AUTH_MEMBER_UNION_ID_RESET(5908, "已被其他用户绑定");

    private int code;
    private String message;

    ResultStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }}
