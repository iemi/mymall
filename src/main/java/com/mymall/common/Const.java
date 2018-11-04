package com.mymall.common;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * 常量类
 */
public class Const{

    public final static String CURRENT_USER = "currentUser";//session存放的user

    public static final String USERNMAE = "username";
    public static  final String EMAIL = "email";

    //用户角色
    public interface Role{
        int ROLE_CUSTOMER = 0;//普通用户
        int ROLE_ADMIN = 1;//管理员
    }

    //购物车选中是否 和是否超出库存
    public interface Cart {
        int CHECKED = 1;
        int UN_CHECKED = 0;
        String LIMIT_NUM_SUCCESS = "LIMIT_NUM_SUCCESS";
        String LIMIT_NUM_FAIL = "LIMIT_NUM_FAIL";
    }

    //排序规则
    public interface OrderBy{
        Set<String> PRICE_ASC_DESC = Sets.newHashSet("price_desc","price_asc");
    }

    //产品状态
    public enum ProductStatusEnum{

        ON_SALE(1,"在售");


        private int code;
        private String value;

        ProductStatusEnum(int code , String value){
            this.value = value;
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }
    }

    public enum OrderStatusEnum{
        CANCELED(0,"以取消"),
        NO_PAY(10,"未付款"),
        PAID(20,"以付款"),
        SHIPPED(40,"以发货"),
        ORDER_SUCCESS(50,"交易成功"),
        ORDER_CLOSE(60,"交易关闭")
        ;
        int code;
        String value;

        OrderStatusEnum(int code, String value) {
            this.code = code;
            this.value = value;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public static OrderStatusEnum getEnumByCode(int code){
            for(OrderStatusEnum orderStatusEnum : values()){
                if(orderStatusEnum.getCode() == code){
                    return orderStatusEnum;
                }
            }
            throw new RuntimeException("么有找到对应的枚举");
        }
    }

    //支付宝回调 交易状态
    public interface AlipayCallback{
        String TRADE_STATUS_WAIT_BUYER_PAY = "WAIT_BUYER_PAY";
        String TRADE_STATUS_TRADE_SUCCESS = "TRADE_SUCCESS";

        String RESPONSE_SUCCESS = "success";
        String RESPONSE_FAIL = "failed";
    }

    //支付平台
    public enum TradePlatformEnum{
        ALIPAY(1,"支付宝")
        ;

        int code;
        String value;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        TradePlatformEnum(int code, String value) {

            this.code = code;
            this.value = value;
        }
    }

    //支付方式
    public enum PaymentTypeEnum{
        ONLINE(1,"在线支付")
        ;

        int code;
        String value;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        PaymentTypeEnum(int code, String value) {

            this.code = code;
            this.value = value;
        }

        public static PaymentTypeEnum codeOf(int code){
            for(PaymentTypeEnum paymentTypeEnum : values()){
                if(paymentTypeEnum.getCode() == code){
                    return paymentTypeEnum;
                }
            }
            throw new RuntimeException("么有找到对应的枚举");
        }
    }
}
