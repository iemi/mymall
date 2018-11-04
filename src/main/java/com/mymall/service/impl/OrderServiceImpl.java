package com.mymall.service.impl;

import com.alipay.api.AlipayResponse;
import com.alipay.api.domain.SignResultValue;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mymall.common.Const;
import com.mymall.common.ServerResponse;
import com.mymall.dao.*;
import com.mymall.pojo.*;
import com.mymall.service.IOrderService;
import com.mymall.util.BigDecimalUtil;
import com.mymall.util.FTPUtil;
import com.mymall.util.PropertiesUtil;
import com.mymall.vo.OrderItemVo;
import com.mymall.vo.OrderProductVo;
import com.mymall.vo.OrderVo;
import com.mymall.vo.ShippingVo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class OrderServiceImpl implements IOrderService{

    Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private PayInfoMapper payInfoMapper;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ShippingMapper shippingMapper;


//    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    /**
     * 创建订单 清空购物车
     * @param userId
     * @param shippingId
     * @return
     */
    public ServerResponse<Object> createOrder(Integer userId , Integer shippingId){
        List<Cart> cartList = cartMapper.selectCheckedCartByUserId(userId);
        if(cartList == null){
            return ServerResponse.createByErrorMessage("购物车中未勾选商品");
        }

        //计算订单总价
        ServerResponse serverResponse = getCartOrderItem(userId,cartList);
        if(!serverResponse.isSuccess()){
            return serverResponse;
        }
        List<OrderItem> orderItemList = (List<OrderItem>) serverResponse.getData();
        BigDecimal payment = getOrderTotalPrice(orderItemList);

        //生成订单
        Order order = assembleOrder(userId,shippingId,payment);
        if(order == null){
            return  ServerResponse.createByErrorMessage("生成订单失败");
        }
        //更新OrderItem的订单号
        for(OrderItem orderItem : orderItemList){
            orderItem.setOrderNo(order.getOrderNo());
        }

        //mybatis批量插入
        orderItemMapper.batchInsert(orderItemList);

        //减少库存
        reduceStock(orderItemList);
        //清空购物车
        cleanCart(cartList);

        //返回数据给前端
        OrderVo orderVo = assembleOrderVo(order,orderItemList);
        return ServerResponse.createBySuccess(orderVo);
    }

    /**
     * 取消订单
     * @param orderNo
     * @return
     */
    public ServerResponse cancelOrder(Integer userId,Long orderNo){

        Order order = orderMapper.selectByUserIdAndOrderNo(userId,orderNo);
        if(order == null){
            return ServerResponse.createByErrorMessage("该用户没有此订单");
        }

        if(order.getStatus() > Const.OrderStatusEnum.PAID.getCode()){
            //已经付款
            return ServerResponse.createByErrorMessage("此订单已付款，无法被取消");
        }
        order.setStatus(Const.OrderStatusEnum.CANCELED.getCode());
        order.setCloseTime(new Date());
        orderMapper.updateByPrimaryKeySelective(order);

        return ServerResponse.createBySuccess();
    }

    /**
     * 获取购物车勾选商品信息详情
     * @param userId
     * @return
     */
    public ServerResponse getOrderCartProduct(Integer userId){

        OrderProductVo orderProductVo = new OrderProductVo();
        List<Cart> cartList = cartMapper.selectCartByUserId(userId);
        ServerResponse serverResponse = getCartOrderItem(userId,cartList);
        if(!serverResponse.isSuccess()){
            return serverResponse;
        }
        List<OrderItem> orderItemList  = (List<OrderItem>) serverResponse.getData();

        List<OrderItemVo> orderItemVoList = Lists.newArrayList();

        for(OrderItem orderItem : orderItemList){
            orderItemVoList.add(assembleOrderItemVo(orderItem));
        }

        BigDecimal payment = getOrderTotalPrice(orderItemList);

        orderProductVo.setOrderItemVoList(orderItemVoList);
        orderProductVo.setProductTotalPrice(payment);
        orderProductVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

        return ServerResponse.createBySuccess(orderProductVo);
    }

    /**
     * 获取订单详情
     * @param userId
     * @param orderNo
     * @return
     */
    public ServerResponse getDetai(Integer userId,Long orderNo){
        Order order = orderMapper.selectByUserIdAndOrderNo(userId,orderNo);
        if (order == null){
            return ServerResponse.createByErrorMessage("用户不存在该订单");
        }
        List<OrderItem> orderItemList = orderItemMapper.selectByUserIdAndOrderNo(userId,orderNo);
        OrderVo orderVo = assembleOrderVo(order,orderItemList);
        return ServerResponse.createBySuccess(orderVo);

    }

    /**
     * 获取所有订单列表
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return
     */
    public ServerResponse<PageInfo> list(Integer userId,Integer pageNum,Integer pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Order> orderList = orderMapper.selectByUserId(userId);
        List<OrderVo> orderVoList = assembleOrderVoList(orderList,userId);

        PageInfo result = new PageInfo(orderList);
        result.setList(orderVoList);

        return ServerResponse.createBySuccess(result);
    }

    //backend
    public ServerResponse<PageInfo> manageList(Integer pageNum,Integer pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Order> orderList = orderMapper.selectAllOrder();
        List<OrderVo> orderVoList = assembleOrderVoList(orderList,null);

        PageInfo result = new PageInfo(orderList);
        result.setList(orderVoList);

        return ServerResponse.createBySuccess(result);
    }

    public ServerResponse<OrderVo> manageGetDetai(Long orderNo){
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null){
            return ServerResponse.createByErrorMessage("不存在该订单");
        }
        List<OrderItem> orderItemList = orderItemMapper.selectOrderNo(orderNo);
        OrderVo orderVo = assembleOrderVo(order,orderItemList);
        return ServerResponse.createBySuccess(orderVo);

    }

    public ServerResponse<String> sendGoods(Long orderNo){
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null){
            return ServerResponse.createByErrorMessage("不存在该订单");
        }
        if(order.getStatus() == Const.OrderStatusEnum.PAID.getCode()){
            order.setStatus(Const.OrderStatusEnum.SHIPPED.getCode());
            order.setSendTime(new Date());
            orderMapper.updateByPrimaryKeySelective(order);
            return ServerResponse.createBySuccess("发货成功");
        }
        return ServerResponse.createByErrorMessage("发货失败");

    }



    private List<OrderVo> assembleOrderVoList(List<Order> orderList,Integer userId){
        List<OrderVo> orderVoList = Lists.newArrayList();
        for(Order order : orderList){
            List<OrderItem> orderItemList = Lists.newArrayList();
            if(userId == null){
                //管理员
                orderItemList = orderItemMapper.selectOrderNo(order.getOrderNo());
            }else{
                orderItemList = orderItemMapper.selectByUserIdAndOrderNo(userId,order.getOrderNo());
            }
            OrderVo orderVo = assembleOrderVo(order,orderItemList);
            orderVoList.add(orderVo);
        }
        return orderVoList;
    }

    private OrderVo assembleOrderVo(Order order, List<OrderItem> orderItemList){
        OrderVo orderVo = new OrderVo();
        orderVo.setOrderNo(order.getOrderNo());
        orderVo.setPayment(order.getPayment());
        orderVo.setPaymentType(order.getPaymentType());
        orderVo.setPaymentTypeDesc(Const.OrderStatusEnum.getEnumByCode(order.getStatus()).getValue());

        orderVo.setPostage(order.getPostage());
        orderVo.setStatus(order.getStatus());
        orderVo.setStatusDesc(Const.PaymentTypeEnum.codeOf(order.getPaymentType()).getValue());

        orderVo.setShippingId(order.getShippingId());
        Shipping shipping = shippingMapper.selectByPrimaryKey(order.getShippingId());
        if(shipping != null){
            orderVo.setReceiverName(shipping.getReceiverName());
            orderVo.setShippingVo(assembleShippingVo(shipping));
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if(order.getPaymentTime() != null){
            orderVo.setPaymentTime(format.format(order.getPaymentTime()));
        }
        if(order.getSendTime() != null){
            orderVo.setSendTime(format.format(order.getSendTime()));        }
        if(order.getEndTime() != null){
            orderVo.setEndTime(format.format(order.getEndTime()));
        }
        if(order.getCloseTime() != null){
            orderVo.setCloseTime(format.format(order.getCloseTime()));
        }
        if(order.getCreateTime() != null){
            orderVo.setCreateTime(format.format(order.getCreateTime()));
        }


        orderVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));


        List<OrderItemVo> orderItemVoList = Lists.newArrayList();

        for(OrderItem orderItem : orderItemList){
            OrderItemVo orderItemVo = assembleOrderItemVo(orderItem);
            orderItemVoList.add(orderItemVo);
        }
        orderVo.setOrderItemVoList(orderItemVoList);
        return orderVo;
    }

    //组装OrderItemVo
    private OrderItemVo assembleOrderItemVo(OrderItem orderItem){
        OrderItemVo orderItemVo = new OrderItemVo();
        orderItemVo.setOrderNo(orderItem.getOrderNo());
        orderItemVo.setProductId(orderItem.getProductId());
        orderItemVo.setProductName(orderItem.getProductName());
        orderItemVo.setProductImage(orderItem.getProductImage());
        orderItemVo.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
        orderItemVo.setQuantity(orderItem.getQuantity());
        orderItemVo.setTotalPrice(orderItem.getTotalPrice());

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if(orderItem.getCreateTime() != null){
            orderItemVo.setCreateTime(format.format(orderItem.getCreateTime()));
        }
        return orderItemVo;
    }

    //组装shipingVo
    private ShippingVo assembleShippingVo(Shipping shipping){
        ShippingVo shippingVo = new ShippingVo();
        shippingVo.setReceiverName(shipping.getReceiverName());
        shippingVo.setReceiverAddress(shipping.getReceiverAddress());
        shippingVo.setReceiverProvince(shipping.getReceiverProvince());
        shippingVo.setReceiverCity(shipping.getReceiverCity());
        shippingVo.setReceiverDistrict(shipping.getReceiverDistrict());
        shippingVo.setReceiverMobile(shipping.getReceiverMobile());
        shippingVo.setReceiverZip(shipping.getReceiverZip());
        shippingVo.setReceiverPhone(shippingVo.getReceiverPhone());
        return shippingVo;
    }

    //减少商品库存
    private void reduceStock(List<OrderItem> orderItemList){
        for(OrderItem orderItem : orderItemList){
            Product product  = productMapper.selectByPrimaryKey(orderItem.getProductId());
            product.setStock(product.getStock() - orderItem.getQuantity());
            productMapper.updateByPrimaryKeySelective(product);
        }
    }

    //清空购物车
    private void cleanCart(List<Cart> cartList){
        for (Cart cart : cartList){
            cartMapper.deleteByPrimaryKey(cart.getId());
        }
    }

    //生成订单 插入数据库
    private Order assembleOrder(Integer userId,Integer shippingId,BigDecimal payment){
        Order order = new Order();
        long orderNo = generateOrderNo();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setShippingId(shippingId);
        order.setPayment(payment);
        order.setPaymentType(Const.PaymentTypeEnum.ONLINE.getCode());
        order.setPostage(0);//暂定为0
        order.setStatus(Const.OrderStatusEnum.NO_PAY.getCode());

        //付款时间
        //发货时间
        int rowCount = orderMapper.insert(order);
        if(rowCount > 0){
            return order;
        }
        return null ;
    }

    //生成订单号
    private long generateOrderNo(){
        long currentTime = System.currentTimeMillis();
        return currentTime + new Random().nextInt(100);
    }


    //计算订单总价
    private BigDecimal getOrderTotalPrice(List<OrderItem> orderItemList){
        BigDecimal payment = new BigDecimal("0");
        for (OrderItem orderItem : orderItemList){
            payment = BigDecimalUtil.add(payment.doubleValue(),orderItem.getTotalPrice().doubleValue());
        }
        return  payment;
    }


    //根据购物车中勾选的商品 返回订单详情
    private ServerResponse<List<OrderItem>> getCartOrderItem(Integer userId,List<Cart> cartList){

        List<OrderItem> orderItemList = Lists.newArrayList();

        if(CollectionUtils.isEmpty(cartList)){
            return ServerResponse.createByErrorMessage("购物车中未勾选商品");
        }

        for(Cart cart : cartList){
            OrderItem orderItem = new OrderItem();
            //从购物车中获取商品详情  通过productId
            Product product = productMapper.selectByPrimaryKey(cart.getProductId());
            //校验商品是否在售
            if(Const.ProductStatusEnum.ON_SALE.getCode() != product.getStatus()){
                return ServerResponse.createByErrorMessage("商品" + product.getName() + "不处于在售状态");
            }
            //校验商品库存
            if(product.getStock() <  cart.getQuantity()){
                return ServerResponse.createByErrorMessage("商品" + product.getName() + "库存不足");
            }

            //组装返回对象
            orderItem.setUserId(userId);
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setCurrentUnitPrice(product.getPrice());
            orderItem.setQuantity(cart.getQuantity());
            orderItem.setTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(),cart.getQuantity().doubleValue()));

            orderItemList.add(orderItem);
        }
        return ServerResponse.createBySuccess(orderItemList);
    }


    //支付部分
    public ServerResponse pay(Integer userId,long orderNo,String path){

        Map<String,String> resultMap = Maps.newHashMap();
        Order order = orderMapper.selectByUserIdAndOrderNo(userId,orderNo);
        if(order == null){
            return ServerResponse.createByErrorMessage("用户不存在该订单");
        }
        resultMap.put("orderNo",String.valueOf(orderNo));


        //设置向支付宝请求预下单的订单信息
        String outTradeNo = String.valueOf(orderNo);

        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店当面付扫码消费”
        String subject = new StringBuilder("mymall扫码支付,订单号为").append(outTradeNo).toString();

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = order.getPayment().toString();

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0";

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
        String body = new StringBuilder("订单").append(outTradeNo).append("购买商品").append(totalAmount).append("元").toString();

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId("2088100200300400500");

        // 支付超时，定义为120分钟
        String timeoutExpress = "120m";



        // 商品明细列表，需填写购买商品详细信息，
        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();
        //取出订单中所有商品的明细
        List<OrderItem> orderItemList = orderItemMapper.selectByUserIdAndOrderNo(userId, orderNo);
        for(OrderItem orderItem : orderItemList){
            // 创建一个商品信息，参数含义分别为商品id（使用国标）、名称、单价（单位为分）、数量，如果需要添加商品类别，详见GoodsDetail
            GoodsDetail goods = GoodsDetail.newInstance(orderItem.getProductId().toString(), orderItem.getProductName(),
                    BigDecimalUtil.mul(orderItem.getCurrentUnitPrice().doubleValue(),new Double(100).doubleValue()).longValue(), orderItem.getQuantity());
            // 创建好一个商品后添加至商品明细列表
            goodsDetailList.add(goods);
        }



        // 创建扫码支付请求builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                .setNotifyUrl(PropertiesUtil.getProperty("alipay.callback.url"))//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                .setGoodsDetailList(goodsDetailList);


        /** 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         *  Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
         */
        Configs.init("zfbinfo.properties");

        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         */
        AlipayTradeService tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();

        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                logger.info("支付宝预下单成功: )");

                AlipayTradePrecreateResponse response = result.getResponse();
                dumpResponse(response);

                //创建二维码文件储存路径
                File folder = new File(path);
                if(!folder.exists()){
                    folder.mkdirs();
                    folder.setWritable(true);
                }

                // 需要修改为运行机器上的路径
                String qrPath = String.format(path + "/qr-%s.png", response.getOutTradeNo());
                String qrFileName = String.format("qr-%s.png",response.getOutTradeNo());
                //生成二维码图片保存到quPath guawa提供
                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, qrPath);

                //上传二维码文件到FTP服务器
