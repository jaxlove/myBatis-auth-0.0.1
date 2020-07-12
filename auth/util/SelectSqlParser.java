package com.tydic.auth.util;

import cn.hutool.core.util.StrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author wangdejun
 * @description: 单句Sql解析器，忽略子嵌套查询
 * @date 2020/2/3 14:01
 */
public class SelectSqlParser {


    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String END_SIGNAL = "ENDOFSQL";

    //原sql（不加pageHelper的sql） select的前缀
    private String originSqlPrefix;

    private List<SubSql> subSqlList = new ArrayList<>();

    // #sub_sql# 倆遍要加空格，否则可能出现 select * from() => select * from#sub_sql#，导致后面无法解析
    private static final String SUB_SQL = " #sub_sql# ";

    /**
     * 原始Sql语句
     */
    protected String originalSql;

    protected String formatSql;

    /**
     * Sql语句片段
     */
    protected List<SqlSegment> segments;

    /**
     * 构造函数，传入原始Sql语句，进行劈分。
     *
     * @param originalSql
     */
    public SelectSqlParser(String originalSql) {
        this.originalSql = replaceSubSql(originalSql);
        //sql中，还包含这myBatis中的各种表达式，不能转为小写
//        formatSql = this.originalSql.trim().toLowerCase().replaceAll("\\s{1,}", " ") + " " + END_SIGNAL;
        formatSql = this.originalSql.trim().replaceAll("\\s{1,}", " ") + " " + END_SIGNAL;
        segments = new ArrayList<>();
        initializeSegments();
        splitSql2Segment();
    }

    /**
     * 初始化segments
     */
    protected void initializeSegments() {
        String[] split = this.originalSql.split("([sS][eE][lL][eE][cC][tT])");
        this.originSqlPrefix = split[0];
        // ? 正则 代表非贪心模式，按最短匹配
        segments.add(new SqlSegment("(select )(.+?)( from )", "[,]"));
        segments.add(new SqlSegment("( from )(.+?)( where | having | group by | order by | " + END_SIGNAL + ")", "(,| left join | right join | inner join )"));
        segments.add(new SqlSegment("( where | having )(.+?)( group by | order by | " + END_SIGNAL + ")", "( and | or )"));
        segments.add(new SqlSegment("( group by )(.+?)( order by | " + END_SIGNAL + ")", "[,]"));
        segments.add(new SqlSegment("( order by )(.+?)( " + END_SIGNAL + ")", "[,]"));
    }

    /**
     * 将originalSql劈分成一个个片段
     */
    protected void splitSql2Segment() {
        for (SqlSegment sqlSegment : segments) {
            sqlSegment.parse(formatSql);
        }
    }

    private String replaceSubSql(String sql) {
        int start = 0;
        int startFlag = 0;
        int endFlag = 0;
        for (int i = 0; i < sql.length(); i++) {
            if (sql.charAt(i) == '(') {
                startFlag++;
                if (startFlag == endFlag + 1) {
                    start = i;
                }
            } else if (sql.charAt(i) == ')') {
                endFlag++;
                if (endFlag == startFlag) {
                    String subSql = sql.substring(start, i + 1);
                    subSqlList.add(new SubSql(subSql));
                    sql = sql.substring(0, start) + SUB_SQL + sql.substring(i + 1);
                    sql = replaceSubSql(sql);
                }
            }
        }
        return sql;
    }

    private String fullSubSql(String sql) {
        for (int i = 0; i < subSqlList.size(); i++) {
            sql = sql.replaceFirst(SUB_SQL, subSqlList.get(i).originalSql);
        }
        return sql;
    }

    /**
     * 得到解析完毕的Sql语句
     *
     * @return
     */
    public String getParsedSql() {
//        logger.info("获取解析好的sql");
        StringBuffer sb = new StringBuffer(originSqlPrefix);
        for (SqlSegment sqlSegment : segments) {
            sb.append(sqlSegment.getParsedSqlSegment());
        }
//        logger.info("替换 sub_sql 前语句：{}", sb.toString());
//        logger.info("subList 保存的值：{}", GsonTool.gsonString(subSqlList));
        return fullSubSql(sb.toString());
    }

