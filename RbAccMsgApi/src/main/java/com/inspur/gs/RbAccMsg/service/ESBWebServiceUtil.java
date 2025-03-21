package com.inspur.gs.RbAccMsg.service;


import com.alibaba.fastjson.JSONObject;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * ESB WebService接口调用工具类
 */
public class ESBWebServiceUtil {

    private static final Logger log = LoggerFactory.getLogger(ESBWebServiceUtil.class);


    public static String doPost(String serviceCode, String url, String clientId, Object data) throws Exception {
        StringBuffer s = null;
        try {
            ArrayList<Object> dataList = new ArrayList<>();
            dataList.add(data);
            JSONObject pzParam = new JSONObject();
            pzParam.put("datas", dataList);
            //调用总线接口
            log.error("创建ServiceClient");
            ServiceClient serviceClient = new ServiceClient();
            //创建服务地址WebService的URL,注意不是WSDL的URL
            log.error("创建EndpointReference");
            EndpointReference targetEPR = new EndpointReference(url);
            log.error("获取Options");
            Options options = serviceClient.getOptions();
            options.setTo(targetEPR);
            //确定调用方法（wsdl 命名空间地址 (wsdl文档中的targetNamespace) 和 方法名称 的组合）
            options.setAction("http://www.apusic.com/esb/WS_ESB_Standard" + "sendData");
            s = new StringBuffer();
            log.error("获取OMFactory");
            OMFactory fac = OMAbstractFactory.getOMFactory();
            /*
             * 指定命名空间，参数：
             * uri--即为wsdl文档的targetNamespace，命名空间
             * perfix--可不填
             */
            OMNamespace omNs = fac.createOMNamespace("http://www.apusic.com/esb/WS_ESB_Standard", "");
            // 指定方法
            OMElement method = fac.createOMElement("sendData", omNs);
            OMElement reqHead = fac.createOMElement("reqHead", omNs);
            Date date = new Date();
            SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String uuid = UUID.randomUUID().toString();
            reqHead.setText("{\"clientID\":\"" + clientId + "\",\"reqBatchID\":\"" + uuid + "\",\"reqTime\":\"" + ft.format(date) + "\",\"tarService\":\"" + serviceCode + "\"}");
            OMElement reqBody = fac.createOMElement("reqBody", omNs);
            reqBody.setText(pzParam.toJSONString());
            method.addChild(reqHead);
            method.addChild(reqBody);
            method.build();

            serviceClient.cleanupTransport();
            OMElement result = serviceClient.sendReceive(method);
            Iterator it = result.getChildElements();

            while (it.hasNext()) {
                OMElement ome = (OMElement) it.next();
                if ("outBody".equals(ome.getLocalName())) {
                    s.append(ome.getText());
                }
            }
            log.error("转换后返回值为：" + s.toString());
            return s.toString();
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
            log.error("接口调用错误 AxisFault：",axisFault);
            return axisFault.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("接口调用错误 Exception：",e);
            throw e;
        }
    }


    private ESBWebServiceUtil() {
    }
}
