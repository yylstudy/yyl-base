package com.cqt.cdr.cloudccsfaftersales.service;

import com.cqt.cdr.cloudccsfaftersales.entity.dto.SFStatusReq;

import java.util.Map;
import java.util.concurrent.CompletableFuture;


public interface CtiWorkeventService {

    CompletableFuture<Map<String, String>> check(SFStatusReq sfStatusReq);
}


