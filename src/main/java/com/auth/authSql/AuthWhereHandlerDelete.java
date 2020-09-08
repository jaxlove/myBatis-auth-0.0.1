package com.auth.authSql;

import com.auth.exception.AuthException;

/**
 * @author wangdejun
 * @date 2020/9/7 11:53
 */
public class AuthWhereHandlerDelete implements AuthWhereHandler {

    private AuthWhereHandler delete;

    public AuthWhereHandlerDelete(AuthWhereHandler delete) {
        this.delete = delete;
    }

    @Override
    public ScopeSql getWhere(String tableNameAlias) throws AuthException {
        return delete.getWhere(tableNameAlias);
    }
}
