package com.sky.service.impl;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j

public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Override
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {

        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);

        // 向菜品表插入1条数据-1个菜品
        dishMapper.insert(dish);
        Long dishId = dish.getId(); // 获取菜品id
        // 向菜品口味表插入多条数据-多个口味,
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            // 遍历口味列表，设置每个口味的菜品id，Lambda表达式
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId); // 设置口味对应的菜品id
            });
            //数据库插入不需要遍历
            dishFlavorMapper.insertBatch(flavors);
        }
    }
}
