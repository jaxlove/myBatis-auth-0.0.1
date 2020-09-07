package com.mybatis.dao;

import com.auth.entity.BaseAuthInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TestMapper {

    List test(BaseAuthInfo authQueryInfo);

}
