package com.auth.plugin;

import com.auth.authSql.Scope;
import com.auth.authSql.ScopeSql;
import com.auth.dialect.DialectUtil;
import com.auth.entity.BaseAuthInfo;
import com.auth.exception.AuthException;
import com.auth.exception.UnKownDialect;
import com.auth.exception.UnknownSqlTypeException;
import com.auth.util.AuthHelper;
import com.auth.util.MyBatisAuthUtils;
import com.auth.util.RelationTypeEnum;
import com.auth.util.StringUtils;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
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
        if (needSetAuth()) {
            //将boundSql里的sql，拼接权限语句
            setAuthBoundSql(target);
        }
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

    private boolean needSetAuth() {
        //没有权限查询信息，权限查询为空或者为false,自动拼接权限sql为空或者false，返回原sql
        //authInfo 新增同一线程可能需要使用多次（数据总数查询，列表查询），不可以将信息从curSearchInfo信息移除，移除操作需要自己实现
        BaseAuthInfo authInfo = AuthHelper.getCurSearchInfo();
        if (authInfo == null || authInfo.getAuthQuery() == null || !authInfo.getAuthQuery()) {
            return false;
        }
        return true;
    }

    private boolean autoAppend() {
        BaseAuthInfo authInfo = AuthHelper.getCurSearchInfo();
        if (authInfo.getAutoAppendAuth() == null || !authInfo.getAutoAppendAuth()) {
            return false;
        }
        return true;
    }

    private void propertiesCheck(Properties properties) throws AuthException {
        String dialect = properties.getProperty("dialect");
        if (StringUtils.isNotBlank(dialect) && !DialectUtil.knownDialect(dialect)) {
            throw new UnKownDialect("未知dialect：" + dialect);
        }
        Configuration.setDialect(dialect);
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
                columnProperties.put("type", column.split("##")[1]);
                authColumList.add(columnProperties);
            }
            Configuration.setAuthColumn(authColumList);
        }
    }

    private void setAuthBoundSql(Object handler) throws AuthException {
        //代理类处理
        StatementHandler nativeHandler = getNativeHandler((StatementHandler) handler);
        MetaObject statementHandlerMetaObject = SystemMetaObject.forObject(nativeHandler);
        BoundSql boundSql = (BoundSql) statementHandlerMetaObject.getValue("boundSql");
        MappedStatement ms = (MappedStatement) statementHandlerMetaObject.getValue("mappedStatement");
        //判断是否select语句
        if (ms.getSqlCommandType() != SqlCommandType.SELECT) {
            return;
        }
        MetaObject metaObject = SystemMetaObject.forObject(boundSql);
        String mappedStatementId = ms.getId();
        Object parameterObject = boundSql.getParameterObject();
        ScopeSql authSql;
        if (autoAppend()) {
            //todo 每次都要拼接，影响效率，可学习pagehelper，生成个新的MappedStatement，注入参数，缓存起来
            authSql = MyBatisAuthUtils.getAuthSql(metaObject.getValue("sql").toString(), ms.getResultMaps(), mappedStatementId, parameterObject);
        } else {
            authSql = new ScopeSql(Scope.ALL, metaObject.getValue("sql").toString());
        }
        metaObject.setValue("sql", authSql.getSql());

    }

    //代理类处理
    private StatementHandler getNativeHandler(StatementHandler statementHandler) {
        Field[] declaredFields = statementHandler.getClass().getDeclaredFields();
        //代理类处理
        for (Field declaredField : declaredFields) {
            if (declaredField.getType().isAssignableFrom(StatementHandler.class)) {
                MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
                return getNativeHandler((StatementHandler) metaObject.getValue(declaredField.getName()));
            }
        }
        return statementHandler;
    }

}
