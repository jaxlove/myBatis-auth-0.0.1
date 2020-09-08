package com.auth.authSql;

import com.auth.exception.AuthException;

public interface AuthWhereHandler {
    ScopeSql getWhere(String tableNameAlias) throws AuthException;
}
