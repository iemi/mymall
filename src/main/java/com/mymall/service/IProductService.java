package com.mymall.service;

import com.github.pagehelper.PageInfo;
import com.mymall.common.ServerResponse;
import com.mymall.pojo.Product;
import com.mymall.vo.ProductDetailVo;
import org.springframework.web.bind.annotation.RequestParam;


public interface IProductService {

    ServerResponse productSave(Product product);

    ServerResponse setSaleStatus(Integer productId,Integer status);

    ServerResponse<ProductDetailVo> manageProductDetail(Integer productId);

    ServerResponse<PageInfo> getProductList(Integer pageNum,Integer pageSize);

    ServerResponse<PageInfo> searchProduct(String productName,Integer productId,Integer pageNum,Integer pageSize);

    ServerResponse<ProductDetailVo> productDetail(Integer productId);

    ServerResponse<PageInfo> portalSearchProduct(Integer categoryId, String keyword, Integer pageNum, Integer pageSize, String orderBy);
}
