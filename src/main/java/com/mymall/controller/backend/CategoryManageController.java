package com.mymall.controller.backend;

import com.mymall.common.Const;
import com.mymall.common.ServerResponse;
import com.mymall.pojo.User;
import com.mymall.service.ICategoryService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * 后台分类管理
 */
@Controller
@RequestMapping("/manage/category")
public class CategoryManageController {

    @Autowired
    private ICategoryService iCategoryService;

    /**
     * 增加分类
     * @param session
     * @param categoryName
     * @param parentId
     * @return
     */
    @RequestMapping(value = "add_category.do")
    @ResponseBody
    public ServerResponse addCategory(HttpSession session,String categoryName,@RequestParam(defaultValue = "0") int parentId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        //校验用户是否是管理员
        if(user.getRole() == 0){
            return ServerResponse.createByErrorMessage("无权限");
        }
        return iCategoryService.addCategory(categoryName,parentId);
    }

    /**
     * 修改分类名
     * @param id
     * @param categoryName
     * @return
     */
    @RequestMapping("set_category_name.do")
    @ResponseBody
    public ServerResponse setCategoryName(HttpSession session,Integer id,String categoryName){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        //校验用户是否是管理员
        if(user.getRole() == 0){
            return ServerResponse.createByErrorMessage("无权限");
        }
        return iCategoryService.setCategoryName(id,categoryName);
    }

    /**
     * 获取当前分类的子节点（平级）
     * @param session
     * @param categoryId
     * @return
     */
    @RequestMapping("get_category.do")
    @ResponseBody
    public ServerResponse getCategory(HttpSession session,@RequestParam(defaultValue = "0") Integer categoryId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        //校验用户是否是管理员
        if(user.getRole() == 0){
            return ServerResponse.createByErrorMessage("无权限");
        }
        //查询子节点的信息 并且不递归 保持平级
        return iCategoryService.getChildParallelCategory(categoryId);
    }


    /**
     * 递归获取当前节点的所有子节点id
     * @param session
     * @param categoryId
     * @return
     */
    @RequestMapping("get_deep_category.do")
    @ResponseBody
    public ServerResponse getDeepCategory(HttpSession session,@RequestParam(defaultValue = "0") Integer categoryId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        //校验用户是否是管理员
        if(user.getRole() == 0){
            return ServerResponse.createByErrorMessage("无权限");
        }
        //查询子节点的id 递归
        return iCategoryService.getDeepCategory(categoryId);
    }
}
