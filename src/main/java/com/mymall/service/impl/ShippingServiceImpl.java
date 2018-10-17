package com.mymall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.mymall.common.ServerResponse;
import com.mymall.dao.ShippingMapper;
import com.mymall.pojo.Shipping;
import com.mymall.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ShippingServiceImpl implements IShippingService{


    @Autowired
    private ShippingMapper shippingMapper;
    /**
     * 添加收货地址
     * @param shipping
     * @return
     */
    public ServerResponse add(Integer userId,Shipping shipping){

        shipping.setUserId(userId);

        //返回值为shippingId
        int resultCount = shippingMapper.insert(shipping);
        if (resultCount > 0){
            Map resultMap = Maps.newHashMap();
            resultMap.put("shippingId",shipping.getId());
            return ServerResponse.createBySuccess("新建地址成功",resultMap);
        }
        return ServerResponse.createByErrorMessage("新建地址失败");
    }

    /**
     * 删除收货地址
     * @param shippingId
     * @return
     */
    public ServerResponse del(Integer shippingId,Integer userId){


        //防止横向越权问题 必须传userId进行删除
        int resultCount = shippingMapper.deleteByShippingIdAndUserId(shippingId,userId);
        if (resultCount > 0){
            return ServerResponse.createBySuccess("删除地址成功");
        }
        return ServerResponse.createByErrorMessage("删除地址失败");
    }

    /**
     * 更新收货地址
     * @param shipping
     * @return
     */
    public ServerResponse update(Shipping shipping,Integer userId){


        shipping.setUserId(userId);//防止横向越权
        int resultCount = shippingMapper.updateByShippingIdAndUserId(shipping);
        if (resultCount > 0){
            return ServerResponse.createBySuccess("更新地址成功");
        }
        return ServerResponse.createByErrorMessage("更新地址失败");
    }

    /**
     * 查看收货地址
     * @param shippingId
     * @return
     */
    public ServerResponse<Shipping> select(Integer shippingId,Integer userId){


        Shipping shipping = shippingMapper.selectByShippingIdAndUserId(shippingId,userId);
        if (shipping != null){
            return ServerResponse.createBySuccess(shipping);
        }
        return ServerResponse.createByErrorMessage("查看地址失败");
    }

    public ServerResponse<PageInfo> list(Integer pageNum , Integer pageSize,Integer userId){

        PageHelper.startPage(pageNum,pageSize);

        List<Shipping> shippingList = shippingMapper.getAllShippingByUserId(userId);

        PageInfo pageInfo = new PageInfo(shippingList);
        return ServerResponse.createBySuccess(pageInfo);
    }

}
