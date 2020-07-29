package com.auth.plugin;

import com.auth.exception.AuthException;
import com.auth.exception.UnknownSqlTypeException;
import com.auth.util.MyBatisAuthUtils;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.Properties;

/**
 * @author wangdejun
 * @description: 权限拦截器
 * @date 2020/1/21 14:17
 */
@Intercepts(@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}))
public class AuthPlugin implements Interceptor {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object target = invocation.getTarget();
        //将boundSql里的sql，拼接权限语句
        if (!Configuration.isInitSuccess()) {
            return invocation.proceed();
        }
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
        try {
            propertiesCheck(properties);
        } catch (AuthException e) {
            logger.error("权限插件异常：", e);
            Configuration.setInitSuccess(false);
        }
    }

    private void propertiesCheck(Properties properties) throws AuthException {
        String sqlType = properties.getProperty("sqlType");
        if (AuthType.COMPLEX.toString().equalsIgnoreCase(sqlType)) {
            Configuration.setAuthType(AuthType.COMPLEX);
        } else if (AuthType.SIMPLE.toString().equalsIgnoreCase(sqlType)) {
            Configuration.setAuthType(AuthType.SIMPLE);
        } else {
            throw new UnknownSqlTypeException("未知sqlType，仅支持 COMPLEX;SIMPLE");
        }
    }

    private void setAuthBoundSql(Object handler) {
        MetaObject statementHandler = SystemMetaObject.forObject(handler);
        MetaObject boundSqlHandler;
        BoundSql boundSql;
        MappedStatement mappedStatement;

        if (handler instanceof RoutingStatementHandler) {
            boundSql = (BoundSql) statementHandler.getValue("delegate.boundSql");
            mappedStatement = (MappedStatement) statementHandler.getValue("delegate.mappedStatement");
        } else {
            boundSql = (BoundSql) statementHandler.getValue("boundSql");
            mappedStatement = (MappedStatement) statementHandler.getValue("mappedStatement");
        }
        //判断是否select语句
        if (mappedStatement.getSqlCommandType() != SqlCommandType.SELECT) {
            return;
        }
        boundSqlHandler = SystemMetaObject.forObject(boundSql);
        boundSqlHandler.setValue("sql", MyBatisAuthUtils.getAuthSql(boundSqlHandler.getValue("sql").toString()));
    }

}
