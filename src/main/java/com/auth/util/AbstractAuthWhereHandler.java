package com.auth.util;

import com.auth.exception.AuthException;
import com.auth.plugin.Configuration;
import org.apache.commons.collections4.CollectionUtils;

public abstract class AbstractAuthWhereHandler {

    protected AuthQueryInfo curSearchInfo = AuthHelper.getCurSearchInfo();

    protected boolean allDataSign = false;

    public String getWhere(String tableNameAlias) throws AuthException {
        Sign sign = authPreSet();
        if (sign.equals(Sign.NONE)) {
            return Configuration.getEmptySql();
        } else if (sign.equals(Sign.ALL)) {
            return null;
        }
        return doGetWhere(tableNameAlias);
    }

    abstract String doGetWhere(String tableNameAlias) throws AuthException;

    private Sign authPreSet() throws AuthException {
        //权限信息为空，权限查询为空或者false,返回null
        if (curSearchInfo == null || curSearchInfo.getAuthQuery() == null || !curSearchInfo.getAuthQuery()) {
            return Sign.ALL;
        }
        allDataSign = curSearchInfo.isAllDataSign();
        //超管，设置为全部数据权限
        if (allDataSign) {
            return null;
        }
        //无数据权限，返回空
        if (!allDataSign && CollectionUtils.isEmpty(curSearchInfo.getDataScope())) {
            return Sign.NONE;
        }
        return Sign.AUTH;
    }

    enum Sign {
        AUTH, UN_HANDLED, ALL, NONE
    }

}
