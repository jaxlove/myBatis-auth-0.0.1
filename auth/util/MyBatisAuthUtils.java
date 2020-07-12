package com.tydic.auth.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Sets;
import com.tydic.util.PropertityUtil;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectItem;
import org.apache.commons.collections4.CollectionUtils;
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
        if (StrUtil.isBlank(sql) || sql.contains(AUTH_TABLE_ALIAS)) {
            return sql;
        }
        //超管，设置为全部数据权限
        if (curSearchInfo.isSuperAdmin()) {
            curSearchInfo.setCityIdDataScope(new HashSet<>(Arrays.asList(0)));
        }
        //无数据权限，返回空
        if (CollUtil.isEmpty(curSearchInfo.getCityIdDataScope())) {
            return EMPTY_SQL;
        }
        String authSql;
        //权限字段
        List<Pair<String, Class>> authColumn = CollectionUtils.isEmpty(curSearchInfo.getAuthColumn()) ? Arrays.asList(new Pair<>("CITY_ID", Integer.class)) : curSearchInfo.getAuthColumn();
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
    private static boolean includeAuthColumn(SelectSqlParser selectSqlParser, List<Pair<String, Class>> authColumns) {
        List<SelectSqlParser.SelectColumn> selectColumn = selectSqlParser.getSelectColumn();
        if (CollUtil.isNotEmpty(selectColumn)) {
            Optional<SelectSqlParser.SelectColumn> hasAuthColumn = selectColumn.stream().filter(t -> {
                for (Pair<String, Class> pair : authColumns) {
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
        return "select "+ AUTH_TABLE_ALIAS +".* from (" + sql + ") "+ AUTH_TABLE_ALIAS;
    }

    /**
     * 向plainSelect中设置权限信息
     *
     * @param selectSqlParser
     * @param authSql
     */
    private static String setAuthWhere(SelectSqlParser selectSqlParser, String authSql) {
        //拼接where条件
        if (StrUtil.isBlank(authSql)) {
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
        if (CollUtil.isEmpty(curSearchInfo.getCityIdDataScope())) {
            return "(0 = 1)";
        }
        //权限字段
        List<Pair<String, Class>> authColumnNames = CollectionUtils.isEmpty(curSearchInfo.getAuthColumn()) ? Arrays.asList(new Pair<>("CITY_ID", Integer.class)) : curSearchInfo.getAuthColumn();
        //判断当前sql查询出来的字段，是否包含任一权限字段或者x.*,如果包含，则将原始sql包在一起，外部加上${AUTH_ALIAS}再加上权限条件,如果没有，则直接加上权限条件，再加上${AUTH_ALIAS}
        AuthColumnType authColumnType = "1".equals(curSearchInfo.getAuthColumnType()) ? AuthColumnType.AND : AuthColumnType.OR;
        Set<Integer> authValue = curSearchInfo.getCityIdDataScope();
        tableNameAlias = StrUtil.isBlank(tableNameAlias) ? curSearchInfo.getAuthTableAlias() : tableNameAlias;
        //获取所有的权限的条件
        List<String> authAuthList = new ArrayList<>();
        for (Pair<String, Class> authColumn : authColumnNames) {
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
                    singleColumnAuth += CollUtil.join(authValue, ",");
                } else if (authColumn.getValue() == String.class) {
                    List<String> authValueStrList = authValue.stream().map(t -> "'" + t + "'").collect(Collectors.toList());
                    singleColumnAuth += CollUtil.join(authValueStrList, ",");
                } else {

                }
                singleColumnAuth += ")";
            }
            if (StrUtil.isNotBlank(singleColumnAuth)) {
                authAuthList.add(singleColumnAuth);
            }
        }
        //返回where条件
        if (CollUtil.isEmpty(authAuthList)) {
            return null;
        }
        if (authAuthList.size() == 1) {
            return "(" + authAuthList.get(0) + ")";
        } else {
            return "(" + CollUtil.join(authAuthList, " " + authColumnType.operator + " ") + ")";
        }
    }

    private static String getAliasAndColumnName(String alias, Pair<String, Class> columnInfo) {
        return StrUtil.isBlank(alias) ? columnInfo.getKey() : alias + "." + columnInfo.getKey();
    }

    enum AuthColumnType {

        AND("and"), OR("or");
        private String operator;

        AuthColumnType(String operator) {
            this.operator = operator;
        }
    }

    public static void main(String[] args) {
        getAuthSqlTest();
//        getNormalTest();
//        pageHelperTest();
    }

    private static void pageHelperTest() {
        String s = MyBatisAuthUtils.splitPageHelperSql("select *\n" +
                "from (select tmp_page.*, rownum row_id\n" +
                "      from (select n.*\n" +
                "            from (\n" +
                "                     SELECT c.CELL_NAME,\n" +
                "                            c.ENB_ID,\n" +
                "                            c.CELL_ID,\n" +
                "                            c.PROJECT_ID,\n" +
                "                            c.LONGITUDE,\n" +
                "                            c.LATITUDE,\n" +
                "                            c.LOCAL_CELLID,\n" +
                "                            c.ECI,\n" +
                "                            c.CELL_PINYIN_NAME,\n" +
                "                            c.CELL_ADDRESS,\n" +
                "                            c.ENB_NAME,\n" +
                "                            c.ENB_ADDRESS_CODE,\n" +
                "                            c.ENB_ADDRESS_NAME,\n" +
                "                            c.WORK_STATUS,\n" +
                "                            c.IS_SAME_PCI_CELL,\n" +
                "                            c.SAME_PCI_BID,\n" +
                "                            c.IS_WORKPOINT_CELL,\n" +
                "                            c.WORKPOINT_CELL_CODE,\n" +
                "                            c.IS_CONTACT_CELL,\n" +
                "                            c.CONTACT_CELL_ID,\n" +
                "                            c.RRU,\n" +
                "                            c.ID,\n" +
                "                            c.RRU_PORT,\n" +
                "                            c.VENDOR,\n" +
                "                            c.RRU_MODEL,\n" +
                "                            c.RRU_SHORTNAME,\n" +
                "                            c.PROPERTY_RIGHTS,\n" +
                "                            c.IS_SHARE_TYPE,\n" +
                "                            c.ENB_TYPE,\n" +
                "                            c.CELL_TYPE,\n" +
                "                            c.BUSINESS_TYPE,\n" +
                "                            c.COVERAGE_AREA_TYPE,\n" +
                "                            c.COVERAGE_ROAD_TYPE,\n" +
                "                            c.COVERAGE_IMPORTANT_PLACE_TYPE,\n" +
                "                            c.COVERAGE_UNIVERSITY_TYPE,\n" +
                "                            c.COVERAGE_CRH_TYPE,\n" +
                "                            c.COVERAGE_HIGHWAY_TYPE,\n" +
                "                            c.COVERAGE_INTENSIVE_HOUSE_TYPE,\n" +
                "                            c.COVERAGE_BUSINESS_AREA_TYPE,\n" +
                "                            c.COVERAGE_METRO_TYPE,\n" +
                "                            c.BORDER_TYPE,\n" +
                "                            c.OTHER_PROVINCE_NAME,\n" +
                "                            c.OTHER_CITY_NAME,\n" +
                "                            c.CITY_id,\n" +
                "                            c.IS_SCHOOL_DISCOUNT_CELL,\n" +
                "                            c.PHY_CELL_ID,\n" +
                "                            c.TAC,\n" +
                "                            c.BAND_DIRECTIVE,\n" +
                "                            c.UL_EAR_FCN,\n" +
                "                            c.DL_EAR_FCN,\n" +
                "                            c.UL_BAND_WIDTH,\n" +
                "                            c.DL_BAND_WIDTH,\n" +
                "                            c.CELL_POWER,\n" +
                "                            c.HIGH_SPEED_PROPERTY,\n" +
                "                            c.CELL_RADIUS,\n" +
                "                            c.CARRIER_MERGE_TYPE,\n" +
                "                            c.CA_RRU_MODEL,\n" +
                "                            c.CA_RRU_FREQ_BAND,\n" +
                "                            c.DIRECT_ANGLE,\n" +
                "                            c.PRE_ELEC_DOWNDIP_ANGLE,\n" +
                "                            c.TRAN_ELEC_DOWNDIP_ANGLE,\n" +
                "                            c.ELEC_INCLINATION_ANGLE,\n" +
                "                            c.MECH_INCLINATION_ANGLE,\n" +
                "                            c.ALL_INCLINATION_ANGLE,\n" +
                "                            c.ANTENNA_FLOOR_NUM,\n" +
                "                            c.ANTENNA_HIGH,\n" +
                "                            c.SECTOR_ALTITUDE,\n" +
                "                            c.ANTENNA_DIRECT_TYPE,\n" +
                "                            c.MULTIPLE_FREQ_ANTENNA_TYPE,\n" +
                "                            c.ANTENNA_PLUS,\n" +
                "                            c.SWEEP_ANGLE,\n" +
                "                            c.PLUMB_ANGLE,\n" +
                "                            c.IS_BEAUTIFY_ANTENNA,\n" +
                "                            c.BEAUTIFY_ANTENNA_TYPE,\n" +
                "                            c.ANTENNA_VENDOR,\n" +
                "                            c.ANTENNA_MODEL,\n" +
                "                            c.ANTENNA_TX_NUM,\n" +
                "                            c.ANTENNA_RX_NUM,\n" +
                "                            c.SECTOR_ROOM_SHARE_INFO,\n" +
                "                            c.SECTOR_ROOM_RIGHT,\n" +
                "                            c.TOWER_MAST_SHARE_INFO,\n" +
                "                            c.TOWER_MAST_RIGHT,\n" +
                "                            c.TOWER_MAST_TYPE,\n" +
                "                            c.DATE_OF_ACCESS,\n" +
                "                            c.UPDATE_TIME,\n" +
                "                            c.UPDATE_USER_NAME,\n" +
                "                            c.REMARK,\n" +
                "                            c.PROVINCE_NAME,\n" +
                "                            c.CITY_NAME,\n" +
                "                            c.DISTRICT_NAME,\n" +
                "                            c.TOWN_NAME,\n" +
                "                            c.AREA_ID,\n" +
                "                            c.AREA_NAME,\n" +
                "                            c.GRID_ID,\n" +
                "                            c.GRID_NAME,\n" +
                "                            c.RESPONSIBILITY_AREA_NAME,\n" +
                "                            c.PROVINCE_ID,\n" +
                "                            c.CITY_ID,\n" +
                "                            c.DISTRICT_ID,\n" +
                "                            c.NET_DATE,\n" +
                "                            c.INSERT_TIME,\n" +
                "                            c.INT_ID,\n" +
                "                            c.UPDATE_USER,\n" +
                "                            c.RRU_ID,\n" +
                "                            c.IS_SUPPORT_AB_PORT,\n" +
                "                            c.TOWN_ID,\n" +
                "                            c.GRID_TYPE,\n" +
                "                            c.ANTENNA_CODE,\n" +
                "                            c.SECTORID,\n" +
                "                            c.C_NET_SEARCH_KEY_VALUES,\n" +
                "                            c.IS_BBU_POOL,\n" +
                "                            c.SYSTEM_TYPE,\n" +
                "                            c.IS_CHANGABLE,\n" +
                "                            c.IS_FREQUENCY_STANDBY,\n" +
                "                            c.PHY_ENB_NAME,\n" +
                "                            r.rru_location             as rruLocation,\n" +
                "                            r.rru_coverage             as rruCoverage,\n" +
                "                            l.data_status              as r_data_status,\n" +
                "                            l.antenna_output_type      as antenna_output_type,\n" +
                "                            l.l_cell_repeater_near_num as l_cell_repeater_near_num,\n" +
                "                            l.l_cell_repeater_far_num  as l_cell_repeater_far_num,\n" +
                "                            l.source_ventor            as sourceVentor,\n" +
                "                            l.source_type              as sourceType,\n" +
                "                            l.indoor_type              as lIndoorType,\n" +
                "                            l.rru_key                  as rruKey\n" +
                "                     FROM COL_INDOOR_WAREHOUSE_L l\n" +
                "                              LEFT JOIN COL_LTE_CELL c ON c.CELL_ID = l.L_CELL_ID\n" +
                "                         AND c.ENB_ID = l.L_ENB_ID\n" +
                "                         AND nvl(TO_CHAR(c.SAME_PCI_BID), -9999) = nvl(l.L_PCI_NO, -9999)\n" +
                "                         AND c.workpoint_cell_code = l.l_powersplit_cell_no\n" +
                "                              LEFT JOIN COL_LTE_RRU r on c.rru = r.rru_identify\n" +
                "                     WHERE l.INDOOR_CODE = '11'\n" +
                "                 ) n) tmp_page\n" +
                "      where rownum <= 10)\n" +
                "where row_id > 1");
        System.out.println(s);
        System.out.println("==========================");
        System.out.println(MyBatisAuthUtils.pageHelperPreSqlThread.get() + s + MyBatisAuthUtils.pageHelperSufSqlThread.get());
//        System.out.println("asdf)asdf)".replaceAll("\\)",""));
    }


    private static void getAuthSqlTest() {
        String sql = "SELECT c.data_status,\n" +
                "       c.CELL_NAME,\n" +
                "       c.ENB_ID,\n" +
                "       c.CELL_ID,\n" +
                "       c.PROJECT_ID,\n" +
                "       c.LONGITUDE,\n" +
                "       c.LATITUDE,\n" +
                "       c.LOCAL_CELLID,\n" +
                "       c.ECI,\n" +
                "       c.CELL_PINYIN_NAME,\n" +
                "       c.CELL_ADDRESS,\n" +
                "       c.ENB_NAME,\n" +
                "       c.ENB_ADDRESS_CODE,\n" +
                "       c.ENB_ADDRESS_NAME,\n" +
                "       c.WORK_STATUS,\n" +
                "       c.IS_SAME_PCI_CELL,\n" +
                "       c.SAME_PCI_BID,\n" +
                "       c.IS_WORKPOINT_CELL,\n" +
                "       c.WORKPOINT_CELL_CODE,\n" +
                "       c.IS_CONTACT_CELL,\n" +
                "       c.CONTACT_CELL_ID,\n" +
                "       c.RRU,\n" +
                "       c.ID,\n" +
                "       c.RRU_PORT,\n" +
                "       c.VENDOR,\n" +
                "       c.RRU_MODEL,\n" +
                "       c.RRU_SHORTNAME,\n" +
                "       c.PROPERTY_RIGHTS,\n" +
                "       c.IS_SHARE_TYPE,\n" +
                "       c.ENB_TYPE,\n" +
                "       c.CELL_TYPE,\n" +
                "       c.BUSINESS_TYPE,\n" +
                "       c.COVERAGE_AREA_TYPE,\n" +
                "       c.COVERAGE_ROAD_TYPE,\n" +
                "       c.COVERAGE_IMPORTANT_PLACE_TYPE,\n" +
                "       c.COVERAGE_UNIVERSITY_TYPE,\n" +
                "       c.COVERAGE_CRH_TYPE,\n" +
                "       c.COVERAGE_HIGHWAY_TYPE,\n" +
                "       c.COVERAGE_INTENSIVE_HOUSE_TYPE,\n" +
                "       c.COVERAGE_BUSINESS_AREA_TYPE,\n" +
                "       c.COVERAGE_METRO_TYPE,\n" +
                "       c.BORDER_TYPE,\n" +
                "       c.OTHER_PROVINCE_NAME,\n" +
                "       c.OTHER_CITY_NAME,\n" +
                "       c.IS_SCHOOL_DISCOUNT_CELL,\n" +
                "       c.PHY_CELL_ID,\n" +
                "       c.TAC,\n" +
                "       c.BAND_DIRECTIVE,\n" +
                "       c.UL_EAR_FCN,\n" +
                "       c.DL_EAR_FCN,\n" +
                "       c.UL_BAND_WIDTH,\n" +
                "       c.DL_BAND_WIDTH,\n" +
                "       c.CELL_POWER,\n" +
                "       c.HIGH_SPEED_PROPERTY,\n" +
                "       c.CELL_RADIUS,\n" +
                "       c.CARRIER_MERGE_TYPE,\n" +
                "       c.CA_RRU_MODEL,\n" +
                "       c.CA_RRU_FREQ_BAND,\n" +
                "       c.DIRECT_ANGLE,\n" +
                "       c.PRE_ELEC_DOWNDIP_ANGLE,\n" +
                "       c.TRAN_ELEC_DOWNDIP_ANGLE,\n" +
                "       c.ELEC_INCLINATION_ANGLE,\n" +
                "       c.MECH_INCLINATION_ANGLE,\n" +
                "       c.ALL_INCLINATION_ANGLE,\n" +
                "       c.ANTENNA_FLOOR_NUM,\n" +
                "       c.ANTENNA_HIGH,\n" +
                "       c.SECTOR_ALTITUDE,\n" +
                "       c.ANTENNA_DIRECT_TYPE,\n" +
                "       c.MULTIPLE_FREQ_ANTENNA_TYPE,\n" +
                "       c.ANTENNA_PLUS,\n" +
                "       c.SWEEP_ANGLE,\n" +
                "       c.PLUMB_ANGLE,\n" +
                "       c.IS_BEAUTIFY_ANTENNA,\n" +
                "       c.BEAUTIFY_ANTENNA_TYPE,\n" +
                "       c.ANTENNA_VENDOR,\n" +
                "       c.ANTENNA_MODEL,\n" +
                "       c.ANTENNA_TX_NUM,\n" +
                "       c.ANTENNA_RX_NUM,\n" +
                "       c.SECTOR_ROOM_SHARE_INFO,\n" +
                "       c.SECTOR_ROOM_RIGHT,\n" +
                "       c.TOWER_MAST_SHARE_INFO,\n" +
                "       c.TOWER_MAST_RIGHT,\n" +
                "       c.TOWER_MAST_TYPE,\n" +
                "       c.DATE_OF_ACCESS,\n" +
                "       c.UPDATE_TIME,\n" +
                "       c.UPDATE_USER_NAME,\n" +
                "       c.REMARK,\n" +
                "       c.PROVINCE_NAME,\n" +
                "       c.CITY_NAME,\n" +
                "       c.DISTRICT_NAME,\n" +
                "       c.TOWN_NAME,\n" +
                "       c.AREA_ID,\n" +
                "       c.AREA_NAME,\n" +
                "       c.GRID_ID,\n" +
                "       c.GRID_NAME,\n" +
                "       c.RESPONSIBILITY_AREA_NAME,\n" +
                "       c.PROVINCE_ID,\n" +
                "       c.CITY_ID,\n" +
                "       c.DISTRICT_ID,\n" +
                "       c.NET_DATE,\n" +
                "       c.INSERT_TIME,\n" +
                "       c.INT_ID,\n" +
                "       c.UPDATE_USER,\n" +
                "       c.RRU_ID,\n" +
                "       c.IS_SUPPORT_AB_PORT,\n" +
                "       c.TOWN_ID,\n" +
                "       c.GRID_TYPE,\n" +
                "       c.ANTENNA_CODE,\n" +
                "       c.SECTORID,\n" +
                "       c.C_NET_SEARCH_KEY_VALUES,\n" +
                "       c.IS_BBU_POOL,\n" +
                "       c.SYSTEM_TYPE,\n" +
                "       c.IS_CHANGABLE,\n" +
                "       c.IS_FREQUENCY_STANDBY,\n" +
                "       c.PHY_ENB_NAME,\n" +
                "       r.rru_location             as rruLocation,\n" +
                "       r.rru_coverage             as rruCoverage,\n" +
                "       l.data_status              as r_data_status,\n" +
                "       l.antenna_output_type      as antenna_output_type,\n" +
                "       l.l_cell_repeater_near_num as l_cell_repeater_near_num,\n" +
                "       l.l_cell_repeater_far_num  as l_cell_repeater_far_num,\n" +
                "       l.source_ventor            as sourceVentor,\n" +
                "       l.source_type              as sourceType,\n" +
                "       l.indoor_type              as lIndoorType,\n" +
                "       l.rru_key                  as rruKey\n" +
                "FROM COL_INDOOR_WAREHOUSE_L l\n" +
                "         LEFT JOIN COL_LTE_CELL c ON c.CELL_ID = l.L_CELL_ID\n" +
                "    AND c.ENB_ID = l.L_ENB_ID\n" +
                "    AND nvl(TO_CHAR(c.SAME_PCI_BID), -9999) = nvl(l.L_PCI_NO, -9999)\n" +
                "    AND c.workpoint_cell_code = l.l_powersplit_cell_no\n" +
                "         LEFT JOIN COL_LTE_RRU r on c.rru = r.rru_identify\n" +
                "\n";
        try {
            AuthQueryInfo authQueryInfo = new AuthQueryInfo();
            authQueryInfo.setAutoAppendAuth(true);
//            authQueryInfo.setAuthColumnType("1");
            authQueryInfo.setAuthColumn(Arrays.asList(new Pair<>("CITY_ID", Integer.class)));
            authQueryInfo.setCityIdDataScope(Sets.newHashSet(Arrays.asList(0, 2)));
            AuthHelper.setCurSearch(authQueryInfo);
            long start = System.currentTimeMillis();
            String authSql = MyBatisAuthUtils.getAuthSql(sql);
            System.out.println("sql解析耗时========" + (System.currentTimeMillis() - start));
            String authSql2 = MyBatisAuthUtils.getAuthSql(sql);
            System.out.println("sql解析耗时========" + (System.currentTimeMillis() - start));
            System.out.println(authSql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void getNormalTest() {
        String sql = "select * from (with x as (select 1 from dual) SELECT  v as x,3,count(1) " +
                "FROM ((select * from xx wHere a=1)) where 1=2 and x = #{xx} order by x)";
        try {
            Select select = (Select) CCJSqlParserUtil.parse(sql);
            SelectBody selectBody = select.getSelectBody();
            PlainSelect plainSelect = (PlainSelect) selectBody;
            List<SelectItem> selectItems = plainSelect.getSelectItems();
            Expression orignWhere = plainSelect.getWhere();
            Column column = new Column(new Table("table"), "columnName");
            LongValue longValue = new LongValue(11);
            Function function = new Function();
            function.setName("nvl");
            function.setParameters(new ExpressionList(Arrays.asList(column, new LongValue(-999))));

            EqualsTo equalsTo = new EqualsTo();
            equalsTo.setLeftExpression(function);
            equalsTo.setRightExpression(function);


            ExpressionList expressionList = new ExpressionList(Arrays.asList(longValue));
            InExpression inExpression = new InExpression(column, expressionList);
            if (orignWhere != null) {
                AndExpression andExpression = new AndExpression(orignWhere, inExpression);
                plainSelect.setWhere(andExpression);
            } else {
                plainSelect.setWhere(inExpression);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
