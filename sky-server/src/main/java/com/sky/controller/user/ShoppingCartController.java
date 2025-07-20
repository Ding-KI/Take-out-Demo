package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/shoppingCart")
@Slf4j
@Api(tags = "Client-end shopping cart management interface")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @PostMapping("/add")
    @ApiOperation("Add shopping cart")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("Add shopping cart: {}", shoppingCartDTO);
        shoppingCartService.addShoppingCart(shoppingCartDTO);
        return Result.success();
    }

    /*Query shopping cart
     */
    @GetMapping("/list")
    @ApiOperation("Query shopping cart")
    public Result <List<ShoppingCart>> list(){
        List<ShoppingCart> list = shoppingCartService.showShoppingCart();
        return Result.success(list);
    }

    // Empty shopping cart
    @DeleteMapping("/clean")
    @ApiOperation("Empty shopping cart")
    public Result clean() {
        log.info("Empty shopping cart");
        shoppingCartService.cleanShoppingCart();
        return Result.success();
    }

    // Delete an item in the shopping cart
    @PostMapping("/sub")
    @ApiOperation("Delete an item in the shopping cart")
    public Result sub(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("Delete an item in the shopping cart: {}", shoppingCartDTO);
        shoppingCartService.subShoppingCart(shoppingCartDTO);
        return Result.success();
    }
}
