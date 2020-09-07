package com.auth.authSql;

/**
 * @author wangdejun
 * @description: TODO description
 * @date 2020/9/7 11:04
 */
public class WhereSql {

    private WhereScope whereScope;
    private String sql;

    public WhereSql(WhereScope whereScope) {
        this.whereScope = whereScope;
    }

    public WhereSql(WhereScope whereScope, String sql) {
        this.whereScope = whereScope;
        this.sql = sql;
    }

    public WhereScope getWhereScope() {
        return whereScope;
    }

    public void setWhereScope(WhereScope whereScope) {
        this.whereScope = whereScope;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

}
