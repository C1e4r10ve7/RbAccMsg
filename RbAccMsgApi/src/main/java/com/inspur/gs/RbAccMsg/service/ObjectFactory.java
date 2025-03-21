
package com.inspur.gs.RbAccMsg.service;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.spring.webService.ws package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _SendDataResponse_QNAME = new QName("http://www.apusic.com/esb/WS_ESB_Standard", "sendDataResponse");
    private final static QName _SendData_QNAME = new QName("http://www.apusic.com/esb/WS_ESB_Standard", "sendData");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.spring.webService.ws
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link SendData }
     * 
     */
    public SendData createSendData() {
        return new SendData();
    }

    /**
     * Create an instance of {@link SendDataResponse }
     * 
     */
    public SendDataResponse createSendDataResponse() {
        return new SendDataResponse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SendDataResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.apusic.com/esb/WS_ESB_Standard", name = "sendDataResponse")
    public JAXBElement<SendDataResponse> createSendDataResponse(SendDataResponse value) {
        return new JAXBElement<SendDataResponse>(_SendDataResponse_QNAME, SendDataResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SendData }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.apusic.com/esb/WS_ESB_Standard", name = "sendData")
    public JAXBElement<SendData> createSendData(SendData value) {
        return new JAXBElement<SendData>(_SendData_QNAME, SendData.class, null, value);
    }

}
