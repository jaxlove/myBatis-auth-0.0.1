package com.tydic.auth.util;

import com.tydic.common.biz.entity.AuthQueryEntity;
import com.tydic.common.biz.entity.AuthQueryListen;

/**
 * @author wangdejun
 * @description: AuthQueryListen实现
 * @date 2020/2/27 10:32
 */
public class AuthHelperAuthQueryListen implements AuthQueryListen {

    @Override
    public void autoAppendAuth(AuthQueryEntity authQueryEntity) {
        AuthHelper.refreshAuthHelper(authQueryEntity);
    }

    @Override
    public void authTableAlias(AuthQueryEntity authQueryEntity) {
        AuthHelper.refreshAuthHelper(authQueryEntity);
    }
}
