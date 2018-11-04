package com.mymall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.mymall.common.Const;
import com.mymall.common.ResponseCode;
import com.mymall.common.ServerResponse;
import com.mymall.dao.CategoryMapper;
import com.mymall.dao.ProductMapper;
import com.mymall.pojo.Category;
import com.mymall.pojo.Product;
import com.mymall.service.ICategoryService;
import com.mymall.service.IProductService;
import com.mymall.util.PropertiesUtil;
import com.mymall.vo.ProductDetailVo;
import com.mymall.vo.ProductListVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Service("iProductService")
public class ProductServiceImpl implements IProductService{

    @Autowired
    private ProductMapper productMapper;


    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private ICategoryService iCategoryService;
    /**
     * 增加或者更新产品
     * @param product
     * @return
     */
    public ServerResponse productSave(Product product){
        if(product == null){
            return ServerResponse.createByErrorMessage("参数错误");
        }

        //设置主图
        if(StringUtils.isNotBlank(product.getSubImages())){
            String[] subImgs = product.getSubImages().split(",");
            product.setMainImage(subImgs[0]);
        }

        if(product.getId() == null){
            //添加产品
            int rowCount = productMapper.insert(product);
            if(rowCount > 0){
                return ServerResponse.createBySuccess("新增产品成功");
            }else{
                return ServerResponse.createByErrorMessage("新增产品失败");
            }
        }

        //更新产品
        int rowCount = productMapper.updateByPrimaryKeySelective(product);
        if(rowCount > 0){
            return ServerResponse.createBySuccess("更新产品成功");
        }else{
            return ServerResponse.createByErrorMessage("更新产品失败");
        }
    }


    /**
     * 更改产品状态
     * @param productId
     * @param status
     * @return
     */
    public ServerResponse setSaleStatus(Integer productId,Integer status){
        if(productId == null || status == null){
            return ServerResponse.createByErrorMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);

        int rowCount = productMapper.updateByPrimaryKeySelective(product);
        if(rowCount > 0){
            return ServerResponse.createBySuccessMessage("修改产品状态成功");
        }
        return ServerResponse.createBySuccessMessage("修改产品状态失败");
    }


    /**
     * 获取产品详细信息 后台
     * @param productId
     * @return
     */
    public ServerResponse<ProductDetailVo> manageProductDetail(Integer productId){

        if(productId == null){
            return ServerResponse.createByErrorMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        Product product = productMapper.selectByPrimaryKey(productId);
        if(product == null){
            return ServerResponse.createByErrorMessage("商品以下架或者删除");
        }
        ProductDetailVo productDetailVo = assembleProductDetailVo(product);
        return ServerResponse.createBySuccess(productDetailVo);
    }

    /**
     * 获取productVo对象
     */
    private ProductDetailVo assembleProductDetailVo(Product product){
        ProductDetailVo productDetailVo = new ProductDetailVo();
        productDetailVo.setId(product.getId());
        productDetailVo.setSubtitle(product.getSubtitle());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setSubImages(product.getSubImages());
        productDetailVo.setCategoryId(product.getCategoryId());
        productDetailVo.setDetail(product.getDetail());
        productDetailVo.setName(product.getName());
        productDetailVo.setStatus(product.getStatus());
        productDetailVo.setStock(product.getStock());

        productDetailVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://img.happymmall.com/"));

        Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        if(category == null){
            productDetailVo.setParentCategoryId(0);
        }
        productDetailVo.setParentCategoryId(category.getParentId());

        //日期格式化
        SimpleDateFormat format  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        productDetailVo.setCreateTime(format.format(product.getCreateTime()));
        productDetailVo.setUpdateTime(format.format(product.getUpdateTime()));

        return productDetailVo;
    }


    /**
     * 利用pageHelper获取产品list
     * @param pageNum
     * @param pageSize
     * @return
     */
    public ServerResponse<PageInfo> getProductList(@RequestParam(defaultValue = "1") Integer pageNum, @RequestParam(defaultValue = "10") Integer pageSize){

        //pageHelper--starPage
        //填充sql查询逻辑
        //pageHelper--收尾
        PageHelper.startPage(pageNum,pageSize);//pageNum为起始页，pageSize为每页的容量

        //虽然我们需要的是productVoList，但是因为pageHelper是对dao层在执行mapper的时候才会动态分页，所以我们要先执行一下mapper
        List<Product> productList = productMapper.selectList();
        List<ProductListVo> productListlVoList = Lists.newArrayList();
        for(Product product : productList){
            ProductListVo productListVo = assembleProductListVo(product);
            productListlVoList.add(productListVo);
        }

        //初始化参数必须是dao层执行mapper返回的对应类型，List<Product>，才能触发分页
        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListlVoList);

        return ServerResponse.createBySuccess(pageInfo);
    }

