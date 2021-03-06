package com.auth.util;

/**
 * @author wangdejun
 * @description: 字段关系枚举
 * @date 2020/7/21 18:34
 */
public enum RelationTypeEnum {
    AND("and"), OR("or");
    private String operator;
    RelationTypeEnum(String operator) {
        this.operator = operator;
    }

    public String getOperator() {
        return operator;
    }
}
