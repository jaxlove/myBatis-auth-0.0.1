package com.auth.plugin;

import com.auth.util.RelationTypeEnum;

import java.util.List;
import java.util.Properties;

/**
 * @author wangdejun
 * @description: 全局配置
 * @date 2020/7/27 18:11
 */
public class Configuration {

    /**
     * 权限sql类型
     */
    private static SqlType sqlType;

    /**
     * 是否自动拼接权限sql
     */
    private static Boolean autoAppendAuth = false;

    /**
     * 是否为权限查询（为false，则不会自动拼接权限sql，且不会设置authSql字段值）
     */
    private static Boolean authQuery = true;

    /**
     * 自定义权限sql拼接
     */
    private static String appendAuthSql;

    /**
     * 权限查询数据库字段，可以为多个，为空时默认为city_id
     */
    private static List<Properties> authColumn;

    /**
     * 权限查询数据库多个字段时，字段的判断方式，null为or关系
     * 0：OR
     * 1：AND
     */
    private static RelationTypeEnum relationTypeEnum;

    /**
     * 权限表查询时的别名
     */
    private static String authTableAlias;

    private static Boolean initSuccess = true;

    public static SqlType getSqlType() {
        return sqlType;
    }

    protected static void setSqlType(SqlType sqlType) {
        Configuration.sqlType = sqlType;
    }

    public static Boolean getInitSuccess() {
        return initSuccess;
    }

    public static void setInitSuccess(Boolean initSuccess) {
        Configuration.initSuccess = initSuccess;
    }

    public static Boolean getAutoAppendAuth() {
        return autoAppendAuth;
    }

    public static void setAutoAppendAuth(Boolean autoAppendAuth) {
        Configuration.autoAppendAuth = autoAppendAuth;
    }

    public static Boolean getAuthQuery() {
        return authQuery;
    }

    public static void setAuthQuery(Boolean authQuery) {
        Configuration.authQuery = authQuery;
    }

    public static String getAppendAuthSql() {
        return appendAuthSql;
    }

    public static void setAppendAuthSql(String appendAuthSql) {
        Configuration.appendAuthSql = appendAuthSql;
    }

    public static List<Properties> getAuthColumn() {
        return authColumn;
    }

    public static void setAuthColumn(List<Properties> authColumn) {
        Configuration.authColumn = authColumn;
    }

    public static RelationTypeEnum getRelationTypeEnum() {
        return relationTypeEnum;
    }

    public static void setRelationTypeEnum(RelationTypeEnum relationTypeEnum) {
        Configuration.relationTypeEnum = relationTypeEnum;
    }

    public static String getAuthTableAlias() {
        return authTableAlias;
    }

    public static void setAuthTableAlias(String authTableAlias) {
        Configuration.authTableAlias = authTableAlias;
    }
}

