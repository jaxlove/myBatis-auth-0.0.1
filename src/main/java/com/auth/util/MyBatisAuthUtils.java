package com.auth.util;

import com.auth.authSql.ScopeSql;
import com.auth.exception.AuthException;

/**
 * @author wangdejun
 * @description: mybaits权限工具类
 * @date 2019/9/20 10:35
 */
public class MyBatisAuthUtils {

    //todo 如果返回类型是int等基本类型，不可返回null，应返回 0，待实现
    public static ScopeSql getAuthSql(String sql, String mappedStatementId, Object parameterObject) throws AuthException {
        return AuthSqlUtils.getAuthSql(sql, mappedStatementId, parameterObject);
    }

}
