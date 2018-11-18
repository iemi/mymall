package com.mymall.service.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mymall.common.Const;
import com.mymall.common.ResponseCode;
import com.mymall.common.ServerResponse;
import com.mymall.dao.CartMapper;
import com.mymall.dao.ProductMapper;
import com.mymall.pojo.Cart;
import com.mymall.pojo.Product;
import com.mymall.service.ICartService;
import com.mymall.util.BigDecimalUtil;
import com.mymall.util.PropertiesUtil;
import com.mymall.vo.CartProductVo;
import com.mymall.vo.CartVo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartSereviceImpl implements ICartService{

    @Autowired
    CartMapper cartMapper;

    @Autowired
    ProductMapper productMapper;

    /**
     * 添加商品到购物车
     * @param userId
     * @param count
     * @param productId
     * @return
     */
    public ServerResponse<CartVo> add(Integer userId ,Integer productId , Integer count ){

        if(productId == null || count == null){
            return ServerResponse.createByErrorMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        Cart cart = cartMapper.selectCartByUserIdAndProductId(userId,productId);
        if(cart == null){
            //购物车中不存在该商品
            Cart cartItem = new Cart();
            cartItem.setProductId(productId);
            cartItem.setUserId(userId);
            cartItem.setQuantity(count);
            cartItem.setChecked(Const.Cart.CHECKED);
            cartMapper.insert(cartItem);
        }else{
            //购物车中已经存在该商品
            count = cart.getQuantity() + count;
            cart.setQuantity(count);
            cartMapper.updateByPrimaryKeySelective(cart);
        }

        return list(userId);
    }

    /**
     * 更新购物车
     * @param userId
     * @param productId
     * @param count
     * @return
     */
    public ServerResponse<CartVo> update(Integer userId ,Integer productId , Integer count ){

        if(productId == null || count == null){
            return ServerResponse.createByErrorMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        Cart cart = cartMapper.selectCartByUserIdAndProductId(userId,productId);
        if(cart == null){
            return ServerResponse.createByErrorMessage("购物车不存在该商品");
        }
        cart.setQuantity(count);
        cartMapper.updateByPrimaryKeySelective(cart);

        return list(userId);
    }


    /**
     * 删除购物车商品
     * @param userId
     * @param productIds
     * @return
     */
    public ServerResponse<CartVo> delete(Integer userId ,String productIds){
        //已经和前端约定好实用,分割
        List<String> productList = Splitter.on(",").splitToList(productIds);
        if(CollectionUtils.isEmpty(productList)){
            return ServerResponse.createByErrorMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        cartMapper.deleteByUserIdAndProductId(userId,productList);
        return list(userId);
    }


    /**
     * 购物车列表
     * @param userId
     * @return
     */
    public ServerResponse<CartVo> list(Integer userId){

        CartVo cartVo = getCartVoLimit(userId);
        return ServerResponse.createBySuccess(cartVo);
    }

    /**
     * 全选或者全反选 或者单选和反选
     * @param userId
     * @return
     */
    public ServerResponse<CartVo> checkOrUncheck(Integer userId,Integer productId , Integer checked){

        cartMapper.checkOrUncheckAllProduct(userId,null,checked);
        return list(userId);
    }

    /**
     * 获取购车所有商品数量
     * @param userId
     * @param userId
     * @return
     */
    public ServerResponse<Integer> getCartProductCount(Integer userId){
        return ServerResponse.createBySuccess(cartMapper.getCartProductCount(userId));
    }


    /**
     * 获取CartVo对象
     */
    private CartVo getCartVoLimit(Integer userId){

        List<Cart> cartList = cartMapper.selectCartByUserId(userId);
        List<CartProductVo> cartProductVoList = Lists.newArrayList();
        BigDecimal cartTotalPrice = new BigDecimal("0");//购物车总金额

        if(CollectionUtils.isNotEmpty(cartList)){
            for(Cart cart : cartList){
                CartProductVo cartProductVo = new CartProductVo();
                cartProductVo.setId(cart.getId());
                cartProductVo.setUserId(userId);
                cartProductVo.setProductId(cart.getProductId());

                Product product = productMapper.selectByPrimaryKey(cart.getProductId());
                if(product != null){
                    cartProductVo.setProductName(product.getName());
                    cartProductVo.setProductSubtitle(product.getSubtitle());
                    cartProductVo.setProductMainImage(product.getMainImage());
                    cartProductVo.setProductStatus(product.getStatus());
                    cartProductVo.setProductPrice(product.getPrice());
                    cartProductVo.setProductStock(product.getStock());

                    //判断库存
                    int buyLimitCount = 0;
                    if(product.getStock() >= cart.getQuantity()){
                        //库存充足
                        buyLimitCount = cart.getQuantity();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);
                    }else{
                        buyLimitCount = product.getStock();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);
                        Cart cartForQuanlity = new Cart();
                        cartForQuanlity.setId(cart.getId());
                        cartForQuanlity.setQuantity(product.getStock());
                        cartMapper.updateByPrimaryKeySelective(cartForQuanlity);
                    }
                    cartProductVo.setQuantity(buyLimitCount);
                    //设置当前物品总金额
                    cartProductVo.setProductTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(),cartProductVo.getQuantity()));
                    cartProductVo.setProductChecked(cart.getChecked());

                    if(cart.getChecked() == Const.Cart.CHECKED){
                        cartTotalPrice = BigDecimalUtil.add(cartProductVo.getProductTotalPrice().doubleValue(),cartTotalPrice.doubleValue());
                    }
                }
                cartProductVoList.add(cartProductVo);
            }
        }

        CartVo cartVo = new CartVo();
        cartVo.setCartProductVoList(cartProductVoList);
        cartVo.setCartTotalPrice(cartTotalPrice);
        cartVo.setAllChecked(getAllCheckedStatus(userId));
        cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

        return cartVo;

    }

    /**
     * 判断是否所有商品都处于勾选状态
     * @param userId
     * @return
     */
    private boolean getAllCheckedStatus(Integer userId){
        if(userId == null){
            return false;
        }
        return cartMapper.getAllCheckedStatus(userId) == 0;
    }

}
