
package com.inspur.gs.RbAccMsg.service;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>sendDataResponse complex type�� Java �ࡣ
 * 
 * <p>����ģʽƬ��ָ�������ڴ����е�Ԥ�����ݡ�
 * 
 * <pre>
 * &lt;complexType name="sendDataResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="outHead" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="outBody" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "sendDataResult*", propOrder = {
    "outHead",
    "outBody"
})
public class SendDataResponse {

    @XmlElement(required = true)
    protected String outHead;
    @XmlElement(required = true)
    protected String outBody;

    /**
     * ��ȡoutHead���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOutHead() {
        return outHead;
    }

    /**
     * ����outHead���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOutHead(String value) {
        this.outHead = value;
    }

    /**
     * ��ȡoutBody���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOutBody() {
        return outBody;
    }

    /**
     * ����outBody���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOutBody(String value) {
        this.outBody = value;
    }

}
