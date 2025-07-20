package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService {

    // Add shopping cart
    void addShoppingCart(ShoppingCartDTO shoppingCartDTO);

    // Query shopping cart
    List<ShoppingCart> showShoppingCart();


    // Clean shopping cart
    void cleanShoppingCart();

    // Delete an item in the shopping cart
    void subShoppingCart(ShoppingCartDTO shoppingCartDTO);
}
