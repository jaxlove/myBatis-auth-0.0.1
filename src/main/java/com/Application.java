package com;

import com.mybatis.dao.TestMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

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
        System.out.println(bean.test("哈哈"));
    }

}
