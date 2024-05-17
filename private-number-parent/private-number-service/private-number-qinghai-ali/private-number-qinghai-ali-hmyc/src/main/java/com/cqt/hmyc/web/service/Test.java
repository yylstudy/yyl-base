package com.cqt.hmyc.web.service;

import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.internal.util.StringUtils;
import com.taobao.api.request.AlibabaAliqinAxbVendorCallControlRequest;
import com.taobao.api.response.AlibabaAliqinAxbVendorCallControlResponse;

/**
 * @author linshiqiang
 * date:  2023-07-18 17:21
 */
public class Test {

    public static void main(String[] args) throws ApiException {
        TaobaoClient client = new DefaultTaobaoClient("http://gw.api.taobao.com/router/rest", "34410838", "0a8058fa778744fd721df980c73a8df2");
        AlibabaAliqinAxbVendorCallControlRequest req = new AlibabaAliqinAxbVendorCallControlRequest();
        AlibabaAliqinAxbVendorCallControlRequest.StartCallRequest obj1 = new AlibabaAliqinAxbVendorCallControlRequest.StartCallRequest();
        obj1.setExtension("123");
        obj1.setSecretNo("17010000000");
        obj1.setCallNo("13519000000");
        obj1.setCallTime(StringUtils.parseDateTime("2018-01-01 12:00:00"));
        obj1.setCallId("435cf14f7f077e52");
        obj1.setRecordType("CALL");
        obj1.setVendorKey("CMCC");
        req.setStartCallRequest(obj1);
        AlibabaAliqinAxbVendorCallControlResponse rsp = client.execute(req);
        System.out.println(rsp.getBody());
    }
}
