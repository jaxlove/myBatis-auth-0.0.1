package com.auth.util;

import com.auth.exception.AuthException;
import com.auth.exception.UnSurpportJdbcType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.TypeAliasRegistry;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author wangdejun
 * @description: SIMPLE 类型 sql where条件部分
 * @date 2020/7/30 20:33
 */
public class SimpleAuthWhereHandler implements AuthWhereHandler {
    @Override
    public String getWhere(String tableNameAlias) throws AuthException {
        //权限信息为空，权限查询为空或者false,返回null
        if (curSearchInfo == null || curSearchInfo.getAuthQuery() == null || !curSearchInfo.getAuthQuery()) {
            return null;
        }
        boolean allDataSign = curSearchInfo.isAllDataSign();
        //超管，设置为全部数据权限
        if (allDataSign) {
            curSearchInfo.setDataScope(new HashSet<>(Arrays.asList(0)));
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
        //判断当前sql查询出来的字段，是否包含任一权限字段或者x.*,如果包含，则将原始sql包在一起，外部加上${AUTH_ALIAS}再加上权限条件,如果没有，则直接加上权限条件，再加上${AUTH_ALIAS}
        RelationTypeEnum relationTypeEnum = curSearchInfo.getRelationTypeEnum();
        Set<Integer> authValue = curSearchInfo.getDataScope();
        tableNameAlias = StringUtils.isBlank(tableNameAlias) ? curSearchInfo.getAuthTableAlias() : tableNameAlias;
        //获取所有的权限的条件
        List<String> authAuthList = new ArrayList<>();
        for (Properties authColumn : authColumnNames) {
            String singleColumnAuth = null;
            if (allDataSign) {
                Boolean authColumnCheck = Boolean.getBoolean(authColumn.getProperty("authColumnCheck"));
                if (authColumnCheck != null && authColumnCheck) {
                    singleColumnAuth = " nvl(" + getAliasAndColumnName(tableNameAlias, authColumn) + ",-999999) = nvl(" + getAliasAndColumnName(tableNameAlias, authColumn) + ",-999999)";
                }
            } else {
                //仅支持基础类型和字符串
                Class jdbcType = new TypeAliasRegistry().resolveAlias(authColumn.getProperty("jdbcType"));
                singleColumnAuth = getAliasAndColumnName(tableNameAlias, authColumn) + " in (";
                if (Number.class.isAssignableFrom(jdbcType)) {
                    singleColumnAuth += StringUtils.join(authValue.toArray(), ",");
                } else if (jdbcType == String.class) {
                    List<String> authValueStrList = authValue.stream().map(t -> "'" + t + "'").collect(Collectors.toList());
                    singleColumnAuth += StringUtils.join(authValueStrList, ",");
                } else {
                    throw new UnSurpportJdbcType("不支持的JdbcType:" + jdbcType.getSimpleName());
                }
                singleColumnAuth += ")";
            }
            if (StringUtils.isNotBlank(singleColumnAuth)) {
                authAuthList.add(singleColumnAuth);
            }
        }
        //返回where条件
        if (CollectionUtils.isEmpty(authAuthList)) {
            return null;
        }
        if (authAuthList.size() == 1) {
            return "(" + authAuthList.get(0) + ")";
        } else {
            return "(" + StringUtils.join(authAuthList, " " + relationTypeEnum.getOperator() + " ") + ")";
        }
    }

    private static String getAliasAndColumnName(String alias, Properties columnInfo) {
        return StringUtils.isBlank(alias) ? columnInfo.getProperty("column") : alias + "." + columnInfo.getProperty("column");
    }


}
