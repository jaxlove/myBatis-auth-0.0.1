package com.auth.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author wangdejun
 * @description: PageHelper 插件处理类
 * @date 2020/7/27 18:42
 */
public class PageHelperUtil {

    //分页sql的前面的sql
    private static ThreadLocal<String> pageHelperPreSqlThread = ThreadLocal.withInitial(() -> "");

    //分页sql的后面的sql
    private static ThreadLocal<String> pageHelperSufSqlThread = ThreadLocal.withInitial(() -> "");

    public static void preHandler(StringBuilder sql){
    }

    public static void sufHandler(StringBuilder sql){
        sql.insert(0,pageHelperPreSqlThread.get()).append(pageHelperSufSqlThread.get());
        pageHelperPreSqlThread.remove();
        pageHelperSufSqlThread.remove();
    }

    /**
     * 将pageHelper拼接的分页sql移除，并保存下来
     *
     * @param sql
     * @return
     */
    private static String splitPageHelperSql(String sql) {
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
    public static boolean isPageSelect() {
        return false;
    }

}
