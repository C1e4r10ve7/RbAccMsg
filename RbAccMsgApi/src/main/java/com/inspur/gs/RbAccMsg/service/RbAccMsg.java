package com.inspur.gs.RbAccMsg.service;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.inspur.edp.bef.api.services.IBefSessionManager;
import com.inspur.edp.cef.designtime.api.util.Guid;
import com.inspur.gs.RbAccMsg.api.RbAccmsgApi;
import com.inspur.gs.RbAccMsg.entity.DataCleanDaoIMpl;
import io.iec.edp.caf.common.JSONSerializer;
import io.iec.edp.caf.commons.utils.SpringBeanUtils;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import io.iec.edp.caf.commons.utils.StringUtils;
import io.iec.edp.caf.rpc.api.service.RpcClient;
import org.hibernate.SQLQuery;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class RbAccMsg implements RbAccmsgApi {
    /**
     * 数据库连接
     */
    // 获取数据库连接
    @PersistenceContext(type = PersistenceContextType.EXTENDED)
    EntityManager entityManager = SpringBeanUtils.getBean(EntityManager.class);

    RpcClient rpcClient = SpringBeanUtils.getBean(RpcClient.class);
    /**
     * 日志
     */
    Logger logger = LoggerFactory.getLogger(RbAccMsg.class);

    @Autowired
    UpdateTransIDP updateTrans = SpringBeanUtils.getBean(UpdateTransIDP.class);
    DataCleanDaoIMpl dataCleanDao = new DataCleanDaoIMpl();

    public RbAccMsg() {
    }

    @Override
    public String hello() {
        return "hello";
    }

    @Override
    public String getAccDocInfo(String AcDocIDs) throws Exception {
        List<String> AccDocIDs = Arrays.asList(AcDocIDs.replace("\"","").split(","));
        if (AccDocIDs.size() > 0) {
            logger.error("参数：" + AccDocIDs.get(0));
        }
        //获取id数
        Integer size = AccDocIDs.size();
        logger.info("凭证数" + size);
        List sqlResultList = new ArrayList<Map<String, Object>>();

        String DWBH = "";
        //获取所有凭证下发状态，包含已下发无法操作
        List<String> newAcDocIDs = iSWriteback(AccDocIDs);
        //保存成功失败数据
        JSONObject successList = new JSONObject();
        JSONObject failList = new JSONObject();

        String getESBMapSql = "SELECT SERVICECODE AS \"SERVICECODE\", ORGID AS \"ORGID\" FROM ESBSERVICEMAP";

        sqlResultList =
                entityManager.createNativeQuery(getESBMapSql).unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).getResultList();
        Map<String,String> ESBMap = new HashMap<String,String>();
        for (int i = 0; i < sqlResultList.size(); i++) {
            ESBMap.put(((HashMap) sqlResultList.get(i)).get("ORGID").toString(),((HashMap) sqlResultList.get(i)).get("SERVICECODE").toString());
        }

        //Modify By WangJC 20250220：增加回传操作人+操作时间；在此定义操作人ID，定义回传操作时间
        String User = AccDocIDs.get(AccDocIDs.size()-1).split(":")[1];

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // 格式化当前时间
        String formattedDateTime = now.format(formatter);

        //遍历获取凭证头数据
        for (int b = 0; b < AccDocIDs.size()-1; b++) {
            String AcDocID = AccDocIDs.get(b);
            //定义凭证参数
            String year = AcDocID.split("-")[0];
                JSONObject pzParam = new JSONObject();
                String hszz = "";
                AcDocID = AcDocID.replaceFirst(AcDocID.split("-")[0]+"-","");
                String getPZTSql = "SELECT A.SECRETLEVELNUM AS \"SECNUM\", C.CODE AS \"ZBBH\",A.readonlyflag AS \"SFZD\"," +
                        "A.ACCPERIODCODE AS \"KJQJ\",A.ACCDOCDATE AS \"PZRQ\",A.YEAR AS \"KJND\",A.MAKERCODE AS \"ZDR\"," +
                        "A.NUMBEROFATTCH AS \"FJZS\",D.CODE AS \"PZR\",F.CODE AS \"CNR\",A.ABSTRACT AS \"ZY\"," +
                        "G.CODE AS \"SHR\",A.NUMBEROFNOTE AS \"TZDZS\",H.CODE AS \"JBR\",A.ID AS \"PZID\", " +
                        "A.ACCDOCCODE AS \"PZBH\", A.BIZBILLTYPEID AS \"BIZBILLTYPEID\", B.CODE AS \"DWBH\" " +
                        "FROM figlaccountingdocument" + year + " A LEFT JOIN BFACCOUNTINGORGANIZATION B " +
                        "ON A.ACCORGID=B.id left join bfledger C ON A.LEDGER=C.ID LEFT JOIN BFACCOUNTINGEMPLOYEE" + year + " D " +
                        "ON A.APPROVERID=D.ID LEFT JOIN BFACCOUNTINGEMPLOYEE" + year + " F " +
                        "ON A.cashierid=F.ID LEFT JOIN BFACCOUNTINGEMPLOYEE" + year + " G " +
                        "ON A.AUDITOR=G.ID LEFT JOIN BFACCOUNTINGEMPLOYEE" + year + " H " +
                        "ON A.OPERATORID=H.ID where A.ID='" + AcDocID + "'";
                logger.error(getPZTSql);
                sqlResultList =
                        entityManager.createNativeQuery(getPZTSql).unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).getResultList();
                if (sqlResultList.size() > 0) {
                    //获取凭证头信息
                    HashMap<String,Object> pztMapHash = (HashMap) sqlResultList.get(0);
                    logger.error(pztMapHash.toString());

                    year = pztMapHash.get("KJND").toString();

                    //注释：修改取数sql，省略多次调用数据库
                    //根据核算组织ID取编号
                    DWBH = pztMapHash.get("DWBH").toString();

                    //根据批准人ID取编号
                    if(pztMapHash.get("PZR")==null || pztMapHash.get("PZR")==""){
                        pztMapHash.put("PZR","");
                    }

                    //根据出纳人ID取编号
                    if(pztMapHash.get("CNR")==null || pztMapHash.get("CNR")==""){
                        pztMapHash.put("CNR","");
                    }

                    //根据审核人ID取编号
                    if(pztMapHash.get("SHR")==null || pztMapHash.get("SHR")==""){
                        pztMapHash.put("SHR","");
                    }

                    //根据经办人ID取编号
                    if(pztMapHash.get("JBR")==null || pztMapHash.get("JBR")==""){
                        pztMapHash.put("JBR","");
                    }

                    //增加WBPZH
                    pztMapHash.put("WBPZH", pztMapHash.get("PZBH"));

                    //根据记账人ID取编号
                    //Add by wjc 20250221 删除记账人
                    /*if(pztMapHash.get("JZR")==null || pztMapHash.get("JZR")==""){
                        pztMapHash.put("JZR","");
                    }*/

                    hszz = pztMapHash.get("DWBH").toString();
                    logger.info("所属核算组织："+hszz);

                    //组合凭证参数
                    JSONArray pztArray = new JSONArray();
                    pztArray.add(pztMapHash);
                    pzParam.put("PZT",pztArray);
                } else {
                    failList.put(AcDocID,"未找到该凭证");
                }
                //根据凭证ID获取本凭证分录号集合
                List<String> FLIDS = getFLIDList(AcDocID,year);

                //创建Array存放多个分录信息
                JSONArray pzfl = new JSONArray();

                //创建Array存放多个辅助信息
                JSONArray pzfz = new JSONArray();

                //遍历获取分录数据
                for (String flid:FLIDS) {
                    String getPZFLSql = "SELECT A.ACCENTRYCODE AS \"FLBH\",A.ACCTITLECODE AS \"KMBH\",A.ABSTRACT AS \"ZY\",A.AMOUNT " +
                            "AS \"JFJE\",A.AMOUNT AS \"DFJE\",A.QUANTITY AS \"SL\",A.UNITPRICE AS \"DJ\",A.EXCHANGERATE AS \"HL\",A.FOREIGNCURRENCY " +
                            "AS \"WB\",A.LENDINGDIRECTION AS \"JZFX\",A.BIZDATE AS \"YWRQ\",C.CODE AS \"WBBH\",A.FOREIGNCURRENCY " +
                            "AS \"JFWB\",A.FOREIGNCURRENCY AS \"DFWB\" " +
                            "FROM FIGLACCDOCENTRY"+year+" A left join bfchartofaccount"+year+" B ON A.ACCTITLEID=B.ID " +
                            "left join BFCURRENCY C ON A.CURRENCYID=C.ID " +
                            "WHERE A.ID='" + flid + "'";
                    logger.error(getPZFLSql);
                    sqlResultList =
                            entityManager.createNativeQuery(getPZFLSql).unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).getResultList();
                    if (sqlResultList.size() > 0) {
                        //获取凭证分录信息
                        HashMap<String, Object> pzflMap = (HashMap) sqlResultList.get(0);
                        logger.error(pzflMap.toString());

                        //根据科目ID取编号
                        String KMTXBH = getKMTXBH(pzflMap.get("KMBH").toString(),year);

                        logger.error("科目体系编号：::" + KMTXBH);
                        pzflMap.put("KMTXBH",KMTXBH);

                        //根据记账方向重新定义借贷金额
                        String JZFX = pzflMap.get("JZFX").toString();
                        if (JZFX.equals("1")){
                            pzflMap.put("DFJE","0");
                            pzflMap.put("DFWB","0");
                        }else {
                            pzflMap.put("JFJE","0");
                            pzflMap.put("JFWB","0");
                        }

                        //组合凭证分录参数
                        logger.error("：：：分录参数整合：：：");
                        pzfl.add(pzflMap);

                        //根据分录ID获取分录下辅助集合
                        logger.error("：：：获取辅助ID List：：：");
                        List<String> FZIDS = getFZIDList(flid,year,AcDocID);
                        logger.error("：：：辅助ID获取完成：：：");
                        //遍历获取辅助信息
                        for (String fzid: FZIDS) {
                            String FZXMSql = "A.SPECATEID01 AS \"XM01\",A.SPECATEID02 AS \"XM02\",A.SPECATEID03 AS \"XM03\",A.SPECATEID04 AS \"XM04\",A.SPECATEID05 AS \"XM05\",A.SPECATEID06 AS \"XM06\",A.SPECATEID07 AS \"XM07\",A.SPECATEID08 AS \"XM08\",A.SPECATEID09 AS \"XM09\",A.SPECATEID10 AS \"XM10\",A.SPECATEID11 AS \"XM11\",A.SPECATEID12 AS \"XM12\",A.SPECATEID13 AS \"XM13\",A.SPECATEID14 AS \"XM14\",A.SPECATEID15 AS \"XM15\",A.SPECATEID16 AS \"XM16\",A.SPECATEID17 AS \"XM17\",A.SPECATEID18 AS \"XM18\",A.SPECATEID19 AS \"XM19\",A.SPECATEID20 AS \"XM20\",A.SPECATEID21 AS \"XM21\",A.SPECATEID22 AS \"XM22\",A.SPECATEID23 AS \"XM23\",A.SPECATEID24 AS \"XM24\",A.SPECATEID25 AS \"XM25\",A.SPECATEID26 AS \"XM26\",A.SPECATEID27 AS \"XM27\",A.SPECATEID28 AS \"XM28\",A.SPECATEID29 AS \"XM29\",A.SPECATEID30 AS \"XM30\",A.SPECATEID31 AS \"XM31\",A.SPECATEID32 AS \"XM32\",A.SPECATEID33 AS \"XM33\",A.SPECATEID34 AS \"XM34\",A.SPECATEID35 AS \"XM35\",A.SPECATEID36 AS \"XM36\",A.SPECATEID37 AS \"XM37\",A.SPECATEID38 AS \"XM38\",A.SPECATEID39 AS \"XM39\",A.SPECATEID40 AS \"XM40\",A.SPECATEID41 AS \"XM41\",A.SPECATEID42 AS \"XM42\",A.SPECATEID43 AS \"XM43\",A.SPECATEID44 AS \"XM44\",A.SPECATEID45 AS \"XM45\",A.SPECATEID46 AS \"XM46\",A.SPECATEID47 AS \"XM47\",A.SPECATEID48 AS \"XM48\",A.SPECATEID49 AS \"XM49\",A.SPECATEID50 AS \"XM50\",A.SPECATEID51 AS \"XM51\",A.SPECATEID52 AS \"XM52\",A.SPECATEID53 AS \"XM53\",A.SPECATEID54 AS \"XM54\",A.SPECATEID55 AS \"XM55\",A.SPECATEID56 AS \"XM56\",A.SPECATEID57 AS \"XM57\",A.SPECATEID58 AS \"XM58\",A.SPECATEID59 AS \"XM59\",A.SPECATEID60 AS \"XM60\",A.SPECATEID61 AS \"XM61\",A.SPECATEID62 AS \"XM62\",A.SPECATEID63 AS \"XM63\",A.SPECATEID64 AS \"XM64\",A.SPECATEID65 AS \"XM65\",A.SPECATEID66 AS \"XM66\",A.SPECATEID67 AS \"XM67\",A.SPECATEID68 AS \"XM68\",A.SPECATEID69 AS \"XM69\",A.SPECATEID70 AS \"XM70\",A.SPECATEID71 AS \"XM71\",A.SPECATEID72 AS \"XM72\",A.SPECATEID73 AS \"XM73\",A.SPECATEID74 AS \"XM74\",A.SPECATEID75 AS \"XM75\",A.SPECATEID76 AS \"XM76\",A.SPECATEID77 AS \"XM77\",A.SPECATEID78 AS \"XM78\",A.SPECATEID79 AS \"XM79\",A.SPECATEID80 AS \"XM80\",A.SPECATEID81 AS \"XM81\",A.SPECATEID82 AS \"XM82\",A.SPECATEID83 AS \"XM83\",A.SPECATEID84 AS \"XM84\",A.SPECATEID85 AS \"XM85\",A.SPECATEID86 AS \"XM86\",A.SPECATEID87 AS \"XM87\",A.SPECATEID88 AS \"XM88\",A.SPECATEID89 AS \"XM89\",A.SPECATEID90 AS \"XM90\",A.SPECATEID91 AS \"XM91\",A.SPECATEID92 AS \"XM92\",A.SPECATEID93 AS \"XM93\",A.SPECATEID94 AS \"XM94\",A.SPECATEID95 AS \"XM95\",A.SPECATEID96 AS \"XM96\",A.SPECATEID97 AS \"XM97\",A.SPECATEID98 AS \"XM98\",A.SPECATEID99 AS \"XM99\"";

                            String getPZFZSql = "SELECT A.ACCASSCODE AS \"FZBH\",A.ACCTITLECODE AS \"KMBH\",B.CODE " +
                                    "AS \"BMBH\",C.CODE AS \"WLDWBH\",D.CODE AS \"ZGBH\",E.CODE " +
                                    "AS \"WBBH\",A.LENDINGDIRECTION AS \"JZFX\",A.QUANTITY AS \"SL\",A.UNITPRICE AS \"DJ\",A.FOREIGNCURRENCY " +
                                    "AS \"WB\",A.EXCHANGERATE AS \"HL\",A.AMOUNT AS \"JE\",A.APPLICATION AS \"ZY\",A.BIZDATE AS \"YWRQ\",A.BIZCODE " +
                                    "AS \"YWH\",A.OPERATOR AS \"ZRR\",A.BILLNUMBER AS \"PJH\",SJ01,SJ02,SJ03,SJ04,SJ05,SM01,SM02,SM03," +
                                    "SM04,SM05,SM06,SM07,SM08,SM09,SM10,F.CODE AS \"JSFS\",A.SETTLEMENTNUMBER AS \"JSH\"," +
                                    FZXMSql + " FROM FIGLACCDOCASSISTANCE"+year+" A LEFT JOIN BFACCOUNTINGDEPARTMENT"+year+" B ON A.DEPTID=B.ID " +
                                    "LEFT JOIN BFPARTNER C ON A.RELATEDORGID=C.ID LEFT JOIN BFACCOUNTINGEMPLOYEE"+year+" D ON A.ACCEMPLOYEEID=D.ID " +
                                    "LEFT JOIN BFCURRENCY E ON A.FOREIGNCURRENCYID=E.ID LEFT JOIN BFSETTLEMENTWAY F ON A.SETTLEMENT=F.ID " +
                                    " WHERE A.ID='" + fzid + "'";
                            logger.error(getPZFZSql);
                            sqlResultList =
                                    entityManager.createNativeQuery(getPZFZSql).unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).getResultList();
                            if (sqlResultList.size() > 0) {
                                //获取凭证分录信息
                                HashMap<String, Object> pzfzMap = (HashMap) sqlResultList.get(0);
                                logger.error(pzfzMap.toString());
                                //从分录参数中获取分录编号
                                pzfzMap.put("FLBH",pzflMap.get("FLBH"));

                                //注释：修改取数sql，省略多次调用数据库
                                //获取核算部门ID转编号
                                if (pzfzMap.get("BMBH")==null || pzfzMap.get("BMBH")==""){
                                    pzfzMap.put("BMBH","");
                                }

                                //获取往来单位ID转编号
                                if (pzfzMap.get("WLDWBH")==null || pzfzMap.get("WLDWBH")==""){
                                    pzfzMap.put("WLDWBH","");
                                }

                                //获取核算人员ID转编号
                                if (pzfzMap.get("ZGBH")==null || pzfzMap.get("ZGBH")==""){
                                    pzfzMap.put("ZGBH","");
                                }

                                //结算方式
                                if (pzfzMap.get("JSFS")==null || pzfzMap.get("JSFS")==""){
                                    pzfzMap.put("JSFS","");
                                }

                                //获取核算项目编号
                                for (int i = 1; i < 100; i++){
                                    String XMLX = "";
                                    if (i < 10){ XMLX = "0" + i; }
                                    else { XMLX = "" + i; }
                                    //取辅助核算项目ID
                                    if(pzfzMap.get("XM"+XMLX) == null){
                                        pzfzMap.put("XM" + XMLX,"");
                                    }else{
                                        String XMID = pzfzMap.get("XM"+XMLX).toString();
                                        //根据辅助核算项目ID查编号
                                        if (XMID.replaceAll(" ","").equals("")){
                                            pzfzMap.put("XM" + XMLX,"");
                                        }else{
                                            String XMBH = getXMBH(XMID,year);
                                            pzfzMap.put("XM" + XMLX,XMBH);
                                        }
                                    }
                                }
                                pzfz.add(pzfzMap);
                            }
                        }
                    }
                }
                //组合凭证参数
                //组合凭证参数
                pzParam.put("ZWFZYS",pzfz);
                pzParam.put("ZWPZFLOP",pzfl);
                logger.error("凭证参数："+JSONObject.toJSONString(pzParam,SerializerFeature.WriteMapNullValue));

                //调用总线接口
                SendData sendDataParam = new SendData();
                UUID uuid = Guid.newGuid();
                String ESBUUID = uuid.toString();

                //根据凭证参数确认接口名称
                String serviceCode = ESBMap.get(DWBH);
                logger.info("核算组织0:"+DWBH);
                /*if(DWBH.equals("6751")){
                    serviceCode = ESBMap.get(hszz+"6751");
                    logger.info("服务0:"+serviceCode);
                }*/

                logger.error("核算组织:"+DWBH);
                logger.error("服务:"+serviceCode);
                if (serviceCode == null){
                    logger.error("ERROR:未在ESB获取到服务");
                    failList.put(AcDocID,"未在ESB获取到服务");
                }else {
                    logger.error("POST SOAP");
                    Date date = new Date();
                    SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

                    sendDataParam.setReqHead("{\"clientID\":\"CWGX\",\"reqBatchID\":\"" + ESBUUID + "\",\"reqTime\":\"" + ft.format(date) + "\",\"tarService\":\"" + serviceCode + "\"}");
                    sendDataParam.setReqBody(JSONObject.toJSONString(pzParam,SerializerFeature.WriteMapNullValue));
                    Holder<String> resultHead = new Holder<>();
                    Holder<String> resultBody = new Holder<>();

                    WSESBStandard wsesbStandard = new WSESBStandard();
                    WSESBStandardPortType wsesbStandardPortType = wsesbStandard.getWSESBStandardHttpSoap11Endpoint();
                    logger.error("调用ESB服务");
                    wsesbStandardPortType.sendData(sendDataParam.getReqHead(), sendDataParam.getReqBody(), resultHead, resultBody);
                    JSONObject ESBResult = JSONObject.parseObject(resultBody.value);

                    if (ESBResult == null){
                        JSONObject result = new JSONObject();
                        result.put("successNum",0);
                        result.put("failNum",0);
                        result.put("msg","未接收到ESB返回值，请联系管理员查看");
                    }
                    logger.error("ESB返回：：："+ESBResult.toJSONString());
                    if (StringUtils.isEmpty(ESBResult.getString("status"))){
                        failList.put(AcDocID,"未获取到返回状态,返回信息为："+ESBResult.toJSONString());
                    }else{
                        logger.error("ESB返回状态：" + ESBResult.getString("status").toString());
                        if (ESBResult.getString("status").equals("Success")){
                            successList.put(AcDocID,"success");
                            LinkedHashMap<String, Object> paramMap = new LinkedHashMap();
                            Map<String, Object> info = new HashMap<>();
                            info.put("year", year);
                            info.put(AcDocID, getUSERBH(User));
                            paramMap.put("param", JSONSerializer.serialize(info));
                            Integer a = rpcClient.invoke(Integer.class,
                                    "com.inspur.gs.fi.gl.accountingdocument.api.service.FIGLAccDoc_RpcService.updateExternalDocInfo",
                                    "gl", paramMap, null);
                            if (a > 0) {
                                logger.error("外部凭证号保存成功");
                            } else {
                                logger.error("外部凭证号保存失败");
                            }
                            //执行更新操作时间
                            String sql = "update figlaccountingdocument"+year+" set sm02='"+formattedDateTime+"' where id='"+AcDocID+"'";
                            updateTrans.updateSM02(sql);
                        }else{
                            if (StringUtils.isEmpty(ESBResult.getString("message"))){
                                failList.put(AcDocID,"凭证上传失败,未获取到失败信息,返回信息为："+ESBResult);
                            }else{
                                failList.put(AcDocID,ESBResult);
                            }
                        }
                    }
                }
        }
        JSONObject result = new JSONObject();
        result.put("successNum",successList.size());
        result.put("failNum",failList.size());
        if (failList.size() > 0){
            result.put("failMsg",failList.toJSONString());
        }
        return result.toJSONString();
    }

    /**
     * 获取核算组织编号
     */
    public String getDWBH(HashMap pztMap1){
        List sqlResultList = new ArrayList<Map<String, Object>>();
        String dwid = pztMap1.get("DWBH").toString();
        String dwSql = "SELECT code AS \"CODE\" FROM BFACCOUNTINGORGANIZATION WHERE id='" + dwid + "'";
        sqlResultList =
                entityManager.createNativeQuery(dwSql).unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).getResultList();
        return ((HashMap) sqlResultList.get(0)).get("CODE").toString();
    }

    /**
     * 获取账簿编号
     */
    public String getZBBH(HashMap pztMap){
        List sqlResultList = new ArrayList<Map<String, Object>>();
        String zbid = pztMap.get("ZBBH").toString();
        String zbSql = "SELECT code AS \"CODE\" FROM bfledger WHERE id='" + zbid + "'";
        sqlResultList =
                entityManager.createNativeQuery(zbSql).unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).getResultList();
        return ((HashMap) sqlResultList.get(0)).get("CODE").toString();
    }

    /**
     * 获取人员编号
     */
    public String getUSERBH(String ryid){
        List sqlResultList = new ArrayList<Map<String, Object>>();
        String rySql = "SELECT code AS \"CODE\" FROM GSPUSER WHERE id='" + ryid + "'";
        sqlResultList =
                entityManager.createNativeQuery(rySql).unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).getResultList();
        if (sqlResultList.size() > 0) {
            return ((HashMap) sqlResultList.get(0)).get("CODE").toString();
        } else {
            return ryid;
        }
    }

    /**
     * 获取分录编号集合
     */
    public List<String> getFLIDList(String pzid,String year){
        List sqlResultList = new ArrayList<Map<String, Object>>();
        String flidSql = "SELECT id AS \"ID\" FROM figlaccdocentry"+year+" WHERE accdocid='" + pzid + "'";
        sqlResultList =
                entityManager.createNativeQuery(flidSql).unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).getResultList();
        //创建分录ID集合
        List<String> flids = new ArrayList<>();
        //获取分录数
        int size = sqlResultList.size();
        for (int i = 0; i < size; i++){
            flids.add(((HashMap) sqlResultList.get(i)).get("ID").toString());
        }
        return flids;
    }

    /**
     * 获取科目体系编号
     */
    public String getKMTXBH(String kmbh,String year){
        List sqlResultList = new ArrayList<Map<String, Object>>();
        logger.error("：：：进入getKMTXBH方法：：：");
        String kmtxid = getKMTXID(kmbh,year);
        String kmbhSql = "SELECT code AS \"CODE\" FROM bfchartofaccount"+year+" where id='"+kmtxid+"'";
        sqlResultList =
                entityManager.createNativeQuery(kmbhSql).unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).getResultList();
        logger.error("：：：getKMTXBH方法完成：：：");
        return ((HashMap) sqlResultList.get(0)).get("CODE").toString();
    }

    /**
     * 获取科目体系ID
     */
    public String getKMTXID(String kmbh,String year){
        List sqlResultList = new ArrayList<Map<String, Object>>();
        logger.error("：：：进入getKMTXID方法：：：");
        String kmtxidSql = "SELECT CHARTOFACC AS \"CHARTOFACC\" FROM bfaccounttitle"+year+" where code='" + kmbh + "'";
        sqlResultList =
                entityManager.createNativeQuery(kmtxidSql).unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).getResultList();
        //创建分录ID集合
        logger.error("：：：getKMTXID方法完成：：：");
        return ((HashMap) sqlResultList.get(0)).get("CHARTOFACC").toString();
    }

    /**
     * 根据外币ID获取外币编号
     */
    public String getWBBH(String wbid){
        List sqlResultList = new ArrayList<Map<String, Object>>();
        String kmtxidSql = "SELECT CODE AS \"CODE\" FROM BFCURRENCY WHERE ID='" + wbid + "'";
        sqlResultList =
                entityManager.createNativeQuery(kmtxidSql).unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).getResultList();
        //创建分录ID集合
        return ((HashMap) sqlResultList.get(0)).get("CODE").toString();
    }

    /**
     * 根据分录ID获取辅助集合
     */
    public List<String> getFZIDList(String flid,String year,String pzid){
        logger.error("：：：进入getFZIDList方法：：：");
        List sqlResultList = new ArrayList<Map<String, Object>>();
        String fzidSql = "SELECT id AS \"ID\" FROM figlaccdocassistance"+year+" WHERE ACCDOCENTRYID='" + flid + "' AND ACCDOCID='"+pzid+"'";
        sqlResultList =
                entityManager.createNativeQuery(fzidSql).unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).getResultList();
        //创建分录ID集合
        List<String> fzids = new ArrayList<>();
        logger.error("：：：查询完成：：：");
        //获取分录数
        int size = sqlResultList.size();
        for (int i = 0; i < size; i++){
            fzids.add(((HashMap) sqlResultList.get(i)).get("ID").toString());
        }
        logger.error("：：：getFZIDList方法完成：：：");
        return fzids;
    }

    /**
     * 根据核算部门ID获取编号
     */
    public String getHSBMBH(String bmid,String year){
        List sqlResultList = new ArrayList<Map<String, Object>>();
        String kmbhSql = "SELECT CODE AS \"CODE\" FROM BFACCOUNTINGDEPARTMENT"+year+" WHERE ID='" + bmid + "'";
        sqlResultList =
                entityManager.createNativeQuery(kmbhSql).unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).getResultList();
        return ((HashMap) sqlResultList.get(0)).get("CODE").toString();
    }

    /**
     * 根据往来单位ID获取编号
     */
    public String getWLDWBH(String dwid){
        List sqlResultList = new ArrayList<Map<String, Object>>();
        String kmbhSql = "SELECT CODE AS \"CODE\" FROM BFPARTNER WHERE ID='" + dwid + "'";
        sqlResultList =
                entityManager.createNativeQuery(kmbhSql).unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).getResultList();
        return ((HashMap) sqlResultList.get(0)).get("CODE").toString();
    }

    /**
     * 根据核算人员ID获取编号
     */
    public String getHSRYBH(String zgid,String year){
        List sqlResultList = new ArrayList<Map<String, Object>>();
        String kmbhSql = "SELECT CODE AS \"CODE\" FROM BFACCOUNTINGEMPLOYEE"+year+" WHERE ID='" + zgid + "'";
        sqlResultList =
                entityManager.createNativeQuery(kmbhSql).unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).getResultList();
        return ((HashMap) sqlResultList.get(0)).get("CODE").toString();
    }

    /**
     * 根据结算方式ID获取编号
     */
    public String getJSFSBH(String jsfsid){
        List sqlResultList = new ArrayList<Map<String, Object>>();
        String kmbhSql = "SELECT CODE AS \"CODE\" FROM BFSETTLEMENTWAY where id='" + jsfsid + "'";
        sqlResultList =
                entityManager.createNativeQuery(kmbhSql).unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).getResultList();
        return ((HashMap) sqlResultList.get(0)).get("CODE").toString();
    }

    /**
     * 根据辅助核算项目ID获取编号
     */
    public String getXMBH(String xmid,String year){
        List sqlResultList = new ArrayList<Map<String, Object>>();
        String kmbhSql = "SELECT CUSITEMCODE AS \"CUSITEMCODE\" FROM BFCUSTOMITEM"+year+" WHERE ID='" + xmid + "'";
        sqlResultList =
                entityManager.createNativeQuery(kmbhSql).unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).getResultList();
        return ((HashMap) sqlResultList.get(0)).get("CUSITEMCODE").toString();
    }

    /**
     * 获取核算组织ID
     */
    public String getHSZZID(String DWBH) {
        List sqlResultList = new ArrayList<Map<String, Object>>();
        String executeSQL = "SELECT id AS \"ID\" FROM BFACCOUNTINGORGANIZATION WHERE code='" + DWBH + "'";
        sqlResultList =
                entityManager.createNativeQuery(executeSQL).unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).getResultList();
        if (sqlResultList.size() > 0) {
            return ((HashMap) sqlResultList.get(0)).get("ID").toString();
        } else {
            return "1";
        }
    }

    /**
     * 获取二级核算组织ID
     */
    public String getLastHszz(List<String> HSZZIDS) {
        for (String hszzid:HSZZIDS) {
            List sqlResultList = new ArrayList<Map<String, Object>>();
            String executeSQL = "select parentid AS \"PARENTID\" from BFACCOUNTINGORGANIZATION where ID='" + hszzid + "'";
            sqlResultList =
                    entityManager.createNativeQuery(executeSQL).unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).getResultList();
            if (sqlResultList.size() > 0) {
                String hszzider = ((HashMap) sqlResultList.get(0)).get("PARENTID").toString();
                if (hszzider.equals("66b82407-e222-78h9-b389-ekd126iz1296")){
                    return hszzid;
                }
            }
        }
        return "1";
    }

    /**
     * 获取核算组织集合
     */
    public List<String> getHSZZList(String DWBH) {
        List<String> HSZZIDS = new ArrayList<>();
        String HSZZID = getHSZZID(DWBH);
        while (!getParentHSZZID(HSZZID).equals("1")){
            String hszzid = getParentHSZZID(HSZZID);
            logger.info("各级核算组织ID："+hszzid);
            HSZZIDS.add(HSZZID);
            HSZZID = hszzid;
        }
        return HSZZIDS;
    }

    /**
     * 获取上级核算组织ID
     */
    public String getParentHSZZID(String HSZZID) {
        List sqlResultList = new ArrayList<Map<String, Object>>();
        String executeSQL = "select parentid AS \"PARENTID\" from BFACCOUNTINGORGANIZATION where ID='" + HSZZID + "'";
        logger.info(executeSQL);
        sqlResultList =
                entityManager.createNativeQuery(executeSQL).unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).getResultList();
        try{
            if (sqlResultList.size() > 0){
                return ((HashMap) sqlResultList.get(0)).get("PARENTID").toString();
            }
            return "1";
        }catch (Exception e){
            return "1";
        }
    }

    /**
     * 是否下发
     */
    public List<String> iSWriteback(List<String> AccDocIDs) {
        List sqlResultList = new ArrayList<Map<String, Object>>();
        String IDS = AccDocIDs.toString();
        logger.info(IDS);
        for (String id:AccDocIDs
        ) {
            String executeSQL = "select STATUS AS \"STATUS\" from fiwbaccmsg where PZID='"+id+"'";
            logger.info(executeSQL);
            sqlResultList =
                    entityManager.createNativeQuery(executeSQL).unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).getResultList();
            logger.info("凭证下发状态："+sqlResultList.size());
            if (sqlResultList.size() > 0){
                if(((HashMap)sqlResultList.get(0)).get("STATUS").toString()=="1"){
                    AccDocIDs.remove(id);
                }
            }
        }
        return AccDocIDs;
    }

    /**
     * 更新下发状态
     */

    public void updateStatus(String AccDocID) {
        List sqlResultList = new ArrayList<Map<String, Object>>();
        String executeSQL = "select PZID AS \"PZID\" from fiwbaccmsg where PZID='"+AccDocID+"'";
        logger.info(executeSQL);
        sqlResultList =
                entityManager.createNativeQuery(executeSQL).unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).getResultList();
        logger.info("是否存在该凭证下发状态："+sqlResultList.size());
        if(sqlResultList.size()>0){
            dataCleanDao.CleanDataUpdate(AccDocID);
        }else {
            dataCleanDao.CleanDataInsert(AccDocID);
        }
    }
    /**
     * 更新下发状态
     */

    public boolean update(String AccDocID){
        String executeSQL = "update fiwbaccmsg set status = '1' where PZID='"+AccDocID+"'";
        int res = entityManager.createNativeQuery(executeSQL).executeUpdate();
        logger.info("更新结果"+res);
        if (res==1){
            return true;
        }
        return false;
    }

    /**
     * 更新下发状态
     */

    public boolean insert(String AccDocID){
        String executeSQL = "insert into fiwbaccmsg values('"+AccDocID+"','1',' ',' ',' ')";
        int res = entityManager.createNativeQuery(executeSQL).executeUpdate();
        logger.info("更新结果"+res);
        if (res==1){
            return true;
        }
        return false;
    }

    //region //公共方法
    //region //FastJSON的序列化设置
    private static SerializerFeature[] features = new SerializerFeature[]{
            //输出Map中为Null的值
            SerializerFeature.WriteMapNullValue,

            //如果Boolean对象为Null，则输出为false
            SerializerFeature.WriteNullBooleanAsFalse,

            //如果List为Null，则输出为[]
            SerializerFeature.WriteNullListAsEmpty,

            //如果Number为Null，则输出为0
            SerializerFeature.WriteNullNumberAsZero,

            //输出Null字符串
            SerializerFeature.WriteNullStringAsEmpty,

            //格式化输出日期
            SerializerFeature.WriteDateUseDateFormat
    };
    //endregion

    //endregion

    /**
     * 构造失败返回值
     */
    public String getResult(String badResult) {
        if (badResult.equals("1")) {
            badResult = new String("参数中存在未匹配数据".getBytes(), StandardCharsets.UTF_8);
        }
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("status", "fail");
        result.put("message", badResult);
        return JSON.toJSONString(result, features);
    }

    /**
     * 构造成功返回值
     */
    public String getSuccess() {
        Map<String, Object> result = new HashMap<String, Object>();
        Map<String, String> pzRelation = new HashMap<String, String>();
        result.put("status", "Success");
        result.put("message", "");
        return JSON.toJSONString(result, features);
    }
    //endregion

    public static void main(String[] args) throws Exception {
        String User = "123456";

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // 格式化当前时间
        String formattedDateTime = now.format(formatter);

        System.out.println(User + "&" + formattedDateTime);

    }
}

