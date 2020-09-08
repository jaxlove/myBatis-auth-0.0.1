package com.auth.entity;

import com.auth.authSql.ScopeSql;

/**
 * @author wangdejun
 * @description: 权限信息
 * @date 2020/7/27 18:29
 */
public class BaseAuthInfo {

    /**
     * 是否自动拼接权限sql
     */
    private Boolean autoAppendAuth = true;

    /**
     * 是否为权限查询（为false，则不会自动拼接权限sql，且不会设置authSql字段值）
     */
    private Boolean authQuery = true;

    private ScopeSql authScopSql;

    /**
     * 拼接好的权限 where条件
     * 全部权限为 null
     * simple格式为：(authTableAlias.authColumn in (dataScope) relationTypeEnum.operate authTableAlias.authColumn in (dataScope))
     * complex格式为：()
     */
    private String authSql;

    /**
     * 是否查询全部数据
     */
    private Boolean allDataSign = false;

    public ScopeSql getAuthScopSql() {
        return authScopSql;
    }

    public void setAuthScopSql(ScopeSql authScopSql) {
        this.authScopSql = authScopSql;
        if (authScopSql != null) {
            this.setAuthSql(authScopSql.getSql());
        } else {
            this.setAuthSql(null);
        }
    }

    public Boolean getAllDataSign() {
        return allDataSign;
    }

    public Boolean isAllDataSign() {
        return allDataSign;
    }

    public void setAllDataSign(Boolean allDataSign) {
        this.allDataSign = allDataSign;
    }

    public Boolean getAutoAppendAuth() {
        return autoAppendAuth;
    }

    public void setAutoAppendAuth(Boolean autoAppendAuth) {
        this.autoAppendAuth = autoAppendAuth;
    }

    public Boolean getAuthQuery() {
        return authQuery;
    }

    public void setAuthQuery(Boolean authQuery) {
        this.authQuery = authQuery;
    }

    public String getAuthSql() {
        return authSql;
    }

    private void setAuthSql(String authSql) {
        this.authSql = authSql;
    }
}
