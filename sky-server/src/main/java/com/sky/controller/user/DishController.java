package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api(tags = "Client-end dish browsing interface")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * Query dishes by category ID
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("Query dishes by category ID")
    public Result<List<DishVO>> list(Long categoryId) {
        // Get the key in redis
        String key = "dish_" + categoryId;
        // Query dishes list from redis
        List<DishVO> cachedDish = (List<DishVO>) redisTemplate.opsForValue().get(key);
        // If there is data in the cache, return directly
        if (cachedDish != null && cachedDish.size()>0)  {
            log.info("Get dishes list from cache, category ID: {}", categoryId);
            return Result.success(cachedDish);
        }

        // If there is no data in the cache, query from the database
        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);// Query dishes that are on sale
        // And store in cache
        List<DishVO> list = dishService.listWithFlavor(dish);
        redisTemplate.opsForValue().set(key, list);
        return Result.success(list);
    }

}
