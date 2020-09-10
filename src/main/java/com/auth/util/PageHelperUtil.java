package com.auth.util;

import com.auth.dialect.DialectUtil;
import com.auth.dialect.PageHelperHanlder;
import com.auth.plugin.Configuration;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author wangdejun
 * @description: PageHelper 插件处理类
 * @date 2020/7/27 18:42
 */
public class PageHelperUtil {

    private static PageHelperHanlder pageHelperHanlder;

    private static AtomicBoolean inited = new AtomicBoolean(false);

    private static void init() {
        String dialect = Configuration.getDialect();
        if (StringUtils.isNotBlank(dialect)) {
            pageHelperHanlder = DialectUtil.getDialect(dialect);
        }
    }


    public static String sufHandler(String sql, String mappedStatementId, Object parameterObject) {
        if (!inited.getAndSet(true)) {
            init();
        }
        return pageHelperHanlder.selectSufHandler(sql);
    }

    /**
     * 将pageHelper拼接的分页sql移除，并保存下来
     *
     * @param sql
     * @return
     */
    public static String getNativeSql(String sql, String mappedStatementId, Object parameterObject) {
        if (!inited.getAndSet(true)) {
            init();
        }
        if (isPageSelect(parameterObject)) {
            return pageHelperHanlder.removePagehelperSelectSql(sql);
        }
        return sql;

    }


    /**
     * 获取pageHelper版本
     * 4和5 版本代码差异较大，逻辑也不同
     *
     * @return
     */
    public static int getPagerHelperVersion() {
        return 4;
    }

    /**
     * 是否为分页查询
     *
     * @return
     */
    private static boolean isPageSelect(Object parameterObject) {
        if (Map.class.isAssignableFrom(parameterObject.getClass()) &&
                (((Map) parameterObject).containsKey("First_PageHelper") || ((Map) parameterObject).containsKey("Second_PageHelper"))) {
            return true;
        }
        return false;
    }

    private static boolean isPageCount(StringBuilder sql) {
        SelectSqlParser selectSqlParser = new SelectSqlParser(sql.toString());
        String simpleSql = selectSqlParser.getSimpleSql();
        // select count(0) from #sub_sql# tmp_cout
        if (simpleSql.matches("select(\\s+)count\\((.+)\\)t(\\s+)fromt(\\s+)#sub_sql#t(\\s+)tmp_count")) {
            return true;
        }
        return false;
    }

    private static Class getClass(String className) {
        try {
            Class<?> class_ = Class.forName("className");
            return class_;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

}
