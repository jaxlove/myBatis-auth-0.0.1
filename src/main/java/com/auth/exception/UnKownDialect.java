package com.auth.exception;

/**
 * @author wangdejun
 * @description: 不支持的数据库语言
 * @date 2020/7/30 20:22
 */
public class UnKownDialect extends AuthException {

    public UnKownDialect(String message) {
        super(message);
    }
}
