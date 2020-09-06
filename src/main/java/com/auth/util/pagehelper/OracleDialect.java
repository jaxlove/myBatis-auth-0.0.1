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
    public void getNativeSelectSql(StringBuilder sql) {
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
            originSql.substring(originSql.indexOf("(") + 1);
        }
        pageHelperPreSqlThread.set(pageHelperPreSql);
    }

    @Override
    public void getNativeCountSql(StringBuilder sql) {

    }

    @Override
    public void sufHandler(StringBuilder sql) {
        sql.insert(0, pageHelperPreSqlThread.get()).append(pageHelperSufSqlThread.get());
        pageHelperPreSqlThread.remove();
        pageHelperSufSqlThread.remove();
    }
}
