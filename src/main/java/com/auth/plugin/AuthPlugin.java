package com.auth.plugin;

import com.auth.exception.AuthException;
import com.auth.exception.UnKownDialect;
import com.auth.exception.UnknownSqlTypeException;
import com.auth.util.MyBatisAuthUtils;
import com.auth.util.RelationTypeEnum;
import com.auth.util.pagehelper.DialectUtil;
import org.apache.commons.lang3.StringUtils;
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
import java.util.ArrayList;
import java.util.List;
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
        if (!Configuration.isInitSuccess()) {
            return invocation.proceed();
        }
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
        try {
            propertiesCheck(properties);
        } catch (AuthException e) {
            logger.error("权限插件异常：", e);
            Configuration.setInitSuccess(false);
        }
    }

    private void propertiesCheck(Properties properties) throws AuthException {
        String dialect = properties.getProperty("dialect");
        if (StringUtils.isNotBlank(dialect) && !DialectUtil.kownDialect(dialect)) {
            throw new UnKownDialect("未知dialect：" + dialect);
        }
        String sqlType = properties.getProperty("sqlType");
        if (AuthType.COMPLEX.toString().equalsIgnoreCase(sqlType)) {
            Configuration.setAuthType(AuthType.COMPLEX);
        } else if (AuthType.SIMPLE.toString().equalsIgnoreCase(sqlType)) {
            Configuration.setAuthType(AuthType.SIMPLE);
        } else {
            throw new UnknownSqlTypeException("未知sqlType，仅支持 COMPLEX;SIMPLE");
        }

        String authColumnCheck = properties.getProperty("authColumnCheck");
        if (StringUtils.isNotBlank(authColumnCheck)) {
            Boolean aBoolean = Boolean.valueOf(authColumnCheck);
            Configuration.setAuthColumnCheck(aBoolean);
        }

        String relationTypeEnum = properties.getProperty("relationTypeEnum");
        if (StringUtils.isNotBlank(relationTypeEnum)) {
            Configuration.setRelationTypeEnum(RelationTypeEnum.valueOf(relationTypeEnum.toUpperCase()));
        }

        String authColumn = properties.getProperty("authColumn");
        if (StringUtils.isNotBlank(authColumn)) {
            String[] authColumns = authColumn.split("@@");
            List<Properties> authColumList = new ArrayList<>();
            for (String column : authColumns) {
                Properties columnProperties = new Properties();
                columnProperties.put("column", column.split("##")[0]);
                columnProperties.put("jdbcType", column.split("##")[1]);
                authColumList.add(columnProperties);
            }
            Configuration.setAuthColumn(authColumList);
        }
    }

    private void setAuthBoundSql(Object handler) throws AuthException {
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
        boundSqlHandler.setValue("sql", MyBatisAuthUtils.getAuthSql(boundSqlHandler.getValue("sql").toString(),mappedStatement));
    }

}
