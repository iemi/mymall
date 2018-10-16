package com.mymall.dao;

import com.mymall.pojo.Shipping;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ShippingMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Shipping record);

    int insertSelective(Shipping record);

    Shipping selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Shipping record);

    int updateByPrimaryKey(Shipping record);

    int deleteByShippingIdAndUserId(@Param("shippingid") Integer shippingid,@Param("userId") Integer userId);

    int updateByShippingIdAndUserId(Shipping shipping);

    Shipping selectByShippingIdAndUserId(@Param("shippingid") Integer shippingid,@Param("userId") Integer userId);

    List<Shipping> getAllShippingByUserId(Integer userId);
}