package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetmealService {
    /**
     * Batch delete setmeal
     * @param ids Setmeal ID list
     */
    void deleteBatch(List<Long> ids);

    // Add setmeal, save setmeal and dish relationship
    void saveWithDish(SetmealDTO setmealDTO);

    // Setmeal page query
    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    // Query by ID
    SetmealVO getByIdwithDish(Long id);

    // Modify setmeal
    void update(SetmealDTO setmealDTO);

    // Stop sale or start sale setmeal
    void startOrStop(Integer status, Long id);

    /**
     * Condition query
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * Query dish options by ID
     * @param id
     * @return
     */
    List<DishItemVO> getDishItemById(Long id);
}