//                File targetFile = new File(path,qrFileName);
//                try {
//                    FTPUtil.uploadFile(Lists.newArrayList(targetFile));
//                } catch (IOException e) {
//                    logger.info("上传二维码异常" + e);
//                    e.printStackTrace();
//                }
//                logger.info("qrPath:" + qrPath);

                String qrUrl = PropertiesUtil.getProperty("ftp.server.http.prefix")+qrFileName;
                resultMap.put("qrUrl",qrUrl);

                return ServerResponse.createBySuccess(resultMap);

            case FAILED:
                logger.error("支付宝预下单失败!!!");
                return ServerResponse.createByErrorMessage("支付宝预下单失败");

            case UNKNOWN:
                logger.error("系统异常，预下单状态未知!!!");
                return ServerResponse.createByErrorMessage("系统异常，预下单状态未知");

            default:
                logger.error("不支持的交易状态，交易返回异常!!!");
                return ServerResponse.createByErrorMessage("不支持的交易状态，交易返回异常!!!");
        }
    }


    /**
     * 支付宝回调处理
     * @param params
     * @return
     */
    public ServerResponse aliCallBack(Map<String,String> params){

        String orderNo = params.get("out_trade_no");//我的订单号 也就是支付宝的外部订单号
        String tradeNo = params.get("trade_no");//支付宝交易号
        String tradeStatus = params.get("trade_status");//支付宝交易状态号

        Order order  = orderMapper.selectByOrderNo(Long.parseLong(orderNo));
        if(order == null){
            return ServerResponse.createByErrorMessage("我的商城不存在此订单号，忽略回调");
        }

        //判断数据库保存的交易状态
        if(order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()){
            //订单已经完成付款
            return ServerResponse.createBySuccess("支付宝重复回调");
        }

        //判断回调交易状态
        if(Const.AlipayCallback.TRADE_STATUS_TRADE_SUCCESS.equals(tradeStatus)){
            //回调交易成功
            try {
                order.setPaymentTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(params.get("gmt_payment")));
            } catch (ParseException e) {
                logger.error("日期转换失败"+e);
            }
            order.setStatus(Const.OrderStatusEnum.PAID.getCode());
            //更新数据库状态
            orderMapper.updateByPrimaryKeySelective(order);
        }

        PayInfo payInfo = new PayInfo();
        payInfo.setUserId(order.getUserId());
        payInfo.setOrderNo(order.getOrderNo());
        payInfo.setPayPlatform(Const.TradePlatformEnum.ALIPAY.getCode());
        payInfo.setPlatformNumber(tradeNo);//更新支付宝交易号
        payInfo.setPlatformStatus(tradeStatus);

        payInfoMapper.insert(payInfo);

        return ServerResponse.createBySuccess();

    }

    /**
     * 查询交易状态
     * @return
     */
    public ServerResponse queryOrderPayStatus(Integer userId,Long orderNo){
        Order order = orderMapper.selectByUserIdAndOrderNo(userId,orderNo);
        if(order == null){
            return ServerResponse.createByErrorMessage("我的商城不存在此订单号");
        }

        if(order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()){
            //订单已经完成付款
            return ServerResponse.createBySuccess();
        }
        return  ServerResponse.createByError();

    }

    // 简单打印应答
    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            logger.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                logger.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            logger.info("body:" + response.getBody());
        }
    }
}
