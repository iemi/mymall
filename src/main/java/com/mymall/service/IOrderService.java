package com.mymall.service;

import com.mymall.common.ServerResponse;

import java.util.Map;

public interface IOrderService {
    ServerResponse pay(Integer userId, long orderNo, String path);
    ServerResponse aliCallBack(Map<String,String> params);
    ServerResponse queryOrderPayStatus(Integer userId,Long orderNo);
}
