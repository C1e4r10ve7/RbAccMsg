package com.inspur.gs.RbAccMsg.api;

import com.alibaba.fastjson.JSONObject;
import org.apache.axis2.AxisFault;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.ws.rs.*;
import java.io.UnsupportedEncodingException;
import java.util.List;

@Path("/wb")
public interface RbAccmsgApi {
    @GET
    @Path("/hello")
    String hello();

    @POST
    @Path("/writeBackAccmsg")
    @ResponseBody
    @RequestMapping(method = {RequestMethod.POST}, produces = {"application/json;charset=UTF-8"})
    String getAccDocInfo(@RequestBody String AccDocIDs) throws Exception;
}
