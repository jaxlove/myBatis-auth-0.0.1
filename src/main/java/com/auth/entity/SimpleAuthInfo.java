package com.auth.entity;


import com.auth.util.RelationTypeEnum;

import java.util.List;
import java.util.Properties;

/**
 * @author wangdejun
 * @description: 简单权限查询信息
 * @date 2020/2/22 12:04
 */
public class SimpleAuthInfo extends BaseAuthInfo {

    /**
     * 权限查询数据库字段，可以为多个，为空时默认为city_id
     */
    private List<Properties> authColumn;

    /**
     * 权限查询数据库多个字段时，字段的判断方式，null为or关系
     * 0：or
     * 1：and
     */
    private RelationTypeEnum relationTypeEnum;

    /**
     * 权限表查询时的别名
     */
    private String authTableAlias;


    public String getAuthTableAlias() {
        return authTableAlias;
    }

    public void setAuthTableAlias(String authTableAlias) {
        this.authTableAlias = authTableAlias;
    }

    public List<Properties> getAuthColumn() {
        return authColumn;
    }

    public void setAuthColumn(List<Properties> authColumn) {
        this.authColumn = authColumn;
    }

    public RelationTypeEnum getRelationTypeEnum() {
        return relationTypeEnum;
    }

    public void setRelationTypeEnum(RelationTypeEnum relationTypeEnum) {
        this.relationTypeEnum = relationTypeEnum;
    }

}
