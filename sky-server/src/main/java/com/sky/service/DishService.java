package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {

    // 新增菜品并保存口味信息
    public void saveWithFlavor(DishDTO dishDTO);

    // 分页查询菜品
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    // 批量删除菜品
    void deleteBatch(List<Long> ids);

    // 根据ID查询菜品以及口味数据
    DishVO getByIdWithFlavor(Long id);

    //根据id修改菜品信息以及口味信息
    void updateWithFlavor(DishDTO dishDTO);

    // 根据分类id查询菜品数量
    List<Dish> list(Long categoryId);

    //条件查询菜品和口味
    List<DishVO> listWithFlavor(Dish dish);

    //菜品停售或起售
    void startOrStop(Integer status, Long id);
}

