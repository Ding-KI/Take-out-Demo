package com.sky.service.impl;

import com.fasterxml.jackson.databind.ser.Serializers;
import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        // Here implement the logic of adding to the shopping cart, first copy the attributes
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        // Get the user ID and inject it
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        // Check if the shopping cart contains the dish or setmeal, if it exists, the quantity is increased by 1, otherwise a new record is added - insert
        if(list != null && list.size() > 0){
            ShoppingCart cart = list.get(0);
            cart.setNumber(cart.getNumber()+1);// Execute the update statement
            shoppingCartMapper.updateNumberById(cart);
        }
        else {
            // Does not exist, add a new record
            // First check if it is a dish or setmeal to be added
            Long dishId = shoppingCartDTO.getDishId();
            if(dishId!=null){
                // It is a dish to be added to the shopping cart
                Dish dish = dishMapper.getById(dishId);
                // Inject the dish information into the shopping cart
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
            }else {
                // It is a setmeal to be added to the shopping cart
                Long setmealId = shoppingCartDTO.getSetmealId();
                Setmeal setmeal = setmealMapper.getById(setmealId);

                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
            }
            // Insert the shopping cart record
            shoppingCart.setNumber(1); // The quantity is 1 by default
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }
    }

    // Query shopping cart
    public List<ShoppingCart> showShoppingCart() {
        // Get the current user ID
        Long userId = BaseContext.getCurrentId();
        // Construct the shoppingCart object
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(userId)
                .build();
        List<ShoppingCart>list = shoppingCartMapper.list(shoppingCart);
        return list;
    }

    // Clean shopping cart
    public void cleanShoppingCart() {
        // Get the current user ID
        Long userId = BaseContext.getCurrentId();
        // Delete the shopping cart record
        shoppingCartMapper.deleteByUserId(userId);
    }

    // Delete an item in the shopping cart
    public void subShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        // Get the current user ID, and query the shopping cart record of the user according to the user ID
        shoppingCart.setUserId(BaseContext.getCurrentId());

        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        // If the shopping cart record is not empty
        if (list != null && list.size() > 0) {
            shoppingCart = list.get(0);
            Integer number = shoppingCart.getNumber();
            if(number == 1){
                // If the quantity is 1, delete the record
                shoppingCartMapper.deleteById(shoppingCart.getId());
        } else {
            // If the quantity is greater than 1, the quantity is decreased by 1
                shoppingCart.setNumber(shoppingCart.getNumber() - 1);
                shoppingCartMapper.updateNumberById(shoppingCart);
            }
        }
    }

}
