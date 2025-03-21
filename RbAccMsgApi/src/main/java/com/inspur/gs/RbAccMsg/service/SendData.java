
package com.inspur.gs.RbAccMsg.service;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>sendData complex type�� Java �ࡣ
 * 
 * <p>����ģʽƬ��ָ�������ڴ����е�Ԥ�����ݡ�
 * 
 * <pre>
 * &lt;complexType name="sendData">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="reqHead" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="reqBody" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "sendDataReq", propOrder = {
    "reqHead",
    "reqBody"
})
public class SendData {

    @XmlElement(required = true)
    protected String reqHead;
    @XmlElement(required = true)
    protected String reqBody;

    /**
     * ��ȡreqHead���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReqHead() {
        return reqHead;
    }

    /**
     * ����reqHead���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReqHead(String value) {
        this.reqHead = value;
    }

    /**
     * ��ȡreqBody���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReqBody() {
        return reqBody;
    }

    /**
     * ����reqBody���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReqBody(String value) {
        this.reqBody = value;
    }

}
