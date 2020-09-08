package com.auth.authSql;

import com.auth.entity.SimpleAuthInfo;
import com.auth.exception.AuthException;
import com.auth.exception.UnSurpportJdbcType;
import com.auth.util.RelationTypeEnum;
import com.auth.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author wangdejun
 * @description: SIMPLE 类型 sql where条件部分
 * @date 2020/7/30 20:33
 */
public class SimpleAbstractAuthWhereHandler extends AbstractAuthWhereHandler {

    @Override
    String getAuthWhere(String tableNameAlias) throws AuthException {
        //判断当前sql查询出来的字段，是否包含任一权限字段或者x.*,如果包含，则将原始sql包在一起，外部加上${AUTH_ALIAS}再加上权限条件,如果没有，则直接加上权限条件，再加上${AUTH_ALIAS}
        SimpleAuthInfo simpleAuthInfo = (SimpleAuthInfo) authInfo;
        RelationTypeEnum relationTypeEnum = simpleAuthInfo.getRelationTypeEnum();
        Set<Object> authValue = simpleAuthInfo.getDataScope();
        tableNameAlias = StringUtils.isBlank(tableNameAlias) ? simpleAuthInfo.getAuthTableAlias() : tableNameAlias;
        //获取所有的权限的条件
        List<String> authAuthList = new ArrayList<>();
        List<Properties> authColumns = simpleAuthInfo.getAuthColumn();
        for (Properties authColumn : authColumns) {
            String singleColumnAuth = null;
            //仅支持基础类型和字符串
            String type = authColumn.getProperty("type");
            singleColumnAuth = getAliasAndColumnName(tableNameAlias, authColumn) + " in (";
            if ("Number".equalsIgnoreCase(type)) {
                singleColumnAuth += StringUtils.join(authValue.toArray(), ",");
            } else if ("String".equalsIgnoreCase(type)) {
                List<String> authValueStrList = authValue.stream().map(t -> "'" + t + "'").collect(Collectors.toList());
                singleColumnAuth += StringUtils.join(authValueStrList, ",");
            } else {
                throw new UnSurpportJdbcType("不支持的列类型:" + type);
            }
            singleColumnAuth += ")";
            if (StringUtils.isNotBlank(singleColumnAuth)) {
                authAuthList.add(singleColumnAuth);
            }
        }
        if (authAuthList == null || authAuthList.isEmpty()) {
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
