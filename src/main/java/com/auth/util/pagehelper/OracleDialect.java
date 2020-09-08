package com.auth.util.pagehelper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author wangdejun
 * @description: oracle pagehelper处理类
 * @date 2020/9/4 11:12
 */
public class OracleDialect implements DialectHandler {

    //分页sql的前面的sql
    private static ThreadLocal<String> pageHelperPreSqlThread = ThreadLocal.withInitial(() -> "");

    //分页sql的后面的sql
    private static ThreadLocal<String> pageHelperSufSqlThread = ThreadLocal.withInitial(() -> "");

    @Override
    public String getNativeSelectSql(String sql) {
        Pattern pattern = Pattern.compile("\\)(\\s){0,}tmp_page(.*)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);
        String originSql = null;
        while (matcher.find()) {
            originSql = sql.substring(0, matcher.start());
            pageHelperSufSqlThread.set(matcher.group(0) + sql.substring(matcher.end()));
        }
        // ) 左括号的个数
        long count = (pageHelperSufSqlThread.get().length()) - (pageHelperSufSqlThread.get().replaceAll("\\)", "").length());
        String pageHelperPreSql = "";
        for (int i = 0; i < count; i++) {
            pageHelperPreSql += originSql.substring(0, originSql.indexOf("(") + 1);
            originSql = originSql.substring(originSql.indexOf("(") + 1);
        }
        pageHelperPreSqlThread.set(pageHelperPreSql);
        return originSql;
    }

    @Override
    public String getNativeCountSql(String sql) {
        return null;
    }

    @Override
    public String sufHandler(String sql) {
        sql = pageHelperPreSqlThread.get() + sql + pageHelperSufSqlThread.get();
        pageHelperPreSqlThread.remove();
        pageHelperSufSqlThread.remove();
        return sql;
    }
}
