package com.auth.util;

/**
 * @author wangdejun
 * @description: PageHelper 插件处理类
 * @date 2020/7/27 18:42
 */
public class PageHelperUtil {

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
