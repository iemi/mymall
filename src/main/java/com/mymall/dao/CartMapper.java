package com.mymall.dao;

import com.mymall.pojo.Cart;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CartMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Cart record);

    int insertSelective(Cart record);

    Cart selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Cart record);

    int updateByPrimaryKey(Cart record);

    Cart selectCartByUserIdAndProductId(@Param("userId") Integer userId, @Param("productId") Integer productId);

    List<Cart> selectCartByUserId(Integer userId);

    int getAllCheckedStatus(Integer userId);

    int deleteByUserIdAndProductId(@Param("userId") Integer userId, @Param("productIds") List<String> productId);

    int checkOrUncheckAllProduct(@Param("userId") Integer userId , @Param("productId") Integer productId , @Param("checked") Integer checked);

    int getCartProductCount(Integer userId);
}