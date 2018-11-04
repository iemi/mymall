package com.mymall.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mymall.common.ServerResponse;
import com.mymall.dao.CategoryMapper;
import com.mymall.pojo.Category;
import com.mymall.service.ICategoryService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;

@Service("iCategoryService")
public class CategorySereviceImpl implements ICategoryService{

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 增加分类
     * @param categoryName
     * @param parentId
     * @return
     */
    public ServerResponse addCategory (String categoryName,Integer parentId){
        if(parentId == null || StringUtils.isBlank(categoryName)){
            return ServerResponse.createByErrorMessage("请求参数错误");
        }

        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true);

        int rowCount = categoryMapper.insert(category);
        if(rowCount != 0){
            return ServerResponse.createBySuccessMessage("添加品类成功");
        }
        return ServerResponse.createByErrorMessage("添加品类失败");
    }

    /**
     * 修改分类名
     * @param categoryId
     * @param categoryName
     * @return
     */
    public ServerResponse setCategoryName(Integer categoryId,String categoryName){
        if(categoryId == null || StringUtils.isBlank(categoryName)){
            return ServerResponse.createByErrorMessage("请求参数错误");
        }

        Category category = new Category();
        category.setId(categoryId);
        category.setName(categoryName);

        int rowCount = categoryMapper.updateByPrimaryKeySelective(category);
        if(rowCount != 0){
            return ServerResponse.createBySuccessMessage("更新品类名字成功");
        }
        return ServerResponse.createByErrorMessage("更新品类名字失败");
    }

    /**
     * 获取当前分类的子分类
     * @param categoryId
     * @return
     */
    public ServerResponse<List<Category>> getChildParallelCategory(Integer categoryId){
        //获取当前分类名的子分类
        List<Category> categoryList = categoryMapper.selectCategoryByParentId(categoryId);
        if(CollectionUtils.isEmpty(categoryList)){
            return ServerResponse.createByErrorMessage("未找到子分类");
        }
        return ServerResponse.createBySuccess(categoryList);
    }

    /**
     * 递归查询子节点
     * @param categoryId
     * @return
     */
    public ServerResponse<List<Integer>> getDeepCategory(Integer categoryId){
        Set<Category> categorySet = Sets.newHashSet();
        findChildCategory(categorySet,categoryId);

        List<Integer> categoryList = Lists.newArrayList();
        if(categoryId != null){
            for(Category category : categorySet){
                categoryList.add(category.getId());
            }
        }
        return ServerResponse.createBySuccess(categoryList);
    }

    //递归查询当前节点以及子节点
    private Set<Category> findChildCategory(Set<Category> categorySet,Integer categoryId){
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if(category != null){
            categorySet.add(category);
        }
        //找出当前节点的子节点
        List<Category> categoryList = categoryMapper.selectCategoryByParentId(categoryId);
        for(Category categoryItem : categoryList){
            //进入递归
            findChildCategory(categorySet,categoryItem.getId());
        }
        return categorySet;
    }
}
