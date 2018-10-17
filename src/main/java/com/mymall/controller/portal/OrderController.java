package com.mymall.controller.portal;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.google.common.collect.Maps;
import com.mymall.common.Const;
import com.mymall.common.ResponseCode;
import com.mymall.common.ServerResponse;
import com.mymall.pojo.User;
import com.mymall.service.IOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Iterator;
import java.util.Map;

@Controller
@RequestMapping("/order")
public class OrderController {

    Logger logger  = LoggerFactory.getLogger(OrderController.class);

    //测试
    @Autowired
    private IOrderService iOrderService;

    @RequestMapping("pay.do")
    @ResponseBody
    public ServerResponse pay(HttpSession session, Long orderNo, HttpServletRequest request){

        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if(currentUser == null){
            return  ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        String path = request.getSession().getServletContext().getRealPath("upload");

        return iOrderService.pay(currentUser.getId(),orderNo,path);
    }

    @RequestMapping("alipay_callback.do")
    @ResponseBody
    public Object callBack(HttpServletRequest request){

        //存放请求数据
        Map<String,String> params = Maps.newHashMap();

        //获取请求中的Map 支付宝回调 会把数据存放在request的map中
        Map requestParams = request.getParameterMap();

        Iterator iterator = requestParams.keySet().iterator();
        while (iterator.hasNext()){
            String name = (String) iterator.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for(int i=0;i<values.length;i++){
                //数组中每个value值拼接时加上  ,
                valueStr = (i == values.length-1)?valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(name,valueStr);
        }

        logger.info("支付宝回调，sign:{},trade_status:{},参数:{}",params.get("sign"),params.get("trade_status"),params.toString());

        //移除sign_type参数
        params.remove("sign_type");
        //验证回调的正确性 是不是支付宝发的
        try {
            boolean alipayRSACheckedV2 = AlipaySignature.rsaCheckV2(params, Configs.getAlipayPublicKey(),"utf-8",Configs.getSignType());
            if(!alipayRSACheckedV2){
                //回调验签失败
                return ServerResponse.createByErrorMessage("非法请求，验证不通过");
            }
        } catch (AlipayApiException e) {
            logger.error("支付宝回调异常");
            e.printStackTrace();
        }

        //回调验签成功 todo 校验通知数据的正确性

        ServerResponse serverResponse = iOrderService.aliCallBack(params);
        if(serverResponse.isSuccess()){
            return Const.AlipayCallback.RESPONSE_SUCCESS;
        }
        return Const.AlipayCallback.RESPONSE_FAIL;
    }


    @RequestMapping("query_order_pay_status.do")
    @ResponseBody
    public ServerResponse queryOrderPayStatus(HttpSession session, Long orderNo, HttpServletRequest request){

        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if(currentUser == null){
            return  ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        ServerResponse serverResponse = iOrderService.queryOrderPayStatus(currentUser.getId(),orderNo);
        if(serverResponse.isSuccess()) {
            return ServerResponse.createBySuccess(true);
        }else{
            return ServerResponse.createBySuccess(false);
        }
    }
}
