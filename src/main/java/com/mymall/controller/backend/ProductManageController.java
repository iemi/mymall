package com.mymall.controller.backend;

import com.google.common.collect.Maps;
import com.mymall.common.Const;
import com.mymall.common.ServerResponse;
import com.mymall.pojo.Product;
import com.mymall.pojo.User;
import com.mymall.service.IFileService;
import com.mymall.service.IProductService;
import com.mymall.service.IUserService;
import com.mymall.util.PropertiesUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * 产品后台
 */
@Controller
@RequestMapping("/manage/product")
public class ProductManageController {


    @Autowired
    private IUserService iUserService;

    @Autowired
    private IProductService iProductService;

    @Autowired
    private IFileService iFileService;

    /**
     * 添加或者更新产品
     *
     * @param session
     * @param product
     * @return
     */
    @RequestMapping("save.do")
    @ResponseBody
    public ServerResponse productSave(HttpSession session, Product product) {

        User user = (User) session.getAttribute(Const.CURRENT_USER);

        if (user == null) {
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        if (user.getRole() == 0) {
            return ServerResponse.createByErrorMessage("无权限");
        }
        return iProductService.productSave(product);
    }


    /**
     * 更新产品上下架状态
     *
     * @param session
     * @param productId
     * @param status
     * @return
     */
    @RequestMapping("set_sale_status.do")
    @ResponseBody
    public ServerResponse productSave(HttpSession session, Integer productId, Integer status) {

        User user = (User) session.getAttribute(Const.CURRENT_USER);

        if (user == null) {
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        if (user.getRole() == 0) {
            return ServerResponse.createByErrorMessage("无权限");
        }

        return iProductService.setSaleStatus(productId, status);
    }

    /**
     * 获取商品详情
     *
     * @param session
     * @param productId
     * @return
     */
    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse getDetail(HttpSession session, Integer productId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);

        if (user == null) {
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        if (user.getRole() == 0) {
            return ServerResponse.createByErrorMessage("无权限");
        }
        return iProductService.manageProductDetail(productId);
    }

    /**
     * 获取产品list
     *
     * @return
     */
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse getProductList(HttpSession session, @RequestParam(defaultValue = "1") Integer pageNum, @RequestParam(defaultValue = "10") Integer pageSize) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);

        if (user == null) {
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        if (user.getRole() == 0) {
            return ServerResponse.createByErrorMessage("无权限");
        }
        return iProductService.getProductList(pageNum, pageSize);
    }

    /**
     * 查找产品
     *
     * @param session
     * @param productName
     * @param productId
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping("search.do")
    @ResponseBody
    public ServerResponse searchList(HttpSession session, String productName, Integer productId, @RequestParam(defaultValue = "1") Integer pageNum, @RequestParam(defaultValue = "10") Integer pageSize) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);

        if (user == null) {
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        if (user.getRole() == 0) {
            return ServerResponse.createByErrorMessage("无权限");
        }
        return iProductService.searchProduct(productName, productId, pageNum, pageSize);
    }


    /**
     * springmvc上传文件 并且上传到ftp服务器
     *
     * @param uploadFile
     * @param request
     * @return
     */
    @RequestMapping("upload.do")
    @ResponseBody
    public ServerResponse<Map> upload(HttpSession session, MultipartFile uploadFile, HttpServletRequest request) {

        User user = (User) session.getAttribute(Const.CURRENT_USER);

        if (user == null) {
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        if (user.getRole() == 0) {
            return ServerResponse.createByErrorMessage("无权限");
        }
//        if(uploadFile == null){
//            System.out.println("null");
//            return null;
//        }
        //这个路径是项目根路径，也就是webapp文件夹下
        String path = request.getSession().getServletContext().getRealPath("upload");
        String targetFileName = iFileService.upload(uploadFile, path);
        String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFileName;


        Map fileMap = Maps.newHashMap();
        fileMap.put("uri", targetFileName);
        fileMap.put("url", url);

        return ServerResponse.createBySuccess(fileMap);
    }


    /**
     * 富文本文件上传
     *
     * @param uploadFile
     * @param request
     * @return
     */
    @RequestMapping("richtext_img_upload.do")
    @ResponseBody
    public Map richtextImgUpload(HttpSession session, MultipartFile uploadFile, HttpServletRequest request, HttpServletResponse response) {

        //富文本中 对于返回值有自己的要求 使用simidtor要求进行返回//
//            {"success": true/false,
//                "msg": "error message", # optional
//            "file_path": "[real file path]"
//        }
        Map result = Maps.newHashMap();
        User user = (User) session.getAttribute(Const.CURRENT_USER);

        if (user == null) {
            result.put("success",false);
            result.put("msg","用户未登录，上传失败");
//            return ServerResponse.createByErrorMessage("用户未登录");
            return result;
        }

        if (user.getRole() == 0) {
            result.put("success",false);
            result.put("msg","无权限，上传失败");
//            return ServerResponse.createByErrorMessage("无权限");
            return result;
        }
        //这个路径是项目根路径，也就是webapp文件夹下
        String path = request.getSession().getServletContext().getRealPath("upload");
        String targetFileName = iFileService.upload(uploadFile, path);
        String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFileName;

        if(StringUtils.isBlank(targetFileName)){
            result.put("success",false);
            result.put("msg","上传失败");
        }

        response.addHeader("Access-Control-Allow-Headers","X-File-Name");
        result.put("success",true);
        result.put("msg","上传成功");
        result.put("file_path",url);
//        Map fileMap = Maps.newHashMap();
//        fileMap.put("uri", targetFileName);
//        fileMap.put("url", url);

        return result;
    }
}
