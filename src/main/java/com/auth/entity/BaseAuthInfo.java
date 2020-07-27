package com.auth.entity;

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

    /**
     * 拼接好的权限sql
     * 全部权限为 null
     * 格式为：(authTableAlias.authColumn in (dataScope) relationTypeEnum.operate authTableAlias.authColumn in (dataScope))
     */
    private String authSql;

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

    public void setAuthSql(String authSql) {
        this.authSql = authSql;
    }
}
