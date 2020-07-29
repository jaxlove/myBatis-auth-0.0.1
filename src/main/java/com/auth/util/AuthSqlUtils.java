package com.auth.util;

import com.auth.exception.AuthException;
import com.auth.exception.UnknownAuthTypeException;
import com.auth.plugin.Configuration;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.TypeAliasRegistry;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author wangdejun
 * @description: 权限sql处理类
 * @date 2020/7/27 18:44
 */
public class AuthSqlUtils {

    public static String getAuthSql(String sql) throws AuthException {
        AuthQueryInfo curSearchInfo = AuthHelper.getCurSearchInfo();
        //权限字段
        List<Properties> authColumn = curSearchInfo.getAuthColumn();
        StringBuilder sqlBuffer = new StringBuilder(sql);
        PageHelperUtil.preHandler(sqlBuffer);
        SelectSqlParser selectSqlParser = new SelectSqlParser(sql);
        //获取权限where条件
        String authSqlWhere;
        switch (Configuration.getAuthType()) {
            case SIMPLE:
                if (selectSqlParser.hasColumn(authColumn)) {
                    //select ${AUTH_ALIAS}.* from ( sql ) ${AUTH_ALIAS} where authSql
                    authSqlWhere = getAuthSqlWhere(Configuration.getAuthColumnTableAlias());
                    appendOutSideAuth(sqlBuffer);
                    selectSqlParser = new SelectSqlParser(sqlBuffer.toString());
                    selectSqlParser.setWhere(authSqlWhere);
                } else {
                    // select ${AUTH_ALIAS}.* from ( sql authSql )${AUTH_ALIAS}
                    authSqlWhere = getAuthSqlWhere(null);
                    selectSqlParser.setWhere(authSqlWhere);
                    appendOutSideAuth(sqlBuffer);
                }
                break;
            case COMPLEX:
//                todo 待实现
                break;
            default:
                throw new UnknownAuthTypeException("未知查询类型");
        }
        PageHelperUtil.sufHandler(sqlBuffer);
        return sqlBuffer.toString();
    }

    /**
     * 在原始sql外层加上auth
     *
     * @return
     */
    private static void appendOutSideAuth(StringBuilder sqlBuffer) {
        sqlBuffer.insert(0, "select " + Configuration.getAuthColumnTableAlias() + ".* from (").append(") " + Configuration.getAuthColumnTableAlias());
    }

    /**
     * 获取权限sql where条件部分
     * null 不带权限，或 全部权限
     *
     * @return
     */
    public static String getAuthSqlWhere(String tableNameAlias) {
        AuthQueryInfo curSearchInfo = AuthHelper.getCurSearchInfo();
        //权限信息为空，权限查询为空或者false,返回null
        if (curSearchInfo == null || curSearchInfo.getAuthQuery() == null || !curSearchInfo.getAuthQuery()) {
            return null;
        }
        //超管，设置为全部数据权限
        if (curSearchInfo.isAllDataSign()) {
            curSearchInfo.setDataScope(new HashSet<>(Arrays.asList(0)));
        }
        //无数据权限，返回空
        if (CollectionUtils.isEmpty(curSearchInfo.getDataScope())) {
            return "(0 = 1)";
        }
        //权限字段
        List<Properties> authColumnNames = curSearchInfo.getAuthColumn();
        //判断当前sql查询出来的字段，是否包含任一权限字段或者x.*,如果包含，则将原始sql包在一起，外部加上${AUTH_ALIAS}再加上权限条件,如果没有，则直接加上权限条件，再加上${AUTH_ALIAS}
        RelationTypeEnum relationTypeEnum = curSearchInfo.getRelationTypeEnum();
        Set<Integer> authValue = curSearchInfo.getDataScope();
        tableNameAlias = StringUtils.isBlank(tableNameAlias) ? curSearchInfo.getAuthTableAlias() : tableNameAlias;
        //获取所有的权限的条件
        List<String> authAuthList = new ArrayList<>();
        for (Properties authColumn : authColumnNames) {
            String singleColumnAuth = null;
            if (authValue.contains(0)) {
//                 如果，true，即使权限为所有，也进行权限字段拼接 eg：nvl(a.city_id,-999999) = nvl(a.city_id,-999999)
                //wdjtodo 待修改
                Boolean authColumnCheck = Boolean.getBoolean(authColumn.getProperty("authColumnCheck"));
                if (authColumnCheck != null && authColumnCheck) {
                    singleColumnAuth = " nvl(" + getAliasAndColumnName(tableNameAlias, authColumn) + ",-999999) = nvl(" + getAliasAndColumnName(tableNameAlias, authColumn) + ",-999999)";
                }
            } else {
                Class jdbcType = new TypeAliasRegistry().resolveAlias(authColumn.getProperty("jdbcType"));
                singleColumnAuth = getAliasAndColumnName(tableNameAlias, authColumn) + " in (";
                if (Number.class.isAssignableFrom(jdbcType)) {
                    singleColumnAuth += StringUtils.join(authValue.toArray(), ",");
                } else if (jdbcType == String.class) {
                    List<String> authValueStrList = authValue.stream().map(t -> "'" + t + "'").collect(Collectors.toList());
                    singleColumnAuth += StringUtils.join(authValueStrList, ",");
                } else {

                }
                singleColumnAuth += ")";
            }
            if (StringUtils.isNotBlank(singleColumnAuth)) {
                authAuthList.add(singleColumnAuth);
            }
        }
        //返回where条件
        if (CollectionUtils.isEmpty(authAuthList)) {
            return null;
        }
        if (authAuthList.size() == 1) {
            return "(" + authAuthList.get(0) + ")";
        } else {
            return "(" + StringUtils.join(authAuthList, " " + relationTypeEnum.getOperator() + " ") + ")";
        }
    }

    private static String getAliasAndColumnName(String alias, Properties columnInfo) {
        return StringUtils.isBlank(alias) ? columnInfo.getProperty("column") : alias + "." + columnInfo.getProperty("column");
    }

}
