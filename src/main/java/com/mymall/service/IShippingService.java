package com.mymall.service;

import com.github.pagehelper.PageInfo;
import com.mymall.common.ServerResponse;
import com.mymall.pojo.Shipping;

public interface IShippingService {

    ServerResponse add(Integer userId,Shipping shipping);
    ServerResponse del(Integer shippingId,Integer userId);
    ServerResponse update(Shipping shipping,Integer userId);
    ServerResponse<Shipping> select(Integer shippingId,Integer userId);
    ServerResponse<PageInfo> list(Integer pageNum , Integer pageSize,Integer userId);
}
