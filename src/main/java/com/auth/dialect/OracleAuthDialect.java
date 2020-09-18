package com.auth.dialect;

import com.auth.exception.AuthException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author wangdejun
 * @description: oracle pagehelper处理类
 * @date 2020/9/4 11:12
 */
public class OracleAuthDialect implements DialectHandler {

    //分页sql的前面的sql
    private static ThreadLocal<String> pageHelperPreSqlThread = ThreadLocal.withInitial(() -> "");

    //分页sql的后面的sql
    private static ThreadLocal<String> pageHelperSufSqlThread = ThreadLocal.withInitial(() -> "");

    @Override
    public String removePagehelperSelectSql(String sql) {
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

    //fix me 未实现代码
    @Override
    public String removePagehelperCountSql(String sql) throws AuthException {
        throw new AuthException("unimplemented method");
    }

    @Override
    public String selectSufHandler(String sql) {
        sql = pageHelperPreSqlThread.get() + sql + pageHelperSufSqlThread.get();
        return sql;
    }

    @Override
    public void clear() {
        pageHelperPreSqlThread.remove();
        pageHelperSufSqlThread.remove();
    }

    @Override
    public String getEmptySql() {
        return "select 0 from dual where 1=0";
    }
}
