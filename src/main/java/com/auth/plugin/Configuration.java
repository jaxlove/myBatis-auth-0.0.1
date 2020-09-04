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
    private static AuthType authType;

    /**
     * 数据库
     */
    private static String dialect = null;

    /**
     * 空sql
     */
    private static String EMPTY_SQL = null;

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
     * 是否在全量权限时，检验sql
     */
    private static Boolean authColumnCheck;

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
    private static String authColumnTableAlias;

    private static boolean initSuccess = true;

    private static final String AUTH_TABLE_ALIAS = "AUTH_TABLE_ALIAS";

    public static Boolean getAuthColumnCheck() {
        return authColumnCheck;
    }

    public static void setAuthColumnCheck(Boolean authColumnCheck) {
        Configuration.authColumnCheck = authColumnCheck;
    }

    public static String getAuthTableAlias() {
        return AUTH_TABLE_ALIAS;
    }

    public static boolean isInitSuccess() {
        return initSuccess;
    }

    public static void setInitSuccess(boolean initSuccess) {
        Configuration.initSuccess = initSuccess;
    }

    public static String getEmptySql() {
        return EMPTY_SQL;
    }

    public static void setEmptySql(String emptySql) {
        EMPTY_SQL = emptySql;
    }

    public static String getDialect() {
        return dialect;
    }

    public static void setDialect(String dialect) {
        Configuration.dialect = dialect;
    }

    public static AuthType getAuthType() {
        return authType;
    }

    protected static void setAuthType(AuthType authType) {
        Configuration.authType = authType;
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

    public static String getAuthColumnTableAlias() {
        return authColumnTableAlias;
    }

    public static void setAuthColumnTableAlias(String authColumnTableAlias) {
        Configuration.authColumnTableAlias = authColumnTableAlias;
    }
}

