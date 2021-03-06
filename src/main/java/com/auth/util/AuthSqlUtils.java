package com.auth.util;

import com.auth.authSql.*;
import com.auth.dialect.DialectUtil;
import com.auth.entity.BaseAuthInfo;
import com.auth.entity.SimpleAuthInfo;
import com.auth.exception.AuthException;
import com.auth.exception.UnknownAuthTypeException;
import com.auth.plugin.Configuration;
import org.apache.ibatis.mapping.ResultMap;

import java.util.List;
import java.util.Properties;

/**
 * @author wangdejun
 * @description: 权限sql处理类
 * @date 2020/7/27 18:44
 */
public class AuthSqlUtils {

    public static ScopeSql getAuthSql(String sql, List<ResultMap> resultMaps, String mappedStatementId, Object parameterObject) throws AuthException {
        BaseAuthInfo authInfo = AuthHelper.getCurSearchInfo();
        //获取权限where条件
        ScopeSql authWhere = authInfo.getAuthScopSql();
        if (authWhere == null) {
            getAuthWhere();
        }
        switch (authWhere.getScope()) {
            case ALL:
                return new ScopeSql(Scope.ALL, sql);
            case NONE:
                Class resultType = resultMaps.size() == 1 ? resultMaps.get(0).getType() : null;
                return new ScopeSql(Scope.NONE, DialectUtil.getDialect().getEmptySql(resultType));
        }
        sql = PageHelperUtil.getNativeSql(sql, mappedStatementId, parameterObject);
        SelectSqlParser selectSqlParser = new SelectSqlParser(sql);
        String authSqlWhere = authWhere.getSql();
        switch (Configuration.getAuthType()) {
            case SIMPLE:
                //权限字段
                SimpleAuthInfo simpleAuthInfo = (SimpleAuthInfo) authInfo;
                List<Properties> authColumns = simpleAuthInfo.getAuthColumn();
                if (authColumns == null || authColumns.isEmpty()) {
                    throw new AuthException("未获取到权限列信息");
                }
                if (selectSqlParser.hasColumn(authColumns)) {
                    //select ${AUTH_ALIAS}.* from ( sql ) ${AUTH_ALIAS} where authSql
                    sql = appendOutSideAuth(sql);
                    selectSqlParser = new SelectSqlParser(sql);
                    selectSqlParser.setWhere(authSqlWhere);
                    sql = selectSqlParser.getParsedSql();
                } else {
                    // select ${AUTH_ALIAS}.* from ( sql authSql )${AUTH_ALIAS}
                    selectSqlParser.setWhere(authSqlWhere);
                    sql = selectSqlParser.getParsedSql();
                    sql = appendOutSideAuth(sql);
                }
                break;
            case COMPLEX:
                //todo 乱写的，待实现
                sql = appendOutSideAuth(sql);
                selectSqlParser = new SelectSqlParser(sql);
                selectSqlParser.setWhere(authSqlWhere);
                sql = selectSqlParser.getParsedSql();
                break;
            default:
                throw new UnknownAuthTypeException("未知权限查询类型");
        }
        String authSql = PageHelperUtil.sufHandler(sql, mappedStatementId, parameterObject);
        return new ScopeSql(Scope.AUTH, authSql);
    }

    /**
     * 在原始sql外层加上auth
     *
     * @return
     */
    private static String appendOutSideAuth(String sql) {
        return "select " + Configuration.getAuthTableSign() + ".* from (" + sql + ") " + Configuration.getAuthTableSign();
    }

    public static ScopeSql getAuthWhere() throws AuthException {
        BaseAuthInfo authInfo = AuthHelper.getCurSearchInfo();
        AuthWhereHandlerDelete authWhereHandlerDelete;
        SimpleAuthInfo simpleAuthInfo = (SimpleAuthInfo) authInfo;
        switch (Configuration.getAuthType()) {
            case SIMPLE:
                //权限字段
                authWhereHandlerDelete = new AuthWhereHandlerDelete(new SimpleAbstractAuthWhereHandler());
                break;
            case COMPLEX:
                authWhereHandlerDelete = new AuthWhereHandlerDelete(new ComplexAbstractAuthWhereHandler());
                break;
            default:
                throw new UnknownAuthTypeException("未知权限查询类型");
        }
        return authWhereHandlerDelete.getWhere(simpleAuthInfo.getAuthTableAlias());
    }

    public static void main(String[] args) {
        SelectSqlParser selectSqlParser = new SelectSqlParser("select * from (Select a from bb wHere a.name= '1') where a.name=x and age=12");
        selectSqlParser.setWhere("NAME = 12");
        System.out.println(selectSqlParser.getParsedSql());
    }


}
