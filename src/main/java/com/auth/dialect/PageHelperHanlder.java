package com.auth.dialect;

import com.auth.exception.AuthException;

public interface PageHelperHanlder {

    String removePagehelperSelectSql(String sql);

    String removePagehelperCountSql(String sql) throws AuthException;

    String selectSufHandler(String sql);
}
