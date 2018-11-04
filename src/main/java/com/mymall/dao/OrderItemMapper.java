package com.mymall.dao;

import com.mymall.pojo.OrderItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderItemMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(OrderItem record);

    int insertSelective(OrderItem record);

    OrderItem selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(OrderItem record);

    int updateByPrimaryKey(OrderItem record);

    List<OrderItem> selectByUserIdAndOrderNo(@Param("userId") Integer userId , @Param("orderNo") long orderNo);

    List<OrderItem> selectOrderNo(long orderNo);


    //批量插入
    void batchInsert(@Param("orderItemList") List<OrderItem> orderItemList);
}