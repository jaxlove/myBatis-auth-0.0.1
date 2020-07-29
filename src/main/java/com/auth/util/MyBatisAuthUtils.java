package com.auth.util;

import com.auth.plugin.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author wangdejun
 * @description: mybaits权限工具类
 * @date 2019/9/20 10:35
 */
public class MyBatisAuthUtils {

    private static Logger logger = LoggerFactory.getLogger(MyBatisAuthUtils.class);

    private static final String EMPTY_SQL = Configuration.getEmptySql();

    public static String getAuthSql(String sql) {
        //没有权限查询信息，权限查询为空或者为false,自动拼接权限sql为空或者false，返回原sql
        //curSearchInfo 新增同一线程可能需要使用多次（数据总数查询，列表查询），不可以将信息从curSearchInfo信息移除，移除信息放在DubboProviderAuthFilter中进行
        AuthQueryInfo curSearchInfo = AuthHelper.getCurSearchInfo();
        if (curSearchInfo == null
                || curSearchInfo.getAuthQuery() == null || !curSearchInfo.getAuthQuery()
                || curSearchInfo.getAutoAppendAuth() == null || !curSearchInfo.getAutoAppendAuth()) {
            return sql;
        }
        //sql为空，或者已经包含 ${AUTH_ALIAS}，返回原sql
        if (StringUtils.isBlank(sql) || sql.contains(Configuration.getAuthColumnTableAlias())) {
            return sql;
        }
        //全部数据权限
        if (curSearchInfo.isAllDataSign()) {
            return sql;
        }
        //无数据权限，返回空
        if (curSearchInfo.getDataScope() == null || curSearchInfo.getDataScope().isEmpty()) {
            return EMPTY_SQL;
        }
        return AuthSqlUtils.getAuthSql(sql);
    }

    /**
     * 向plainSelect中设置权限信息
     *
     * @param selectSqlParser
     * @param authSql
     */
    private static String setAuthWhere(SelectSqlParser selectSqlParser, String authSql) {
        //拼接where条件
        if (StringUtils.isBlank(authSql)) {
            return selectSqlParser.getParsedSql();
        }
        selectSqlParser.setWhere(authSql);
        return selectSqlParser.getParsedSql();
    }

}
