package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Api(tags = "Shop management interface")
@Slf4j
public class ShopController {

    public static final String KEY = "SHOP_STATUS";

    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * Update shop status
     * @param status Shop status
     * @return Success message
     */
    @PutMapping("/{status}")
    @ApiOperation("Update shop status")
    public Result setStatus(@PathVariable Integer status) {
        log.info("Set shop status to: {}", status == 1 ? "Open" : "Closed");
        // Store shop status in Redis
        redisTemplate.opsForValue().set(KEY, status);
        return Result.success("Shop status updated to: " + status);
    }

    /**
     * Get shop status
     * @return Shop status
     */
    @GetMapping("/status")
    @ApiOperation("Get shop status")
    public Result<Integer> getStatus() {
        Integer status = (Integer) redisTemplate.opsForValue().get(KEY);

        log.info("Current shop status: {}", status == 1 ? "Open" : "Closed");
        return Result.success(status);
    }
}
