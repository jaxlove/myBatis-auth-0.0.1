package com.auth.util;

import com.auth.exception.AuthException;

public interface AuthWhereHandler {

    AuthQueryInfo curSearchInfo = AuthHelper.getCurSearchInfo();

    String getWhere(String tableNameAlias) throws AuthException;

}
