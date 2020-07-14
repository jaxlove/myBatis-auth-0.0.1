package com.auth.util;

import com.auth.entity.AuthQueryEntity;

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
     * @param authQueryEntity
     */
    public static AuthQueryInfo refreshAuthHelper(AuthQueryEntity authQueryEntity) {
        if (LOCAL_CUR_AUTHINFO.get() != null) {
            synchronized (LOCAL_CUR_AUTHINFO.get()) {
                return doRefreshAuthHelper(authQueryEntity);
            }
        } else {
            return doRefreshAuthHelper(authQueryEntity);
        }
    }

    private static AuthQueryInfo doRefreshAuthHelper(AuthQueryEntity authQueryEntity) {
        AuthQueryInfo authQueryInfo = getAuthQueryInfo(authQueryEntity);
        AuthHelper.setCurSearch(authQueryInfo);
        //设置权限sql where条件
        if (authQueryInfo.getAuthQuery() != null && authQueryEntity.getAuthQuery()) {
            authQueryEntity.setAuthSql(com.tydic.auth.util.MyBatisAuthUtils.getAuthSqlWhere(null));
        }
        return authQueryInfo;
    }

    private static AuthQueryInfo getAuthQueryInfo(AuthQueryEntity entityWithParam) {
        AuthQueryInfo authQueryInfo = new AuthQueryInfo();
        authQueryInfo.setAutoAppendAuth(entityWithParam.getAutoAppendAuth());
        authQueryInfo.setAuthQuery(entityWithParam.getAuthQuery());
        authQueryInfo.setAuthTableAlias(entityWithParam.getAuthTableAlias());
        authQueryInfo.setAuthColumn(entityWithParam.getAuthColumn());
        authQueryInfo.setAuthColumnType(entityWithParam.getAuthColumnType());
        return authQueryInfo;
    }

}
