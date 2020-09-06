package com;

import com.auth.util.AuthHelper;
import com.auth.util.AuthQueryInfo;
import com.auth.util.RelationTypeEnum;
import com.mybatis.dao.TestMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.HashSet;

/**
 * @author wangdejun
 * @description: test
 * @date 2020/7/15 19:39
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(Application.class);
        TestMapper bean = run.getBean(TestMapper.class);
        AuthQueryInfo authQueryInfo = new AuthQueryInfo();
        AuthHelper.setCurSearch(authQueryInfo);
        HashSet hashSet = new HashSet();
        hashSet.add(340100);
        authQueryInfo.setDataScope(hashSet);
        authQueryInfo.setRelationTypeEnum(RelationTypeEnum.AND);
        authQueryInfo.setAuthTableAlias("a");
        System.out.println(bean.test(authQueryInfo).size());
    }

}
