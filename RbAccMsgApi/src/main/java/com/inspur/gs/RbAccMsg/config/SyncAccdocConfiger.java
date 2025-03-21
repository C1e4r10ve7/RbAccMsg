package com.inspur.gs.RbAccMsg.config;

import com.inspur.gs.RbAccMsg.service.RbAccMsg;
import io.iec.edp.caf.rest.RESTEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SyncAccdocConfiger {
    @Bean
    public RESTEndpoint defineWriteBackAccApi() {
        RbAccMsg rbAccmsg = new RbAccMsg();
        return new RESTEndpoint("/jg/syncinterface/v1.0/writebackaccdocapi", rbAccmsg);}
}
