package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper

public interface ShoppingCartMapper {

    // Dynamic query shopping cart
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    // Increase the number of products in the shopping cart of the user by 1
    @Update("UPDATE shopping_cart SET number = #{number} WHERE id = #{id}")
    void updateNumberById(ShoppingCart shoppingCart);

    // Insert shopping cart record
    @Insert("insert into shopping_cart (name, user_id, dish_id, setmeal_id, dish_flavor, number, amount, image, create_time) " +
            "values (#{name}, #{userId}, #{dishId}, #{setmealId}, #{dishFlavor}, #{number}, #{amount}, #{image}, #{createTime})")
    void insert(ShoppingCart shoppingCart);

    // Delete shopping cart record by user ID
    @Delete("DELETE FROM shopping_cart WHERE user_id = #{userId}")
    void deleteByUserId(Long userId);

    // Delete shopping cart record by user ID
    @Delete("DELETE FROM shopping_cart WHERE id = #{userId}")
    void deleteById(Long userId);

    // Batch insert shopping cart data
    void insertBatch(List<ShoppingCart> shoppingCartList);
}
