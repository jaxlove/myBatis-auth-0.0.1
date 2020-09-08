package com.auth.dialect;

import com.auth.dialect.DialectHandler;

/**
 * @author wangdejun
 * @description: pgsql pagehelper处理类
 * @date 2020/9/4 11:12
 */
public class HsqldbDialect implements DialectHandler,PageHelperHanlder {

    @Override
    public String removePagehelperSelectSql(String sql) {
        return sql;
    }

    @Override
    public String removePagehelperCountSql(String sql) {
        return sql;
    }

    @Override
    public String selectSufHandler(String sql) {
        return sql;
    }

    @Override
    public String getEmptySql() {
        return "select 0 where 1=0";
    }
}
