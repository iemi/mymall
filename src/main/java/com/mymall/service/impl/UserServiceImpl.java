package com.mymall.service.impl;

import com.mymall.common.Const;
import com.mymall.common.ServerResponse;
import com.mymall.common.TokenCache;
import com.mymall.dao.UserMapper;
import com.mymall.pojo.User;
import com.mymall.service.IUserService;
import com.mymall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

//注入UserController
@Service("iUserService")
public class UserServiceImpl implements IUserService {


    @Autowired
    private UserMapper userMapper;

    /**
     * 登录
     */
    @Override
    public ServerResponse<User> login(String username, String password) {

        int resultCount = userMapper.checkUsername(username);
        //表里找不到这个username
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("该用户不存在");
        }


        // MD5加密
        String md5Password = MD5Util.MD5EncodeUtf8(password);


        User user = userMapper.selectLogin(username, md5Password);
        if (user == null) {
            return ServerResponse.createByErrorMessage("密码错误");
        }

        //把密码设为空字符串
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess("登录成功", user);
    }

    /**
     * 注册
     */
    @Override
    public ServerResponse<String> register(User user) {

        //校验email
        ServerResponse<String> validResp = this.checkValid(user.getEmail(), Const.EMAIL);
        if (!validResp.isSuccess()) {
            return validResp;
        }

        //校验username
        validResp = this.checkValid(user.getUsername(), Const.USERNMAE);
        if (!validResp.isSuccess()) {
            return validResp;
        }

        //设置为用户
        user.setRole(Const.Role.ROLE_CUSTOMER);

        //MD5加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));

        //向数据表插入user
        int resultCount = userMapper.insert(user);
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("注册失败");
        }

        return ServerResponse.createBySuccessMessage("注册成功");
    }

    /**
     * 检查email和username
     *
     * @param str
     * @param type
     * @return
     */
    @Override
    public ServerResponse<String> checkValid(String str, String type) {
        if (StringUtils.isNotBlank(type)) {
            //开始校验
            if (Const.USERNMAE.equals(type)) {
                int resultCount = userMapper.checkUsername(str);
                if (resultCount != 0) {
                    return ServerResponse.createByErrorMessage("用户以存在");
                }
            }
            if (Const.EMAIL.equals(type)) {
                int resultCount = userMapper.checkEmail(str);
                if (resultCount != 0) {
                    return ServerResponse.createByErrorMessage("email以存在");
                }
            }
        } else {
            return ServerResponse.createByErrorMessage("参数错误");
        }
        return ServerResponse.createBySuccessMessage("校验成功");
    }

    /**
     * 获取用户信息 这个方法并没有和数据库进行交互  没必要写在service层中
     * @param session
     * @return
     */
//    @Override
//    public ServerResponse<User> getUserInfo(HttpSession session) {
//        //session中获取到当前user
//        User user = (User) session.getAttribute(Const.CURRENT_USER);
//        if(user == null){
//            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户信息");
//        }
//        return ServerResponse.createBySuccess(user);
//    }

    /**
     * 通过username获取question
     *
     * @param username
     * @return
     */
    public ServerResponse<String> selectQuestion(String username) {
        ServerResponse<String> checkResp = checkValid(username, Const.USERNMAE);
        if (checkResp.isSuccess()) {
            //username不存在
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        String question = userMapper.selectQuestionByUsername(username);
        if (StringUtils.isBlank(question)) {
            return ServerResponse.createByErrorMessage("找回密码的问题是空的");
        }
        return ServerResponse.createBySuccess(question);
    }

    /**
     * 校验问题答案
     *
     * @param username
     * @param question
     * @param answer
     * @return
     */
    public ServerResponse<String> checkAnswer(String username, String question, String answer) {
//        ServerResponse<String> checkResp = checkValid(username,Const.USERNMAE);
//        if(checkResp.isSuccess()){
//            //username不存在
//            return ServerResponse.createByErrorMessage("用户不存在");
//        }
        int resultCount = userMapper.checkAnswer(username, question, answer);
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("问题答案错误");
        }
        String forgetToken = UUID.randomUUID().toString();
        //token存放在本地缓存中
        TokenCache.setKey("token_" + username, forgetToken);
        return ServerResponse.createBySuccess(forgetToken);
    }

    /**
     * 忘记密码重置密码
     *
     * @param username
     * @param newPassword
     * @param forgetToekn
     * @return
     */
    public ServerResponse<String> forgetResetPassword(String username, String newPassword, String forgetToekn) {
        //必须传入token
        if (StringUtils.isBlank(forgetToekn)) {
            return ServerResponse.createByErrorMessage("参数错误，需要token参数");
        }
        //校验用户存在
        ServerResponse<String> checkResp = checkValid(username, Const.USERNMAE);
        if (checkResp.isSuccess()) {
            //username不存在
            return ServerResponse.createByErrorMessage("用户不存在");
        }

        //获取缓存中的token进行比较
        String token = TokenCache.getKey("token_" + username);
        if (StringUtils.isBlank(token)) {
            return ServerResponse.createByErrorMessage("token无效或者过期");
        }

        if (StringUtils.equals(token, forgetToekn)) {
            int rowCount = userMapper.updatePasswordByUsername(username, MD5Util.MD5EncodeUtf8(newPassword));
            if (rowCount > 0) {
                return ServerResponse.createBySuccessMessage("修改密码成功");
            }
        } else {
            return ServerResponse.createByErrorMessage("token错误，请重新获取token");
        }

        return ServerResponse.createByErrorMessage("修改密码失败");
    }


    /**
     * 登录状态下重置密码
     * @param oldPassword
     * @param newPassword
     * @param user
     * @return
     */
    public ServerResponse<String> resetPassword(String oldPassword, String newPassword, User user) {
        //防止横向越权，在校验旧密码时，必须制定当前user的id，否则旧密码有可能是其他user的密码，出现修改了其他用户密码的情况
        int resultCount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(oldPassword), user.getId());
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("旧密码错误");
        }

        user.setPassword(MD5Util.MD5EncodeUtf8(newPassword));
        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if (updateCount == 0) {
            return ServerResponse.createByErrorMessage("修改密码失败");
        }
        return ServerResponse.createByErrorMessage("修改密码成功");
    }

    /**
     * 更新用户信息
     * @param user
     * @return
     */
    public ServerResponse<User> updateInformation(User user) {
        //username不可以被更新
        //校验email，不可以有相同的email，并且这个相同的email不是当前这个用户的email
        int resultCount = userMapper.checkEmailByUserId(user.getEmail(), user.getId());
        if (resultCount != 0) {
            return ServerResponse.createByErrorMessage("email已经存在");
        }

        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());

        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if (updateCount > 0) {
            return ServerResponse.createBySuccess("更新个人信息成功", updateUser);
        }
        return ServerResponse.createByErrorMessage("更新个人信息失败");
    }

    /**
     * 获取当前用户信息
     * @param user
     * @return
     */
    public ServerResponse<User> getInformation(User user) {

        User resultUser = userMapper.selectByPrimaryKey(user.getId());
        if (resultUser != null) {
            return ServerResponse.createBySuccess(user);
        }
        return ServerResponse.createByErrorMessage("更新失败");
    }
}
