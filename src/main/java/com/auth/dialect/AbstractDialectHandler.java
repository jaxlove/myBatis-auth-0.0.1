package com.auth.dialect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDialectHandler implements DialectHandler {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public String getEmptySql(Class class_) {
        logger.info("数据权限为空，返回 EmptySql");
        return doGetEmptySql(class_);
    }

    abstract String doGetEmptySql(Class class_);
}
