package com.auth.util.pagehelper;

import com.auth.plugin.Configuration;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author wangdejun
 * @description: PageHelper 插件处理类
 * @date 2020/7/27 18:42
 */
public class PageHelperUtil {

    private static DialectHandler dialectHandler;

    private static AtomicBoolean inited = new AtomicBoolean(false);

    private static void init() {
        String dialect = Configuration.getDialect();
        if (!StringUtils.isNotBlank(dialect)) {
            dialectHandler = DialectUtil.getDialect(dialect);
        }
    }


    public static void sufHandler(StringBuilder sql) {
        if (!inited.getAndSet(true)) {
            init();
        }
        dialectHandler.sufHandler(sql);
    }

    /**
     * 将pageHelper拼接的分页sql移除，并保存下来
     *
     * @param sql
     * @return
     */
    public static void setNativeSql(StringBuilder sql) {
        if (!inited.getAndSet(true)) {
            init();
        }
        dialectHandler.getNativeSql(sql);
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
