package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DishMapper {

    /**
     * Query the number of dishes by category ID
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    /**
     * Insert dish
     * @param dish
     */
    @AutoFill(value= OperationType.INSERT)
    void insert(Dish dish);

    // Page query
    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    // Query by ID
    @Select("select * from dish where id = #{id}")
    Dish getById(Long id);

    // Delete dish
    @Delete("delete from dish where id = #{id}")
    void deleteById(Long id);

    // Delete dishes by ID collection
    void deleteByIds(List<Long> ids);

    // Modify dish information by ID
    @AutoFill(value= OperationType.UPDATE)
    void update(Dish dish);

    // Query dishes by category ID, dynamic query
    List<Dish> list(Dish dish);

    // Query dishes by setmeal ID
    @Select("select a.* from dish a left join setmeal_dish b on a.id = b.dish_id where b.setmeal_id = #{setmealId}")
    List<Dish> getBySetmealId(Long id);

    /**
     * Count dishes by conditions
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}
