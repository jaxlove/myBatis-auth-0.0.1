package com.auth.plugin;

/**
 * 权限sql类型
 * COMPLEX：复杂权限sql拼接，由用户自行编写
 * SIMPLE：通过简单的字段判断权限，需要用户配置相关字段，以及字段之间的关系
 */
public enum AuthType {
    COMPLEX,SIMPLE
}
