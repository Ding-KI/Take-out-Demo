package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper

public interface ShoppingCartMapper {

    //动态查询购物车
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    //根据用户id，增加用户的购物车的商品数量+1
    @Update("UPDATE shopping_cart SET number = #{number} WHERE id = #{id}")
    void updateNumberById(ShoppingCart shoppingCart);

    //插入购物车记录
    @Insert("insert into shopping_cart (name, user_id, dish_id, setmeal_id, dish_flavor, number, amount, image, create_time) " +
            "values (#{name}, #{userId}, #{dishId}, #{setmealId}, #{dishFlavor}, #{number}, #{amount}, #{image}, #{createTime})")
    void insert(ShoppingCart shoppingCart);

    //根据用户id删除购物车记录
    @Delete("DELETE FROM shopping_cart WHERE user_id = #{userId}")
    void deleteByUserId(Long userId);

    //根据用户id删除购物车记录
    @Delete("DELETE FROM shopping_cart WHERE id = #{userId}")
    void deleteById(Long userId);

    //批量插入购物车数据
    void insertBatch(List<ShoppingCart> shoppingCartList);
}
