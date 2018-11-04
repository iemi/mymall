package com.mymall.controller.backend;


import com.mymall.common.Const;
import com.mymall.common.ResponseCode;
import com.mymall.common.ServerResponse;
import com.mymall.pojo.User;
import com.mymall.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/manage/order")
public class OrderManegeController {

    @Autowired
    private IOrderService iOrderService;


    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse manageList(HttpSession session,
                                     @RequestParam(value="pageNum",defaultValue = "1") Integer pageNum,
                                     @RequestParam(value="pageSize",defaultValue = "10")Integer pageSize){
        User user = (User) session.getAttribute(Const.CURRENT_USER);

        if (user == null) {
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        if (user.getRole() == 0) {
            return ServerResponse.createByErrorMessage("无权限");
        }
        return iOrderService.manageList(pageNum,pageSize);
    }

    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse manageDetail(HttpSession session,Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);

        if (user == null) {
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        if (user.getRole() == 0) {
            return ServerResponse.createByErrorMessage("无权限");
        }
        return iOrderService.manageGetDetai(orderNo);
    }

    @RequestMapping("send_goods.do")
    @ResponseBody
    public ServerResponse sendGoods(HttpSession session,Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);

        if (user == null) {
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        if (user.getRole() == 0) {
            return ServerResponse.createByErrorMessage("无权限");
        }
        return iOrderService.sendGoods(orderNo);
    }
}
