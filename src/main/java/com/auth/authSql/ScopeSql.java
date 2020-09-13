package com.auth.authSql;

/**
 * @author wangdejun
 * @description: 带权限范围的sql
 * @date 2020/9/7 11:04
 */
public class ScopeSql {

    private Scope scope;
    private String sql;

    public ScopeSql(Scope scope) {
        this.scope = scope;
    }

    public ScopeSql(Scope scope, String sql) {
        this.scope = scope;
        this.sql = sql;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

}
