package com.auth.exception;

/**
 * @author wangdejun
 * @description: 未知 SqlType 异常
 * @date 2020/7/27 18:18
 */
public class UnknownSqlTypeException extends AuthException {

    public UnknownSqlTypeException(String message) {
        super(message);
    }
}
