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
     * 批量删除套餐
     * @param ids 套餐ID列表
     */
    void deleteBatch(List<Long> ids);

    // 新增套餐,保存套餐和菜品的关系
    void saveWithDish(SetmealDTO setmealDTO);

    // 套餐分页查询
    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    // 根据id查询套餐
    SetmealVO getByIdwithDish(Long id);

    //修改套餐
    void update(SetmealDTO setmealDTO);

    // 停售或起售套餐
    void startOrStop(Integer status, Long id);

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    List<DishItemVO> getDishItemById(Long id);
}
