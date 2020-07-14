package com.auth.util;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author wangdejun
 * @description: mybaits权限工具类
 * @date 2019/9/20 10:35
 */
public class MyBatisAuthUtils {

    private static Logger logger = LoggerFactory.getLogger(MyBatisAuthUtils.class);

    private static final String EMPTY_SQL = "select 1 from dual where 0=1";

    private static final String AUTH_TABLE_ALIAS = "AUTH_TABLE_ALIAS";

    //分页sql的前面的sql
    private static ThreadLocal<String> pageHelperPreSqlThread = ThreadLocal.withInitial(() -> "");

    //分页sql的后面的sql
    private static ThreadLocal<String> pageHelperSufSqlThread = ThreadLocal.withInitial(() -> "");


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
        if (StringUtils.isBlank(sql) || sql.contains(AUTH_TABLE_ALIAS)) {
            return sql;
        }
        //超管，设置为全部数据权限
        if (curSearchInfo.isSuperAdmin()) {
            curSearchInfo.setCityIdDataScope(new HashSet(Arrays.asList(0)));
        }
        //无数据权限，返回空
        if (curSearchInfo.getCityIdDataScope() == null || curSearchInfo.getCityIdDataScope().isEmpty()) {
            return EMPTY_SQL;
        }
        String authSql;
        //权限字段
        List<Properties> authColumn = CollectionUtils.isEmpty(curSearchInfo.getAuthColumn()) ? Arrays.asList(new Pair<>("CITY_ID", Integer.class)) : curSearchInfo.getAuthColumn();
        if (isPageHelperSql(sql)) {
            sql = splitPageHelperSql(sql);
        }
        SelectSqlParser selectSqlParser = new SelectSqlParser(sql);
        //获取权限where条件
        String authSqlWhere = null;
        if (includeAuthColumn(selectSqlParser, authColumn)) {
            authSqlWhere = getAuthSqlWhere(AUTH_TABLE_ALIAS);
            //select ${AUTH_ALIAS}.* from ( sql ) ${AUTH_ALIAS} where authSql
            authSql = appendOutSideAuth(sql);
            selectSqlParser = new SelectSqlParser(authSql);
            authSql = setAuthWhere(selectSqlParser, authSqlWhere);
        } else {
            authSqlWhere = getAuthSqlWhere(null);
            // select ${AUTH_ALIAS}.* from ( sql authSql )${AUTH_ALIAS}
            authSql = setAuthWhere(selectSqlParser, authSqlWhere);
            authSql = appendOutSideAuth(authSql);
        }
        authSql = pageHelperPreSqlThread.get() + authSql + pageHelperSufSqlThread.get();
        //本次掉调用结束，移除sql信息
        pageHelperPreSqlThread.remove();
        pageHelperSufSqlThread.remove();
        return authSql;
    }

    /**
     * 是否sql查询的字段中，包含权限字段，存在a.* 或者 * ，视为包含
     *
     * @param selectSqlParser
     * @param authColumns
     * @return
     */
    private static boolean includeAuthColumn(SelectSqlParser selectSqlParser, List<Properties> authColumns) {
        List<SelectSqlParser.SelectColumn> selectColumn = selectSqlParser.getSelectColumn();
        if (CollectionUtils.isNotEmpty(selectColumn)) {
            Optional<SelectSqlParser.SelectColumn> hasAuthColumn = selectColumn.stream().filter(t -> {
                for (Properties pair : authColumns) {
                    if (t.getColumnAlias().indexOf("*") > -1) {
                        return true;
                    }
                    if (t.getColumnAlias().equalsIgnoreCase(pair.getKey())) {
                        return true;
                    }
                }
                return false;
            }).findAny();
            return hasAuthColumn.isPresent();
        }
        return false;
    }

    /**
     * 是否为PageHelper的sql
     *
     * @param sql
     * @return
     */
    private static boolean isPageHelperSql(String sql) {
        if (sql.toLowerCase().indexOf("tmp_page") > -1) {
            return true;
        }
        return false;
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
     * 在原始sql外层加上auth
     *
     * @param sql
     * @return
     */
    private static String appendOutSideAuth(String sql) {
        return "select " + AUTH_TABLE_ALIAS + ".* from (" + sql + ") " + AUTH_TABLE_ALIAS;
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

    /**
     * 获取权限sql where条件部分
     * null 不带权限，或 全部权限
     *
     * @return
     */
    public static String getAuthSqlWhere(String tableNameAlias) {
        AuthQueryInfo curSearchInfo = AuthHelper.getCurSearchInfo();
        //权限信息为空，权限查询为空或者false,返回null
        if (curSearchInfo == null || curSearchInfo.getAuthQuery() == null || !curSearchInfo.getAuthQuery()) {
            return null;
        }
        //超管，设置为全部数据权限
        if (curSearchInfo.isSuperAdmin()) {
            curSearchInfo.setCityIdDataScope(new HashSet<>(Arrays.asList(0)));
        }
        //无数据权限，返回空
        if (CollectionUtils.isEmpty(curSearchInfo.getCityIdDataScope())) {
            return "(0 = 1)";
        }
        //权限字段
        List<Properties> authColumnNames = CollectionUtils.isEmpty(curSearchInfo.getAuthColumn()) ? Arrays.asList(new Pair<>("CITY_ID", Integer.class)) : curSearchInfo.getAuthColumn();
        //判断当前sql查询出来的字段，是否包含任一权限字段或者x.*,如果包含，则将原始sql包在一起，外部加上${AUTH_ALIAS}再加上权限条件,如果没有，则直接加上权限条件，再加上${AUTH_ALIAS}
        AuthColumnType authColumnType = "1".equals(curSearchInfo.getAuthColumnType()) ? AuthColumnType.AND : AuthColumnType.OR;
        Set<Integer> authValue = curSearchInfo.getCityIdDataScope();
        tableNameAlias = StringUtils.isBlank(tableNameAlias) ? curSearchInfo.getAuthTableAlias() : tableNameAlias;
        //获取所有的权限的条件
        List<String> authAuthList = new ArrayList<>();
        for (Properties authColumn : authColumnNames) {
            String singleColumnAuth = null;
            if (authValue.contains(0)) {
//                 如果，true，即使权限为所有，也进行权限字段拼接 eg：nvl(a.city_id,-999999) = nvl(a.city_id,-999999)
                Boolean authColumnCheck = PropertityUtil.getProperty("authColumnCheck", Boolean.class);
                if (authColumnCheck != null && authColumnCheck) {
                    singleColumnAuth = " nvl(" + getAliasAndColumnName(tableNameAlias, authColumn) + ",-999999) = nvl(" + getAliasAndColumnName(tableNameAlias, authColumn) + ",-999999)";
                }
            } else {
                singleColumnAuth = getAliasAndColumnName(tableNameAlias, authColumn) + " in (";
                if (Number.class.isAssignableFrom(authColumn.getValue())) {
                    singleColumnAuth += CollectionUtils.join(authValue, ",");
                } else if (authColumn.getValue() == String.class) {
                    List<String> authValueStrList = authValue.stream().map(t -> "'" + t + "'").collect(Collectors.toList());
                    singleColumnAuth += CollectionUtils.join(authValueStrList, ",");
                } else {

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
            return "(" + CollectionUtils.join(authAuthList, " " + authColumnType.operator + " ") + ")";
        }
    }

    private static String getAliasAndColumnName(String alias, Properties columnInfo) {
        return StringUtils.isBlank(alias) ? columnInfo.getKey() : alias + "." + columnInfo.getKey();
    }

    enum AuthColumnType {

        AND("and"), OR("or");
        private String operator;

        AuthColumnType(String operator) {
            this.operator = operator;
        }
    }


}
