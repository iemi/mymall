package com.mymall.service;

import com.mymall.common.ServerResponse;
import com.mymall.pojo.Category;

import java.util.List;

public interface ICategoryService {

    ServerResponse addCategory (String categoryName, Integer parentId);
    ServerResponse setCategoryName(Integer id,String categoryName);
    ServerResponse<List<Category>> getChildParallelCategory(Integer categoryId);
    ServerResponse<List<Integer>> getDeepCategory(Integer categoryId);
}
