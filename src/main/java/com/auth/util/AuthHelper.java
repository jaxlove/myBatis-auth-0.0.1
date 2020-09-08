package com.auth.util;

import com.auth.authSql.ScopeSql;
import com.auth.entity.AuthQueryInfo;
import com.auth.entity.BaseAuthInfo;
import com.auth.entity.SimpleAuthInfo;
import com.auth.exception.AuthException;

/**
 * @author wangdejun
 * @description: 获取权限信息类
 * @date 2019/9/20 14:33
 */
public class AuthHelper {

    private static final ThreadLocal<BaseAuthInfo> LOCAL_CUR_AUTHINFO = new InheritableThreadLocal<>();

    /**
     * todo
     * 需要自行set，否则权限sql不会生效
     *
     * @param authInfo
     */
    public static void setCurSearch(BaseAuthInfo authInfo) throws AuthException {
        LOCAL_CUR_AUTHINFO.set(authInfo);
        //设置权限sql where条件
        if (authInfo.getAuthQuery() != null && authInfo.getAuthQuery()) {
            ScopeSql scopeSql = AuthSqlUtils.getAuthWhere();
            authInfo.setAuthScopSql(scopeSql);
        }
    }

    /**
     * 设置 权限开关
     *
     * @param authQuery
     */
    public static void setAuthQuery(boolean authQuery) {
        if (LOCAL_CUR_AUTHINFO.get() != null) {
            LOCAL_CUR_AUTHINFO.get().setAutoAppendAuth(authQuery);
        }
    }

    public static void removeCurSearchInfo() {
        LOCAL_CUR_AUTHINFO.remove();
    }

    public static BaseAuthInfo getCurSearchInfo() {
        return LOCAL_CUR_AUTHINFO.get();
    }

    private static AuthQueryInfo getAuthQueryInfo(SimpleAuthInfo entityWithParam) {
        AuthQueryInfo authQueryInfo = new AuthQueryInfo();
        authQueryInfo.setAutoAppendAuth(entityWithParam.getAutoAppendAuth());
        authQueryInfo.setAuthQuery(entityWithParam.getAuthQuery());
        authQueryInfo.setAuthTableAlias(entityWithParam.getAuthTableAlias());
        authQueryInfo.setAuthColumn(entityWithParam.getAuthColumn());
        authQueryInfo.setRelationTypeEnum(entityWithParam.getRelationTypeEnum());
        return authQueryInfo;
    }

}
