//package com.yyl;
//
//import com.fasterxml.jackson.annotation.JsonInclude;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//import org.junit.jupiter.api.Test;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.MediaType;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.TreeMap;
//
///**
// * @author yang.yonglian
// * @version 1.0.0
// * @Description TODO
// * @createTime 2023/7/12 11:05
// */
//
//public class MyTest1 {
//    private static ObjectMapper objectMapper = new ObjectMapper();
//    static{
//        //值为空不参与序列化
//        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
//    }
//    @Test
//    public void test1(){
//        SysUser sysUser = new SysUser();
//        sysUser.setSex("1");
//        long timestamp = System.currentTimeMillis();
//        Map<String,Object> urlParamsMap = new HashMap<>();
//        urlParamsMap.put("key2","2222");
//        urlParamsMap.put("key","1111");
//        String appKey = "linkcircle";
//        String appSecret = "cqt@1234";
//        String bodyStr = toJSONString(sysUser);
//        String sign = sign(sysUser,urlParamsMap,timestamp,appKey,appSecret);
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("X-TIMESTAMP",String.valueOf(timestamp));
//        headers.add("X-APP-KEY",appKey);
//        headers.add("X-Sign",sign);
//        headers.add("X-Access-Token","eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJuYmYiOjE3MTE0MzgyMTksImNvcnBJZCI6IjAwMDAiLCJwaG9uZSI6IjE1MjU1MTc4NTUzIiwiZGVwYXJ0SWQiOiIxNzY0MjYwMTEyOTExODQzMzI4IiwiaWQiOjE3NjQyNjI4OTk3NjI2MTAxNzYsImV4cCI6MTcxMTQ1NjIxOSwiaWF0IjoxNzExNDM4MjE5LCJlbWFpbCI6IjE1OTQ4MTg5NTRAcXEuY29tIiwicmVhbG5hbWUiOiJhZG1pbiJ9.dYsthEfI-_wxTwzD08w5Zr18lRY3eJHNTBbaFnl5PdQ");
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        RestTemplate restTemplate = new RestTemplate();
//        HttpEntity httpEntity = new HttpEntity(bodyStr,headers);
//        String result = restTemplate.postForObject("http://172.0.18.100:8084/get?key=1111&key2=2222",httpEntity,String.class);
////        String body = HttpRequest.post("http://localhost:7001/test1?id=")
////        String body = HttpRequest.post("http://localhost:9999/sys/test1?id=1")
////                .header("X-TIMESTAMP",String.valueOf(timestamp))
////                .header("APP-KEY",appKey)
////                .header("X-Sign",sign)
////                .body(bodyStr)
////                .execute().body();
//        System.out.println(result);
//
//    }
//
//
//    @Test
//    public void test2(){
//        SysUser sysUser = new SysUser();
//        sysUser.setSex("1");
//        long timestamp = System.currentTimeMillis();
//        System.out.println(timestamp);
//        Map<String,Object> urlParamsMap = new HashMap<>();
//        urlParamsMap.put("password","cqt@1234");
//        urlParamsMap.put("id","");
//        urlParamsMap.put("vccId","0000");
//        String appKey = "linkcircle";
//        String appSecret = "cqt@1234";
////        String bodyStr = toJSONString(loginReqDto);
//        String sign = sign(null,urlParamsMap,timestamp,appKey,appSecret);
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("X-TIMESTAMP",String.valueOf(timestamp));
//        headers.add("X-APP-KEY",appKey);
//        headers.add("X-Sign",sign);
////        headers.setContentType(MediaType.APPLICATION_JSON);
//        RestTemplate restTemplate = new RestTemplate();
//        HttpEntity httpEntity = new HttpEntity(null,headers);
//        String result = restTemplate.exchange("http://localhost:9999/sys/test2?id=&vccId=0000&password=cqt@1234",
//                HttpMethod.GET,httpEntity,String.class).getBody();
////        String body = HttpRequest.get("http://localhost:7001/test2?id=&vccId=0000&password=cqt@1234")
////                .header("X-TIMESTAMP",String.valueOf(timestamp))
////                .header("APP-KEY",appKey)
////                .header("X-Sign",sign)
////                .execute().body();
//        System.out.println(result);
//
//    }
//
//    public static String sign(Object body,Map<String,Object> urlParamsMap,long timestamp,String appKey,String appSecret) {
//        try {
//            Map<String,Object> paramMap = new TreeMap<>();
//            if(body != null){
//                Map bodyMap = null;
//                if(body instanceof String){
//                    bodyMap = objectMapper.readValue((String)body,Map.class);
//                }else if(body instanceof Map){
//                    bodyMap = (Map)body;
//                }else{
//                    String agrStr = objectMapper.writeValueAsString(body);
//                    bodyMap = objectMapper.readValue(agrStr,Map.class);
//                }
//                if(bodyMap!=null){
//                    paramMap.putAll(bodyMap);
//                }
//            }
//            if(urlParamsMap!=null){
//                paramMap.putAll(urlParamsMap);
//            }
//            paramMap.put("X-TIMESTAMP",String.valueOf(timestamp));
//            paramMap.put("X-APP-KEY",appKey);
//            paramMap.remove("_t");
//            System.out.println("paramMap:"+paramMap);
//            String paramStr = toJSONString(paramMap);
//            System.out.println("paramStr:"+paramStr);
//            String sign = HmacSHA256Signature.create().computeSignature(appSecret,paramStr);
//
//            return sign;
//        }catch (Exception e){
//            throw new RuntimeException(e);
//        }
//    }
//
//    public static String toJSONString(Object obj){
//        try {
//            return objectMapper.writeValueAsString(obj);
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//
//
//}
