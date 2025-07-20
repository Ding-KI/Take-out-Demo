package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j

public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {

        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);

        // Insert 1 dish into the dish table
        dishMapper.insert(dish);
        Long dishId = dish.getId(); // Get dish id
        // Insert multiple data into the dish flavor table - multiple flavors
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            // Loop through the flavor list, set the dish id of each flavor, Lambda expression
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId); // Set the dish id corresponding to the flavor
            });
            // Database insertion does not need to loop
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    // Page query dishes
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    // Batch delete dishes
    @Transactional
    public void deleteBatch(List<Long> ids) {
        // Check if the dish is in sale - if it is in sale, it cannot be deleted
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if(dish.getStatus()== StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        // Check if the dish is associated with the setmeal - if it is associated with the setmeal, it cannot be deleted
        List<Long> setmealIds = setmealDishMapper.getSetmealIdbyDishIds(ids);
        if(setmealIds != null && setmealIds.size() > 0){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        // Delete dish data in the dish table - batch delete
//        for (Long id : ids) {
//            dishMapper.deleteById(id);
//            // Delete flavor data associated with the flavor table
//            dishFlavorMapper.deleteByDishId(id);
//        }
        dishMapper.deleteByIds(ids);
        dishFlavorMapper.deleteByDishIds(ids);
    }

    // Query by ID
    @Transactional
    public DishVO getByIdWithFlavor(Long id) {
        // Query by ID
        Dish dish = dishMapper.getById(id);
        if (dish == null) {
            return null; // If the dish is not found, return null
        }

        // Query flavor data by dish ID
        List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishId(id);

        // Package the dish and flavor data into DishVO
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(dishFlavors);

        return dishVO;
    }

    // Modify dish information and flavor information by ID
    public void updateWithFlavor(DishDTO dishDTO) {
        // Update dish information
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.update(dish);

        // Delete existing flavor data
        dishFlavorMapper.deleteByDishId(dishDTO.getId());

        // Insert new flavor data
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            // Loop through the flavor list, set the dish id of each flavor, Lambda expression
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishDTO.getId()); // Set the dish id corresponding to the flavor
            });
            // Database insertion does not need to loop
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    // Query dishes by category ID
    public List<Dish> list(Long categoryId) {
        // builder
        Dish dish = Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE) // Only query dishes in enabled status
                .build();
        return dishMapper.list(dish);
    }

    /**
     * Condition query dishes and flavors
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            // Query flavor data by dish ID
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }

    // Stop sale or start sale dishes
    @Override
    public void startOrStop(Integer status, Long id) {
        Dish dish = Dish.builder()
                .id(id)
                .status(status) // Set dish status
                .build();
        dishMapper.update(dish);

        if(status == StatusConstant.DISABLE){
            // If the dish is stopped for sale, the setmeal corresponding to the current dish must also be stopped for sale
            List<Long> dishIds = new ArrayList<>();
            dishIds.add(id);
            // select setmeal_id from setmeal_dish where dish_id in (?,?,?)
            List<Long> setmealIds = setmealDishMapper.getSetmealIdbyDishIds(dishIds);
            if(setmealIds != null && setmealIds.size() > 0){
                for (Long setmealId : setmealIds) {
                    // Stop sale setmeal
                    Setmeal setmeal = Setmeal.builder()
                            .id(setmealId)
                            .status(StatusConstant.DISABLE) // Set setmeal status to stop sale
                            .build();
                    setmealMapper.update(setmeal);
                }
            }
        }
    }

}
