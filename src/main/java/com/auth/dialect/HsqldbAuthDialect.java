package com.auth.dialect;

import com.auth.util.SelectSqlParser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author wangdejun
 * @description: pgsql pagehelper处理类
 * @date 2020/9/4 11:12
 */
public class HsqldbAuthDialect implements DialectHandler {

    //分页sql的前面的sql
    private static ThreadLocal<String> pageHelperPreSqlThread = ThreadLocal.withInitial(() -> "");

    //分页sql的后面的sql
    private static final ThreadLocal<String> pageHelperSufSqlThread = ThreadLocal.withInitial(() -> "");

    @Override
    public String removePagehelperSelectSql(String sql) {
        Pattern pattern = Pattern.compile("(\\s){0,}limit(.*)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);
        String originSql = null;
        while (matcher.find()) {
            originSql = sql.substring(0, matcher.start());
            pageHelperSufSqlThread.set(matcher.group(0) + sql.substring(matcher.end()));
        }
        return originSql;
    }

    @Override
    public String removePagehelperCountSql(String sql) {
        SelectSqlParser selectSqlParser = new SelectSqlParser(sql);
        String simpleSql = selectSqlParser.getSimpleSql();
        String[] split = simpleSql.split(SelectSqlParser.SUB_SQL_SIGNAL);
        pageHelperPreSqlThread.set(split[0]);
        if (split.length > 1) {
            pageHelperSufSqlThread.set(split[1]);
        }
        return selectSqlParser.getSubSqlList().get(0).getSubSql();
    }

    @Override
    public String selectSufHandler(String sql) {
        return pageHelperPreSqlThread + "(" + sql + ")" + pageHelperSufSqlThread.get();
    }

    @Override
    public String getEmptySql() {
        return "select 0 where 1=0";
    }

}
