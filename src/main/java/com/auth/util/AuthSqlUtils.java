package com.auth.util;

import com.auth.exception.AuthException;
import com.auth.exception.UnknownAuthTypeException;
import com.auth.plugin.Configuration;
import com.auth.util.pagehelper.PageHelperUtil;

import java.util.List;
import java.util.Properties;

/**
 * @author wangdejun
 * @description: 权限sql处理类
 * @date 2020/7/27 18:44
 */
public class AuthSqlUtils {

    public static String getAuthSql(String sql, String mappedStatementId, Object parameterObject) throws AuthException {
        AuthQueryInfo curSearchInfo = AuthHelper.getCurSearchInfo();
        StringBuilder sqlBuilder = new StringBuilder(sql);
        PageHelperUtil.setNativeSql(sqlBuilder, mappedStatementId, parameterObject);
        SelectSqlParser selectSqlParser = new SelectSqlParser(sql);
        //获取权限where条件
        String authSqlWhere;
        switch (Configuration.getAuthType()) {
            case SIMPLE:
                //权限字段
                List<Properties> authColumns = curSearchInfo.getAuthColumn();
                if (authColumns == null || authColumns.isEmpty()) {
                    if (authColumns == null || authColumns.isEmpty()) {
                        authColumns = Configuration.getAuthColumn();
                        curSearchInfo.setAuthColumn(authColumns);
                    }
                    if (authColumns == null || authColumns.isEmpty()) {
                        throw new AuthException("未获取到权限列信息");
                    }
                }
                if (selectSqlParser.hasColumn(authColumns)) {
                    //select ${AUTH_ALIAS}.* from ( sql ) ${AUTH_ALIAS} where authSql
                    authSqlWhere = new SimpleAbstractAuthWhereHandler().getWhere(curSearchInfo.getCurTableAlias());
                    appendOutSideAuth(sqlBuilder);
                    selectSqlParser = new SelectSqlParser(sqlBuilder.toString());
                    selectSqlParser.setWhere(authSqlWhere);
                    sqlBuilder = new StringBuilder(selectSqlParser.getParsedSql());
                } else {
                    // select ${AUTH_ALIAS}.* from ( sql authSql )${AUTH_ALIAS}
                    authSqlWhere = new SimpleAbstractAuthWhereHandler().getWhere(curSearchInfo.getCurTableAlias());
                    selectSqlParser.setWhere(authSqlWhere);
                    sqlBuilder = new StringBuilder(selectSqlParser.getParsedSql());
                    appendOutSideAuth(sqlBuilder);
                }
                break;
            case COMPLEX:
                authSqlWhere = new ComplexAbstractAuthWhereHandler().getWhere(Configuration.getAuthColumnTableAlias());
                appendOutSideAuth(sqlBuilder);
                selectSqlParser = new SelectSqlParser(sqlBuilder.toString());
                selectSqlParser.setWhere(authSqlWhere);
                sqlBuilder = new StringBuilder(selectSqlParser.getParsedSql());
                break;
            default:
                throw new UnknownAuthTypeException("未知权限查询类型");
        }
        PageHelperUtil.sufHandler(sqlBuilder, mappedStatementId, parameterObject);
        return sqlBuilder.toString();
    }

    /**
     * 在原始sql外层加上auth
     *
     * @return
     */
    private static void appendOutSideAuth(StringBuilder sqlBuffer) {
        sqlBuffer.insert(0, "select " + Configuration.getAuthTableAlias() + ".* from (").append(") " + Configuration.getAuthTableAlias());
    }

    public static void main(String[] args) {
        SelectSqlParser selectSqlParser = new SelectSqlParser("select * from (Select a from bb wHere a.name= '1') where a.name=x and age=12");
        selectSqlParser.setWhere("NAME = 12");
        System.out.println(selectSqlParser.getParsedSql());
    }


}
