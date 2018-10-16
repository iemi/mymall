package com.mymall.controller.portal;

import com.mymall.common.Const;
import com.mymall.common.ResponseCode;
import com.mymall.common.ServerResponse;
import com.mymall.pojo.User;
import com.mymall.service.ICartService;
import com.mymall.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 **购物车控制器
 */
@Controller
@RequestMapping("/cart/")
public class CartController {


    @Autowired
    ICartService iCartService;

    /**
     *添加商品到购物车
     * @param session
     * @param productId
     * @param count
     * @return
     */
    @RequestMapping("add.do")
    @ResponseBody
    public ServerResponse<CartVo> add(HttpSession session , Integer productId , Integer count){

        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if(currentUser == null){
            return  ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        return iCartService.add(currentUser.getId(),productId,count);
    }

    @RequestMapping("update.do")
    @ResponseBody
    public ServerResponse<CartVo> update(HttpSession session , Integer productId , Integer count){

        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if(currentUser == null){
            return  ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        return iCartService.update(currentUser.getId(),productId,count);
    }

    @RequestMapping("delete_product.do")
    @ResponseBody
    public ServerResponse<CartVo> deleteProduct(HttpSession session , String productIds){

        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if(currentUser == null){
            return  ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        return iCartService.delete(currentUser.getId(),productIds);
    }

    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse<CartVo> list(HttpSession session){

        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if(currentUser == null){
            return  ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        return iCartService.list(currentUser.getId());
    }


    @RequestMapping("select_all.do")
    @ResponseBody
    public ServerResponse<CartVo> selectAll(HttpSession session){

        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if(currentUser == null){
            return  ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        return iCartService.checkOrUncheck(currentUser.getId(),null,Const.Cart.CHECKED);
    }

    @RequestMapping("un_select_all.do")
    @ResponseBody
    public ServerResponse<CartVo> unSelectAll(HttpSession session){

        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if(currentUser == null){
            return  ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        return iCartService.checkOrUncheck(currentUser.getId(),null,Const.Cart.UN_CHECKED);
    }

    @RequestMapping("select.do")
    @ResponseBody
    public ServerResponse<CartVo> Select(HttpSession session,Integer productId){

        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if(currentUser == null){
            return  ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        return iCartService.checkOrUncheck(currentUser.getId(),productId,Const.Cart.CHECKED);
    }

    @RequestMapping("un_select.do")
    @ResponseBody
    public ServerResponse<CartVo> unSelect(HttpSession session,Integer productId){

        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if(currentUser == null){
            return  ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        return iCartService.checkOrUncheck(currentUser.getId(),productId,Const.Cart.UN_CHECKED);
    }

    @RequestMapping("get_cart_product_count.do")
    @ResponseBody
    public ServerResponse<Integer> getCartProductCount(HttpSession session){

        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if(currentUser == null){
            return  ServerResponse.createBySuccess(0);
        }

        return iCartService.getCartProductCount(currentUser.getId());
    }

}
