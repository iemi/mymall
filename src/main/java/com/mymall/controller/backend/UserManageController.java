package com.mymall.controller.backend;

import com.mymall.common.Const;
import com.mymall.common.ServerResponse;
import com.mymall.pojo.User;
import com.mymall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * 后台管理员controller
 */
@Controller
@RequestMapping("/manage/user")
public class UserManageController {

    @Autowired
    private IUserService iUserService;


    /**
     * 后台用户登录
     * @param username
     * @param password
     * @param session
     * @return
     */
    @RequestMapping(value = "login.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession session){
        ServerResponse<User> resp = iUserService.login(username,password);
        if(resp.isSuccess()){
            if(resp.getData().getRole() == 1){
                //当前用户是管理员
                session.setAttribute(Const.CURRENT_USER,resp.getData());
                return resp;
            }else{
                return ServerResponse.createByErrorMessage("不是管理员无法登录");
            }
        }
        return resp;
    }
}
