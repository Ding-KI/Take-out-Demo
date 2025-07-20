package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * Dish management controller
 * Responsible for the addition, deletion, modification and query of dishes
 */

@RestController
@RequestMapping("/admin/dish")
@Api(tags = "Dish management interface")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;

    public void cleanCache(String pattern) {
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }

    @PostMapping
    @ApiOperation("Add and save dish")
    public Result save(@RequestBody DishDTO dishDTO) {
        log.info("Add dish: {}", dishDTO);
        dishService.saveWithFlavor(dishDTO);
        // Clear related cache
        String key = "dish_" + dishDTO.getCategoryId();
        cleanCache(key);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("Dish page query")
    public Result<PageResult> page (DishPageQueryDTO dishPageQueryDTO) {
        log.info("Dish page query: {}", dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    @DeleteMapping()
    @ApiOperation("Batch delete dish")
    public Result delete(@RequestParam List<Long> ids) {
        log.info("Delete dish, ID: {}", ids);
        dishService.deleteBatch(ids);
        cleanCache("dish_*");
        return Result.success("Dish deleted successfully");
    }

    @GetMapping("/{id}")
    @ApiOperation("Query dish by ID")
    public Result<DishVO> getById(@PathVariable Long id) {
        log.info("Query dish, ID: {}", id);
        DishVO dishVO = dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    @PutMapping
    @ApiOperation("Modify dish")
    public Result update(@RequestBody DishDTO dishDTO) {
        log.info("Modify dish: {}", dishDTO);
        dishService.updateWithFlavor(dishDTO);
        cleanCache("dish_*");
        return Result.success("Dish modified successfully");
    }


    @PostMapping("/status/{status}")
    @ApiOperation("Batch modify dish status")
    public Result<String> startOrStop(@PathVariable Integer status, Long id){
        dishService.startOrStop(status, id);
        // Clear cache
        cleanCache("dish_*");
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("Query dish list by category ID")
    public Result<List<Dish>> list(Long categoryId) {
        log.info("Query dish list by category ID: {}", categoryId);
        List<Dish> List = dishService.list(categoryId);
        return Result.success(List);
    }

}
