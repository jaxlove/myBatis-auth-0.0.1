package com.auth.util;

import com.auth.entity.SimpleAuthInfo;

/**
 * @author wangdejun
 * @description: 获取权限信息类
 * @date 2019/9/20 14:33
 */
public class AuthHelper {

    private static final ThreadLocal<AuthQueryInfo> LOCAL_CUR_AUTHINFO = new ThreadLocal();

    public static void setCurSearch(AuthQueryInfo authQueryInfo) {
        LOCAL_CUR_AUTHINFO.set(authQueryInfo);
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

    public static AuthQueryInfo getCurSearchInfo() {
        return LOCAL_CUR_AUTHINFO.get();
    }

    /**
     * 刷新权限辅组类AuthHelper信息
     *
     * @param simpleAuthInfo
     */
    public static AuthQueryInfo refreshAuthHelper(SimpleAuthInfo simpleAuthInfo) {
        if (LOCAL_CUR_AUTHINFO.get() != null) {
            synchronized (LOCAL_CUR_AUTHINFO.get()) {
                return doRefreshAuthHelper(simpleAuthInfo);
            }
        } else {
            return doRefreshAuthHelper(simpleAuthInfo);
        }
    }

    private static AuthQueryInfo doRefreshAuthHelper(SimpleAuthInfo simpleAuthInfo) {
        AuthQueryInfo authQueryInfo = getAuthQueryInfo(simpleAuthInfo);
        AuthHelper.setCurSearch(authQueryInfo);
        //设置权限sql where条件
        if (authQueryInfo.getAuthQuery() != null && simpleAuthInfo.getAuthQuery()) {
            simpleAuthInfo.setAuthSql(MyBatisAuthUtils.getAuthSqlWhere(null));
        }
        return authQueryInfo;
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
