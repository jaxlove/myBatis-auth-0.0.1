package com.auth.util;

import com.auth.exception.AuthException;
import com.auth.plugin.Configuration;

/**
 * @author wangdejun
 * @description: COMPLEX 类型 sql where条件部分
 * @date 2020/7/30 20:34
 */
public class ComplexAbstractAuthWhereHandler extends AbstractAuthWhereHandler {

    @Override
    protected String doGetWhere(String tableAlias) throws AuthException {
        String appendAuthSql = Configuration.getAppendAuthSql();
        if (!appendAuthSql.trim().startsWith("(")) {
            return "(" + appendAuthSql + ")";
        }
        return appendAuthSql;
    }
}
