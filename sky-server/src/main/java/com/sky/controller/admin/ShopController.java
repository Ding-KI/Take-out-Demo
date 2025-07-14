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
@Api(tags = "商店管理接口")
@Slf4j
public class ShopController {

    public static final String KEY = "SHOP_STATUS";

    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 更新商店状态
     * @param status 商店状态
     * @return 成功消息
     */
    @PutMapping("/{status}")
    @ApiOperation("更新商店状态")
    public Result setStatus(@PathVariable Integer status) {
        log.info("设置商店状态为: {}", status == 1 ? "营业中" : "打烊中");
        // 将商店状态存入Redis
        redisTemplate.opsForValue().set(KEY, status);
        return Result.success("商店状态已更新为: " + status);
    }

    /**
     * 获取商店状态
     * @return 商店状态
     */
    @GetMapping("/status")
    @ApiOperation("获取商店状态")
    public Result<Integer> getStatus() {
        Integer status = (Integer) redisTemplate.opsForValue().get(KEY);

        log.info("当前商店状态: {}", status == 1 ? "营业中" : "打烊中");
        return Result.success(status);
    }
}
