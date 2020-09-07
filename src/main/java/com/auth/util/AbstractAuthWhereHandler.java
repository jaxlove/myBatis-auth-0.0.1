package com.auth.util;

import com.auth.authSql.WhereScope;
import com.auth.authSql.WhereSql;
import com.auth.entity.BaseAuthInfo;
import com.auth.entity.SimpleAuthInfo;
import com.auth.exception.AuthException;
import com.auth.plugin.AuthType;
import com.auth.plugin.Configuration;
import org.apache.commons.collections4.CollectionUtils;

public abstract class AbstractAuthWhereHandler {

    protected BaseAuthInfo authInfo = AuthHelper.getCurSearchInfo();

    public WhereSql getWhere(String tableNameAlias) throws AuthException {
        WhereScope whereScope = getWhereScope();
        if (whereScope.equals(WhereScope.NONE)) {
            return new WhereSql(WhereScope.NONE, "(0=1)");
        } else if (whereScope.equals(WhereScope.ALL)) {
            new WhereSql(WhereScope.ALL);
        }
        return new WhereSql(WhereScope.AUTH, getAuthWhere(tableNameAlias));
    }

    abstract String getAuthWhere(String tableNameAlias) throws AuthException;

    /**
     * 查询类型
     *
     * @return
     * @throws AuthException
     */
    private WhereScope getWhereScope() throws AuthException {
        //权限信息为空，权限查询为空或者false,返回null
        if (authInfo == null || authInfo.getAuthQuery() == null || !authInfo.getAuthQuery()) {
            return WhereScope.ALL;
        }
        Boolean allDataSign = authInfo.isAllDataSign();
        //超管，设置为全部数据权限
        if (allDataSign != null && allDataSign) {
            return null;
        }
        if (Configuration.getAuthType().equals(AuthType.SIMPLE)) {
            //无数据权限，返回空
            if (!allDataSign && CollectionUtils.isEmpty(((SimpleAuthInfo) authInfo).getDataScope())) {
                return WhereScope.NONE;
            }
        }
        return WhereScope.AUTH;
    }


}
