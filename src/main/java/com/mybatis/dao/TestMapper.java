package com.mybatis.dao;

import com.auth.util.AuthQueryInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TestMapper {

    List test(AuthQueryInfo authQueryInfo);

}
