package com.auth.util.pagehelper;

/**
 * @author wangdejun
 * @description: pgsql pagehelper处理类
 * @date 2020/9/4 11:12
 */
public class PostgreSQLDialect implements DialectHandler {

    @Override
    public String getNativeSelectSql(String sql) {
        return null;
    }

    @Override
    public String getNativeCountSql(String sql) {
        return null;
    }

    @Override
    public String sufHandler(String sql) {
        return null;
    }
}
