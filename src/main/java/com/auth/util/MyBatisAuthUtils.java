package com.auth.util;

import com.auth.authSql.Scope;
import com.auth.authSql.ScopeSql;
import com.auth.entity.BaseAuthInfo;
import com.auth.exception.AuthException;
import com.auth.plugin.Configuration;
import com.auth.util.StringUtils;

/**
 * @author wangdejun
 * @description: mybaits权限工具类
 * @date 2019/9/20 10:35
 */
public class MyBatisAuthUtils {


    private static final String EMPTY_SQL = Configuration.getEmptySql();

    public static ScopeSql getAuthSql(String sql, String mappedStatementId, Object parameterObject) throws AuthException {
        return AuthSqlUtils.getAuthSql(sql, mappedStatementId, parameterObject);
    }

}
