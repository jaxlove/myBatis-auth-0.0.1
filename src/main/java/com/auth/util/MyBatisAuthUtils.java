package com.auth.util;

import com.auth.entity.BaseAuthInfo;
import com.auth.exception.AuthException;
import com.auth.plugin.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author wangdejun
 * @description: mybaits权限工具类
 * @date 2019/9/20 10:35
 */
public class MyBatisAuthUtils {

    private static Logger logger = LoggerFactory.getLogger(MyBatisAuthUtils.class);

    private static final String EMPTY_SQL = Configuration.getEmptySql();

    public static String getAuthSql(String sql, String mappedStatementId, Object parameterObject) throws AuthException {
        //没有权限查询信息，权限查询为空或者为false,自动拼接权限sql为空或者false，返回原sql
        //authInfo 新增同一线程可能需要使用多次（数据总数查询，列表查询），不可以将信息从curSearchInfo信息移除，移除操作需要自己实现
        BaseAuthInfo authInfo = AuthHelper.getCurSearchInfo();
        if (authInfo == null
                || authInfo.getAuthQuery() == null || !authInfo.getAuthQuery()
                || authInfo.getAutoAppendAuth() == null || !authInfo.getAutoAppendAuth()) {
            return sql;
        }
        //sql为空，或者已经包含 ${AUTH_ALIAS}，返回原sql
        if (StringUtils.isBlank(sql) || sql.contains(Configuration.getAuthTableSign())) {
            return sql;
        }
        return AuthSqlUtils.getAuthSql(sql, mappedStatementId, parameterObject);
    }

}
