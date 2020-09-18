package com.auth.dialect;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wangdejun
 * @description: pagehelper 数据处理
 * @date 2020/9/4 11:10
 */
public class DialectUtil {

    private static Logger logger = LoggerFactory.getLogger(DialectUtil.class);
    private static Map<String, Class<? extends DialectHandler>> dialectAliasMap = new HashMap<>();
    private static Map<String, DialectHandler> dialectInstanceCache = new HashMap<>();

    public static void registerDialectAlias(String alias, Class<? extends DialectHandler> dialectClass) {
        dialectAliasMap.put(alias, dialectClass);
    }

    static {
        //注册别名
        registerDialectAlias("hsqldb", HsqldbAuthDialect.class);
        registerDialectAlias("h2", HsqldbAuthDialect.class);
        registerDialectAlias("postgresql", HsqldbAuthDialect.class);
        registerDialectAlias("phoenix", HsqldbAuthDialect.class);

        registerDialectAlias("oracle", OracleAuthDialect.class);
        //达梦数据库,https://github.com/mybatis-book/book/issues/43
        registerDialectAlias("dm", OracleAuthDialect.class);
        //阿里云PPAS数据库,https://github.com/pagehelper/Mybatis-PageHelper/issues/281
        registerDialectAlias("edb", OracleAuthDialect.class);
//        registerDialectAlias("hsqldb", HsqldbDialect.class);
//        registerDialectAlias("h2", HsqldbDialect.class);

//        registerDialectAlias("phoenix", HsqldbDialect.class);
//
//        registerDialectAlias("mysql", MySqlDialect.class);
//        registerDialectAlias("mariadb", MySqlDialect.class);
//        registerDialectAlias("sqlite", MySqlDialect.class);
//
//        registerDialectAlias("herddb", HerdDBDialect.class);

//        registerDialectAlias("db2", Db2Dialect.class);
//        registerDialectAlias("informix", InformixDialect.class);
//        //解决 informix-sqli #129，仍然保留上面的
//        registerDialectAlias("informix-sqli", InformixDialect.class);
//
//        registerDialectAlias("sqlserver", SqlServerDialect.class);
//        registerDialectAlias("sqlserver2012", SqlServer2012Dialect.class);
//
//        registerDialectAlias("derby", SqlServer2012Dialect.class);
//        //达梦数据库,https://github.com/mybatis-book/book/issues/43
//        registerDialectAlias("dm", OracleDialect.class);
//        //阿里云PPAS数据库,https://github.com/pagehelper/Mybatis-PageHelper/issues/281
//        registerDialectAlias("edb", OracleDialect.class);
//        //神通数据库
//        registerDialectAlias("oscar", MySqlDialect.class);
    }

    public static DialectHandler getDialect(String alias) {
        DialectHandler dialect = dialectInstanceCache.get(alias);
        if (dialect == null) {
            synchronized (dialectInstanceCache) {
                if (dialect == null) {
                    Class<? extends DialectHandler> aClass = dialectAliasMap.get(alias);
                    if (aClass != null) {
                        try {
                            dialectInstanceCache.put(alias, aClass.newInstance());
                        } catch (Exception e) {
                            logger.error("DialectUtil实例化DialectHandler异常：", e);
                        }
                    }
                }
            }
        }
        return dialectInstanceCache.get(alias);
    }

    public static boolean knownDialect(String alias) {
        return dialectAliasMap.containsKey(alias);
    }
}
