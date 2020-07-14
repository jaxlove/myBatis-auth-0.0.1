package com.auth.plugin;

import com.tydic.auth.util.MyBatisAuthUtils;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.sql.Connection;
import java.util.Properties;

/**
 * @author wangdejun
 * @description: 权限拦截器
 * @date 2020/1/21 14:17
 */
@Intercepts(@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}))
public class AuthPlugin implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object target = invocation.getTarget();
        //将boundSql里的sql，拼接权限语句
        setAuthBoundSql(target);
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof StatementHandler) {
            return Plugin.wrap(target, this);
        } else {
            return target;
        }
    }

    @Override
    public void setProperties(Properties properties) {
    }

    private void setAuthBoundSql(Object handler) {
        MetaObject statementHandler = SystemMetaObject.forObject(handler);
        MetaObject boundSqlHandler;
        BoundSql boundSql;
        if (handler instanceof RoutingStatementHandler) {
            boundSql = (BoundSql) statementHandler.getValue("delegate.boundSql");
        } else {
            boundSql = (BoundSql) statementHandler.getValue("boundSql");
        }
        //判断是否select语句
        //wdjtodo 判断方式待优化
        String sql = boundSql.getSql();
        if (sql.toLowerCase().indexOf("select") < 0) {
            return;
        }
        boundSqlHandler = SystemMetaObject.forObject(boundSql);
        boundSqlHandler.setValue("sql", MyBatisAuthUtils.getAuthSql(boundSqlHandler.getValue("sql").toString()));
    }

}
