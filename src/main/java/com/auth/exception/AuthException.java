package com.auth.exception;

/**
 * @author wangdejun
 * @description: 所有权限异常父类
 * @date 2020/7/27 18:17
 */
public class AuthException extends Exception {

    public AuthException(String message) {
        super(message);
    }
}
