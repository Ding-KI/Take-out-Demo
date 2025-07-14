package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
    // 根据菜品ID查询套餐ID
    List<Long> getSetmealIdbyDishIds(List<Long> dishIds);

    void insertBatch(List<SetmealDish> setmealDishes);

    // 根据套餐ID删除套餐菜品关系
    @Delete("delete from setmeal_dish where setmeal_id = #{setmealid}")
    void deleteBySetmealId(Long setmealid);

    // 根据套餐ID查询套餐和套餐菜品关系
    @Select("select * from setmeal_dish where setmeal_id = #{setmealId}")
    List<SetmealDish> getBySetmealId(Long id);
}
