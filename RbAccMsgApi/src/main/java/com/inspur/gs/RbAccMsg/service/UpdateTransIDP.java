package com.inspur.gs.RbAccMsg.service;

import com.inspur.fastdweb.core.FastdwebSqlSession;
import io.iec.edp.caf.commons.utils.SpringBeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UpdateTransIDP {

    @Autowired
    FastdwebSqlSession sqlSession = SpringBeanUtils.getBean(FastdwebSqlSession.class);

    public void updateSM02(String sql) {
        sqlSession.update(sql);
    }
}