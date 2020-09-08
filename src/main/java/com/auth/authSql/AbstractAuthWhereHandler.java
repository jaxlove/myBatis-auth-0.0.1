package com.auth.authSql;

import com.auth.entity.BaseAuthInfo;
import com.auth.entity.SimpleAuthInfo;
import com.auth.exception.AuthException;
import com.auth.plugin.AuthType;
import com.auth.plugin.Configuration;
import com.auth.util.AuthHelper;

import java.util.Set;

public abstract class AbstractAuthWhereHandler implements AuthWhereHandler {

    protected BaseAuthInfo authInfo = AuthHelper.getCurSearchInfo();

    @Override
    public ScopeSql getWhere(String tableNameAlias) throws AuthException {
        Scope scope = getWhereScope();
        if (scope.equals(Scope.NONE)) {
            return new ScopeSql(Scope.NONE, "(0=1)");
        } else if (scope.equals(Scope.ALL)) {
            return new ScopeSql(Scope.ALL);
        }
        return new ScopeSql(Scope.AUTH, getAuthWhere(tableNameAlias));
    }

    abstract String getAuthWhere(String tableNameAlias) throws AuthException;

    /**
     * 查询类型
     *
     * @return
     * @throws AuthException
     */
    private Scope getWhereScope() {
        //权限信息为空，权限查询为空或者false,返回null
        if (authInfo == null || authInfo.getAuthQuery() == null || !authInfo.getAuthQuery()) {
            return Scope.ALL;
        }
        Boolean allDataSign = authInfo.getAllDataSign();
        //超管，设置为全部数据权限
        if (allDataSign != null && allDataSign) {
            return Scope.ALL;
        }
        if (Configuration.getAuthType().equals(AuthType.SIMPLE)) {
            //无数据权限，返回空
            Set dataScope = ((SimpleAuthInfo) authInfo).getDataScope();
            if (!allDataSign && (dataScope == null || dataScope.isEmpty())) {
                return Scope.NONE;
            }
        }
        return Scope.AUTH;
    }


}
