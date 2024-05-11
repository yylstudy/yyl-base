//package com.linkcircle.gateway.config;
//
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * @author yang.yonglian
// * @version 1.0.0
// * @Description TODO
// * @createTime 2023/5/6 13:52
// */
//@Component
//public class SignIncludePath {
//    private volatile static List<String> allPathList;
//    @Autowired
//    private SignIncludePathConfig signIncludePathConfig;
//
//    public List<String> getAllPathList(){
//        if(allPathList==null){
//            synchronized (SignIncludePath.class){
//                if(allPathList==null){
//                    allPathList = new ArrayList();
//                    addDefaultExcludePath(allPathList);
//                    allPathList.addAll(signIncludePathConfig.getPath());
//                }
//            }
//        }
//        return allPathList;
//    }
//
//    private void addDefaultExcludePath(List<String> list){
//        list.add("/sys/test1");
//        list.add("/sys/test2");
//    }
//}
