package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
    // Query setmeal ID by dish ID
    List<Long> getSetmealIdbyDishIds(List<Long> dishIds);

    void insertBatch(List<SetmealDish> setmealDishes);

    // Delete setmeal dish relationship by setmeal ID
    @Delete("delete from setmeal_dish where setmeal_id = #{setmealid}")
    void deleteBySetmealId(Long setmealid);

    // Query setmeal and setmeal dish relationship by setmeal ID
    @Select("select * from setmeal_dish where setmeal_id = #{setmealId}")
    List<SetmealDish> getBySetmealId(Long id);
}
