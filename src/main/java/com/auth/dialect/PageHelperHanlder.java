package com.auth.dialect;

public interface PageHelperHanlder {

    String removePagehelperSelectSql(String sql);

    String removePagehelperCountSql(String sql);

    String selectSufHandler(String sql);
}
