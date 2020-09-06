package com.auth.exception;

/**
 * @author wangdejun
 * @description: 不支持的类类型
 * @date 2020/7/30 20:22
 */
public class UnSurpportJdbcType extends AuthException {

    public UnSurpportJdbcType(String message) {
        super(message);
    }
}
