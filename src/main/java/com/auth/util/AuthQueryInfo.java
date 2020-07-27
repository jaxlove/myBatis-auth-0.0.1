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
     * 是否查询全部数据
     */
    private boolean allDataSign = false;

    /**
     * 数据权限
     */
    private Set dataScope;

    /**
     * 是否自动拼接权限sql
     */
    private Boolean autoAppendAuth;

    /**
     * 是否为权限查询
     */
    private Boolean authQuery;

    /**
     * 权限查询数据库字段，可以为多个
     */
    private List<Properties> authColumn;

    /**
     * 权限查询数据库多个字段时，字段的判断方式，null为or关系
     * 0：or
     * 1：and
     */
    private RelationTypeEnum relationTypeEnum;

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

    public RelationTypeEnum getRelationTypeEnum() {
        return relationTypeEnum;
    }

    public void setRelationTypeEnum(RelationTypeEnum relationTypeEnum) {
        this.relationTypeEnum = relationTypeEnum;
    }

    public Set getDataScope() {
        return dataScope;
    }

    public boolean isAllDataSign() {
        return allDataSign;
    }

    public void setAllDataSign(boolean allDataSign) {
        this.allDataSign = allDataSign;
    }

    public void setDataScope(Set dataScope) {
        this.dataScope = dataScope;
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

