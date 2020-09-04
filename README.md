# myBatis-auth-0.0.1
mybatis 自动设置权限开发
使用方法：
在mybatis-config.xml里添加插件
<plugin interceptor="com.tydic.auth.plugin.AuthPlugin">
    <property name="dialect" value="oracle"/>
    <property name="sqlType" value="SIMPLE"/>
    <property name="authColumnCheck" value="true"/>
    <property name="authColumn">
        <list>
            <value="CITY_ID##VARCHAR"/>
            <value="ACCOUNT_ID##INT"/>
        </list>
    </property>
    <property name="relationTypeEnum" value="AND"/>
</plugin>
