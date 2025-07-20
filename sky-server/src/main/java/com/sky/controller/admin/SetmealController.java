package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/setmeal")
@Api(tags = "Setmeal management interface")
@Slf4j

public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    /*Add setmeal
    @param setmealDTO
    @return
     */
    @PostMapping
    @ApiOperation("Add setmeal")
    @CacheEvict(cacheNames = "setmeal", key = "#setmealDTO.categoryId")// Clear setmeal cache
    public Result save(@RequestBody SetmealDTO setmealDTO) {
        setmealService.saveWithDish(setmealDTO);
        return Result.success();
    }

    /*
    Setmeal page query
    @param setmealPageQueryDTO
    @return
     */
    @GetMapping("/page")
    @ApiOperation("Setmeal page query")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageResult pageResult = setmealService.pageQuery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    /*
    Batch delete setmeal
    @param ids
     */
    @DeleteMapping()
    @ApiOperation("Batch delete setmeal")
    @CacheEvict(cacheNames = "setmeal", allEntries = true) // Clear all setmeal cache
    public Result delete(@RequestParam List<Long> ids) {
        setmealService.deleteBatch(ids);
        return Result.success();
    }

    /*    Query setmeal by ID
    @param id Setmeal ID
    @return Setmeal information
     */
    @GetMapping("/{id}")
    @ApiOperation("Query setmeal by ID")
    public Result<SetmealVO> getById(@PathVariable Long id) {
        SetmealVO setmealVO = setmealService.getByIdwithDish(id);
        return Result.success(setmealVO);
    }

    /*Modify setmeal
    @param setmealDTO Setmeal information
    @return Success response
     */
    @PutMapping
    @ApiOperation("Modify setmeal")
    @CacheEvict(cacheNames = "setmeal", allEntries = true)
    public Result update(@RequestBody SetmealDTO setmealDTO) {
        setmealService.update(setmealDTO);
        return Result.success();
    }

    /*Stop or start selling setmeal
    @param status Setmeal status
    @param id
    @return Success response
     */
    @PostMapping("/status/{status}")
    @ApiOperation("Stop or start selling setmeal")
    @CacheEvict(cacheNames = "setmeal", allEntries = true)
    public Result startOrStop(@PathVariable Integer status, Long id) {
        setmealService.startOrStop(status, id);
        return Result.success();
    }
}
