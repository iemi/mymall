package com.mymall.service;

import com.mymall.common.ServerResponse;
import com.mymall.vo.CartVo;

public interface ICartService {
    ServerResponse<CartVo> add(Integer userId , Integer count , Integer productId);
    ServerResponse<CartVo> update(Integer userId ,Integer productId , Integer count );
    ServerResponse<CartVo> delete(Integer userId ,String productIds);
    ServerResponse<CartVo> list(Integer userId);
    ServerResponse<CartVo> checkOrUncheck(Integer userId,Integer productId , Integer checked);
    ServerResponse<Integer> getCartProductCount(Integer userId);
}
