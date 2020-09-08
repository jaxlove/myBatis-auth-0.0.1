package com.auth.util;

import com.auth.common.Contant;
import com.auth.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author wangdejun
 * @description: 单句Sql解析器，忽略子嵌套查询
 * @date 2020/2/3 14:01
 */
public class SelectSqlParser {

    /**
     * sql结束标识
     */
    private static final String END_SIGNAL = "ENDOFSQL";

    /**
     * select 关键字 前面的内容
     */
    private String preSelectSql;

    /**
     * 所有带括号的，都视为子查询，不做处理
     */
    private List<SubSql> subSqlList = new ArrayList<>();

    /**
     * #sub_sql# 倆遍要加空格，否则可能出现 select * from() => select * from#sub_sql#，导致后面无法解析
     */
    private static final String SUB_SQL = Contant.BLANK + "#sub_sql#" + Contant.BLANK;

    /**
     * 原始Sql语句
     */
    protected String originalSql;

    /**
     * 去除子查询的简单sql
     * eg: select * from #sub_sql# where x.y=1
     */
    protected String simpleSql;

    /**
     * Sql语句片段
     */
    protected List<SqlSegment> segments = new ArrayList<>();

    /**
     * 构造函数，传入原始Sql语句，进行劈分。
     *
     * @param sql
     */
    public SelectSqlParser(String sql) {
        //注意sql中，可能包含着myBatis中的各种表达式，不可以转为大写/小写
        this.originalSql = sql;
        //去除多余空格
        simpleSql = sql.trim().replaceAll("\\s+", Contant.BLANK) + Contant.BLANK + END_SIGNAL;
        //去除带有括号的子查询
        setSimpleSql();
        initializeSegments();
        splitSql2Segment();
    }

    /**
     * 初始化segments
     */
    private void initializeSegments() {
        String[] split = this.simpleSql.split("([sS][eE][lL][eE][cC][tT])");
        this.preSelectSql = split[0];
        // ? 正则 代表非贪心模式，按最短匹配
        segments.add(new SqlSegment("(select )(.+?)( from )", "[,]"));
        segments.add(new SqlSegment("( from )(.+?)( where | having | group by | order by | " + END_SIGNAL + ")", "(,| left join | right join | inner join )"));
        segments.add(new SqlSegment("( where | having )(.+?)( group by | order by | " + END_SIGNAL + ")", "( and | or )"));
        segments.add(new SqlSegment("( group by )(.+?)( order by | " + END_SIGNAL + ")", "[,]"));
        segments.add(new SqlSegment("( order by )(.+?)( " + END_SIGNAL + ")", "[,]"));
    }

    public String getSimpleSql() {
        return simpleSql;
    }

    /**
     * 将parsingSql劈分成一个个片段
     */
    private void splitSql2Segment() {
        for (SqlSegment sqlSegment : segments) {
            sqlSegment.parse(simpleSql);
        }
    }

    /**
     * 将所有子查询，用 SUB_SQL 代替，只解析简单的查询sql
     *
     * @return
     */
    public void setSimpleSql() {
        int start = 0;
        int startFlag = 0;
        int endFlag = 0;
        for (int i = 0; i < simpleSql.length(); i++) {
            if (simpleSql.charAt(i) == '(') {
                startFlag++;
                if (startFlag == endFlag + 1) {
                    start = i;
                }
            } else if (simpleSql.charAt(i) == ')') {
                endFlag++;
                if (endFlag == startFlag) {
                    String subSql = simpleSql.substring(start, i + 1);
                    subSqlList.add(new SubSql(subSql));
                    simpleSql = simpleSql.substring(0, start) + SUB_SQL + simpleSql.substring(i + 1);
                    setSimpleSql();
                }
            }
        }
    }

    private String fullSubSql(String sql) {
        for (int i = 0; i < subSqlList.size(); i++) {
            sql = sql.replaceFirst(SUB_SQL, subSqlList.get(i).oldSql);
        }
        return sql;
    }

    /**
     * 得到解析完毕的Sql语句
     *
     * @return
     */
    public String getParsedSql() {
        StringBuffer sb = new StringBuffer(preSelectSql);
        for (SqlSegment sqlSegment : segments) {
            sb.append(sqlSegment.getParsedSqlSegment());
        }
        return fullSubSql(sb.toString());
    }

    public List<SelectColumn> getSelectColumn() {
        List<String> columns = segments.get(0).getBodyPieces();
        List<SelectColumn> selectColumnList = columns.stream().map(t -> new SelectColumn(t)).collect(Collectors.toList());
        return selectColumnList;
    }

    public void setWhere(String whereSql) {
        if (StringUtils.isNotBlank(whereSql)) {
            SqlSegment whereSqlSegment = segments.get(2);
            String parsedSqlSegment = whereSqlSegment.getParsedSqlSegment();
            SqlSegment sqlSegment = new SqlSegment("( where | on | having )(.+)( group by | limit | order by | " + END_SIGNAL + ")", "( and | or )");
            if (StringUtils.isBlank(parsedSqlSegment)) {
                sqlSegment.parse(" where " + whereSql + " " + END_SIGNAL);
            } else {
                sqlSegment.parse(parsedSqlSegment + " and " + "(" + whereSql + ") " + END_SIGNAL);
            }
            segments.set(2, sqlSegment);
        }
    }

    /**
     * 查询的sql 字段中，是否包含 传递的字段
     * 带有 * 算作，或者全部字段，视为 包含
     * todo * 未考虑别名
     *
     * @param columns
     * @return
     */
    public boolean hasColumn(List<Properties> columns) {
        List<SelectSqlParser.SelectColumn> selectColumn = getSelectColumn();
        if (selectColumn != null && !selectColumn.isEmpty()) {
            Optional<SelectColumn> hasAuthColumn = selectColumn.stream().filter(t -> {
                for (Properties property : columns) {
                    if (t.getColumnAlias().indexOf("*") > -1) {
                        return true;
                    }
                    if (t.getColumnAlias().equalsIgnoreCase(property.getProperty("column"))) {
                        return true;
                    }
                }
                return false;
            }).findAny();
            return hasAuthColumn.isPresent();
        }
        return false;
    }


    private class SqlSegment {
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

    private class SelectColumn {
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

    private class SubSql {
        String oldSql;
        String subSql;

        public SubSql(String subSql) {
            this.oldSql = subSql;
            this.subSql = this.oldSql.substring(1, this.oldSql.length() - 1);
        }
    }
}
