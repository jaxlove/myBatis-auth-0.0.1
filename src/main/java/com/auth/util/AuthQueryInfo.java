package com.auth.util;


import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * @author wangdejun
 * @description: 权限信息类
 * @date 2019/9/20 14:55
 */
public class AuthQueryInfo {

    /**
     * 是否为超级管理员，是，不需要权限
     */
    private boolean isSuperAdmin;

    /**
     * 地市权限
     */
    private Set<Integer> cityIdDataScope;

    /**
     * 是否自动拼接权限sql
     */
    private Boolean autoAppendAuth;

    /**
     * 是否为权限查询
     */
    private Boolean authQuery;

    /**
     * 权限查询数据库字段，可以为多个，为空时默认为<city_id,Integer>
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

    public Boolean getAuthQuery() {
        return authQuery;
    }

    public void setAuthQuery(Boolean authQuery) {
        this.authQuery = authQuery;
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

    public Set<Integer> getCityIdDataScope() {
        return cityIdDataScope;
    }

    public boolean isSuperAdmin() {
        return isSuperAdmin;
    }

    public void setSuperAdmin(boolean superAdmin) {
        isSuperAdmin = superAdmin;
    }

    public void setCityIdDataScope(Set<Integer> cityIdDataScope) {
        this.cityIdDataScope = cityIdDataScope;
    }

    public Boolean getAutoAppendAuth() {
        return autoAppendAuth;
    }

    public void setAutoAppendAuth(Boolean autoAppendAuth) {
        this.autoAppendAuth = autoAppendAuth;
    }

    public static void main(String[] args) {
        System.out.println(".*".matches("(.*\\.)*\\*"));
    }
}

