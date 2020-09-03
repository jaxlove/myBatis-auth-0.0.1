package com.auth.util;

import com.auth.exception.AuthException;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Properties;

public abstract class AbstractAuthWhereHandler {

    protected AuthQueryInfo curSearchInfo = AuthHelper.getCurSearchInfo();

    protected boolean allDataSign = false;

    /**
     * 未处理标识
     */
    protected String UnHandled = "UnHandled";

    public String getWhere(String tableNameAlias) throws AuthException {
        String preSet = authPreSet();
        if (!UnHandled.equals(preSet)) {
            return preSet;
        }
        return doGetWhere(tableNameAlias);
    }

    abstract String doGetWhere(String tableNameAlias) throws AuthException;

    protected String authPreSet() {
        //权限信息为空，权限查询为空或者false,返回null
        if (curSearchInfo == null || curSearchInfo.getAuthQuery() == null || !curSearchInfo.getAuthQuery()) {
            return null;
        }
        allDataSign = curSearchInfo.isAllDataSign();
        //超管，设置为全部数据权限
        if (allDataSign) {
            return null;
        }
        //无数据权限，返回空
        if (!allDataSign && CollectionUtils.isEmpty(curSearchInfo.getDataScope())) {
            return "(0 = 1)";
        }
        //权限字段
        List<Properties> authColumnNames = curSearchInfo.getAuthColumn();
        if (CollectionUtils.isEmpty(authColumnNames)) {
            return null;
        }
        return UnHandled;
    }

}
