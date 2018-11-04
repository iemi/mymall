package com.mymall.service;

import com.github.pagehelper.PageInfo;
import com.mymall.common.ServerResponse;
import com.mymall.vo.OrderVo;

import java.util.Map;

public interface IOrderService {
    ServerResponse pay(Integer userId, long orderNo, String path);
    ServerResponse aliCallBack(Map<String,String> params);
    ServerResponse queryOrderPayStatus(Integer userId,Long orderNo);

    ServerResponse<Object> createOrder(Integer userId , Integer shippingId);
    ServerResponse cancelOrder(Integer userId,Long orderNo);
    ServerResponse getOrderCartProduct(Integer userId);
    ServerResponse getDetai(Integer userId,Long orderNo);ServerResponse<PageInfo> list(Integer userId, Integer pageNum, Integer pageSize);

    ServerResponse<PageInfo> manageList(Integer pageNum,Integer pageSize);
    ServerResponse<OrderVo> manageGetDetai(Long orderNo);
    ServerResponse<String> sendGoods(Long orderNo);
}
