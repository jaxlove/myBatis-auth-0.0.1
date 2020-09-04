package com.auth.exception;

/**
 * @author wangdejun
 * @description: 不支持的jdbcType
 * @date 2020/7/30 20:22
 */
public class UnKownDialect extends AuthException {

    public UnKownDialect(String message) {
        super(message);
    }
}
