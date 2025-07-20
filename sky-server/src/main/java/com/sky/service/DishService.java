package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {

    // Add dish and save flavor information
    public void saveWithFlavor(DishDTO dishDTO);

    // Page query dishes
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    // Batch delete dishes
    void deleteBatch(List<Long> ids);

    // Query by ID
    DishVO getByIdWithFlavor(Long id);

    // Modify dish information and flavor information by ID
    void updateWithFlavor(DishDTO dishDTO);

    // Query the number of dishes by category ID
    List<Dish> list(Long categoryId);

    // Condition query dishes and flavors
    List<DishVO> listWithFlavor(Dish dish);

    // Dish stop sale or start sale
    void startOrStop(Integer status, Long id);
}

