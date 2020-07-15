package com.mybatis.dao;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TestMapper {

    void test(String name);

}