    /**
     * 获取ProductListVo对象
     * @param product
     * @return
     */
    private ProductListVo assembleProductListVo(Product product){
        ProductListVo productListVo = new ProductListVo();
        productListVo.setId(product.getId());
        productListVo.setName(product.getName());
        productListVo.setCategoryId(product.getCategoryId());
        productListVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://img.happymmall.com/"));
        productListVo.setMainImage(product.getMainImage());
        productListVo.setPrice(product.getPrice());
        productListVo.setSubtitle(product.getSubtitle());
        productListVo.setStatus(product.getStatus());
        return productListVo;
    }


    /**
     * 查找产品 并进行分页 后台
     * @param productName
     * @param productId
     * @param pageNum
     * @param pageSize
     * @return
     */
    public ServerResponse<PageInfo> searchProduct(String productName,Integer productId,Integer pageNum,Integer pageSize){
        PageHelper.startPage(pageNum,pageSize);
        if(StringUtils.isNotBlank(productName)){
            productName = new StringBuilder().append("%").append(productName).append("%").toString();
        }

        List<Product> productList = productMapper.selectListByProductNameAndProductId(productName,productId);
        List<ProductListVo> productListVoList = Lists.newArrayList();
        for (Product product:
             productList) {
            ProductListVo productListVo = assembleProductListVo(product);
            productListVoList.add(productListVo);
        }
        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListVoList);

        return ServerResponse.createBySuccess(pageInfo);
    }



    /**
     * 获取产品详细信息
     * @param productId
     * @return
     */
    public ServerResponse<ProductDetailVo> productDetail(Integer productId){

        if(productId == null){
            return ServerResponse.createByErrorMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        Product product = productMapper.selectByPrimaryKey(productId);
        if(product == null){
            return ServerResponse.createByErrorMessage("商品以下架或者删除");
        }
        //如果商品不处于在售状态
        if(product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode()){
            return ServerResponse.createByErrorMessage("商品以下架或者删除");
        }
        ProductDetailVo productDetailVo = assembleProductDetailVo(product);
        return ServerResponse.createBySuccess(productDetailVo);
    }

    /**
     * 查找产品 并进行分页 前台
     * @param categoryId
     * @param keyword
     * @param pageNum
     * @param pageSize
     * @param orderBy
     * @return
     */
    public ServerResponse<PageInfo> portalSearchProduct(Integer categoryId, String keyword, Integer pageNum, Integer pageSize, String orderBy){

        //分类和关键字都没有传入
        if(categoryId == null && StringUtils.isBlank(keyword)){
            return ServerResponse.createByErrorMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        //存放category 所有类别节点id
        List<Integer> categoryIds = new ArrayList<Integer>();
        if(categoryId != null){
            Category category = categoryMapper.selectByPrimaryKey(categoryId);
            if(category == null && StringUtils.isBlank(keyword)){
                //没有找到这个分类，并且没有关键字,不报错，返回空结果集
                PageHelper.startPage(pageNum,pageSize);
                List<ProductListVo> productListVoList = Lists.newArrayList();
                PageInfo pageInfo = new PageInfo(productListVoList);
                return ServerResponse.createBySuccess(pageInfo);
            }
            //递归出分类所有子分类id
            categoryIds = iCategoryService.getDeepCategory(categoryId).getData();
        }

        //模糊查找keyword字符串
        if(StringUtils.isNotBlank(keyword)){
            keyword = new StringBuilder().append("%").append(keyword).append("%").toString();
        }

        PageHelper.startPage(pageNum,pageSize);

        //排序处理
        if(StringUtils.isNotBlank(orderBy)){
            if(Const.OrderBy.PRICE_ASC_DESC.contains(orderBy)){
                String[] orderBys = orderBy.split("_");
                //利用PageHelper进行排序
                PageHelper.orderBy(orderBys[0]+" "+orderBys[1]);
            }
        }

        List<Product> productList = productMapper.selectListByProductNameAndCategoryIds(StringUtils.isBlank(keyword)?null:keyword ,
                categoryIds.size() == 0?null:categoryIds);

        List<ProductListVo> productListVoList= Lists.newArrayList();
        for(Product product : productList){
            ProductListVo productListVo = assembleProductListVo(product);
            productListVoList.add(productListVo);
        }

        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListVoList);

        return ServerResponse.createBySuccess(pageInfo);

    }
//    public static void main(String[] args) {
//        System.out.println(new Date());
//        SimpleDateFormat format  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        System.out.println(format.format(new Date()));
//    }
}
