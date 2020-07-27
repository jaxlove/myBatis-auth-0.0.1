package com.auth.entity;

/**
 * @author wangdejun
 * @description: 复杂权限查询信息
 * @date 2020/7/27 18:33
 */
public class ComplextAuthInfo extends BaseAuthInfo {

    /**
     * 自定义权限sql拼接
     */
    private String appendAuthSql;

    public String getAppendAuthSql() {
        return appendAuthSql;
    }

    public void setAppendAuthSql(String appendAuthSql) {
        this.appendAuthSql = appendAuthSql;
    }
}
