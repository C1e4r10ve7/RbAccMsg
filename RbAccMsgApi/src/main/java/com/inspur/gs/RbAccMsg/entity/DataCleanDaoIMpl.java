package com.inspur.gs.RbAccMsg.entity;

import com.inspur.edp.bef.api.services.IBefSessionManager;
import com.inspur.edp.caf.db.dbaccess.DbParameter;
import com.inspur.edp.caf.db.dbaccess.IDbParameter;
import com.inspur.edp.cdp.common.utils.spring.SpringUtil;
import com.inspur.edp.qdp.bql.api.IBqlExecuter;
import org.hibernate.internal.SessionImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author
 */
@Component
public class DataCleanDaoIMpl {
    @Autowired
    private IBefSessionManager befSessionManager;
    /**
     * 数据库连接
     */
    // 获取数据库连接
    private Connection conn;
    @PersistenceContext
    private EntityManager entityManager;
    private final IBqlExecuter bqlExecuter = SpringUtil.getBean(IBqlExecuter.class);
    @Transactional
    public void CleanDataInsert(String AccDocID){
        ArrayList<String> refEntityIDs = new ArrayList<>();
        refEntityIDs.add("bf9a86f3-7345-4c36-9c6a-cb8f215f9d3c");
        String executeSQL = "insert into WbAccMsg.WbAccMsg(WbAccMsg.ID,WbAccMsg.PZID,WbAccMsg.Status) values(NEWID(),@PZID,'1') ";
        IDbParameter[] params = new DbParameter[1];
        params[0] = bqlExecuter.makeInParam("PZID", AccDocID);
        bqlExecuter.executeBqlStatement(executeSQL,refEntityIDs,params);
    }

    @Transactional
    public void CleanDataUpdate(String AccDocID){
        ArrayList<String> refEntityIDs = new ArrayList<>();
        refEntityIDs.add("bf9a86f3-7345-4c36-9c6a-cb8f215f9d3c");
        String executeSQL = "update  WbAccMsg.WbAccMsg SET WbAccMsg.Status = '1' where WbAccMsg.PZID=@PZID ";
        IDbParameter[] params = new DbParameter[1];
        params[0] = bqlExecuter.makeInParam("PZID", AccDocID);
        bqlExecuter.executeBqlStatement(executeSQL,refEntityIDs,params);
    }
}