    public List<SelectColumn> getSelectColumn() {
        List<String> columns = segments.get(0).getBodyPieces();
        List<SelectColumn> selectColumnList = columns.stream().map(t -> new SelectColumn(t)).collect(Collectors.toList());
        return selectColumnList;
    }

    public void setWhere(String whereSql) {
        SqlSegment whereSqlSegment = segments.get(2);
        String parsedSqlSegment = whereSqlSegment.getParsedSqlSegment();
        SqlSegment sqlSegment = new SqlSegment("( where | on | having )(.+)( group by | order by | " + END_SIGNAL + ")", "( and | or )");
        if (StrUtil.isBlank(parsedSqlSegment)) {
            sqlSegment.parse(" where " + whereSql + " " + END_SIGNAL);
        } else {
            sqlSegment.parse(parsedSqlSegment + " and " + "(" + whereSql + ") " + END_SIGNAL);
        }
        segments.set(2, sqlSegment);
    }


    public class SqlSegment {
        /**
         * Sql语句片段开头部分
         */
        private String start;
        /**
         * Sql语句片段中间部分
         */
        private String body;
        /**
         * Sql语句片段结束部分
         */
        private String end;
        /**
         * 用于分割中间部分的正则表达式
         */
        private String bodySplitPattern;
        /**
         * 表示片段的正则表达式
         */
        private String segmentRegExp;
        /**
         * 分割后的Body小片段
         */
        private List<String> bodyPieces;

        /**
         * 构造函数
         *
         * @param segmentRegExp    表示这个Sql片段的正则表达式
         * @param bodySplitPattern 用于分割body的正则表达式
         */
        public SqlSegment(String segmentRegExp, String bodySplitPattern) {
            start = "";
            body = "";
            end = "";
            this.segmentRegExp = segmentRegExp;
            this.bodySplitPattern = bodySplitPattern;
            this.bodyPieces = new ArrayList<>();

        }

        /**
         * 从sql中查找符合segmentRegExp的部分，并赋值到start,body,end等三个属性中
         *
         * @param sql
         */
        public void parse(String sql) {
            Pattern pattern = Pattern.compile(segmentRegExp, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(sql);
            while (matcher.find()) {
                start = matcher.group(1);
                body = matcher.group(2);
                end = matcher.group(3);
                parseBody();
                return;
            }
        }

        /**
         * 解析body部分
         */
        private void parseBody() {

            bodyPieces = new ArrayList<>();
            Pattern p = Pattern.compile(bodySplitPattern, Pattern.CASE_INSENSITIVE);
            // 先清除掉前后空格
            body = body.trim();
            Matcher m = p.matcher(body);
            StringBuffer sb = new StringBuffer();
            boolean result = m.find();
            while (result) {
                m.appendReplacement(sb, m.group(0));
                result = m.find();
            }
            m.appendTail(sb);
            String[] arr = sb.toString().split(bodySplitPattern);
            int arrLength = arr.length;
            for (int i = 0; i < arrLength; i++) {
                bodyPieces.add(arr[i]);
            }
        }

        /**
         * 取得解析好的Sql片段
         *
         * @return
         */
        public String getParsedSqlSegment() {
            StringBuffer sb = new StringBuffer();
            sb.append(start + " " + body + " ");
            return sb.toString();
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }

        public String getEnd() {
            return end;
        }

        public void setEnd(String end) {
            this.end = end;
        }

        public String getStart() {
            return start;
        }


        public void setStart(String start) {
            this.start = start;
        }

        public List<String> getBodyPieces() {
            return bodyPieces;
        }
    }

    public class SelectColumn {
        private String orignal;
        private String columnName;
        private String columnAlias;

        public SelectColumn(String orignal) {
            this.orignal = orignal;
            String[] split = orignal.trim().split("\\s");
            try {
                this.columnName = split[0].split("\\.")[split[0].split("\\.").length - 1];
                if (split.length > 1) {
                    this.columnAlias = split[split.length - 1];
                } else {
                    this.columnAlias = this.columnName;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public String getOrignal() {
            return orignal;
        }

        public String getColumnName() {
            return columnName;
        }

        public String getColumnAlias() {
            return columnAlias;
        }
    }

    public class SubSql {
        String originalSql;
        String subSql;

        public SubSql(String originalSql) {
            this.originalSql = originalSql;
            this.subSql = this.originalSql.substring(1, this.originalSql.length() - 1);
        }
    }


}
