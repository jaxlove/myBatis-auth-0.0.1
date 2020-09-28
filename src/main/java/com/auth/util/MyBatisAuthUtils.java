package com.auth.util;

import com.auth.authSql.ScopeSql;
import com.auth.exception.AuthException;
import org.apache.ibatis.mapping.ResultMap;

import java.util.List;

/**
 * @author wangdejun
 * @description: mybaits权限工具类
 * @date 2019/9/20 10:35
 */
public class MyBatisAuthUtils {

    public static ScopeSql getAuthSql(String sql, List<ResultMap> resultMaps, String mappedStatementId, Object parameterObject) throws AuthException {
        return AuthSqlUtils.getAuthSql(sql, resultMaps, mappedStatementId, parameterObject);
    }

}
