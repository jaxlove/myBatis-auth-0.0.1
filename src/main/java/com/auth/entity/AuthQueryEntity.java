package com.auth.entity;


import java.io.Serializable;
import java.util.List;
import java.util.Properties;

/**
 * @author wangdejun
 * @description: 权限查询实体类
 * @date 2020/2/22 12:04
 */
public class AuthQueryEntity implements Serializable {


    /**
     * 是否自动拼接权限sql
     */
    private Boolean autoAppendAuth = true;

    /**
     * 是否为权限查询（为false，则不会自动拼接权限sql，且不会设置authSql字段值）
     */
    private Boolean authQuery = true;

    /**
     * 权限查询数据库字段，可以为多个，为空时默认为city_id
     */
    private List<Properties> authColumn;

    /**
     * 权限查询数据库多个字段时，字段的判断方式，null为or关系
     * 0：or
     * 1：and
     */
    private String authColumnType;

    /**
     * 权限表查询时的别名
     */
    private String authTableAlias;

    /**
     * 拼接好的权限sql
     * 全部权限为 null
     * 格式为：(authTableAlias.authColumn in (bizBaseAccount.cityIdDataScope) authColumnType authTableAlias.authColumn in (bizBaseAccount.cityIdDataScope))
     */
    private String authSql;

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

    public String getAuthTableAlias() {
        return authTableAlias;
    }

    public void setAuthTableAlias(String authTableAlias) {
        this.authTableAlias = authTableAlias;
    }

    public List<Properties> getAuthColumn() {
        return authColumn;
    }

    public void setAuthColumn(List<Properties> authColumn) {
        this.authColumn = authColumn;
    }

    public String getAuthColumnType() {
        return authColumnType;
    }

    public void setAuthColumnType(String authColumnType) {
        this.authColumnType = authColumnType;
    }

    public Boolean getAutoAppendAuth() {
        return autoAppendAuth;
    }

    public void setAutoAppendAuth(Boolean autoAppendAuth) {
        this.autoAppendAuth = autoAppendAuth;
    }
}
