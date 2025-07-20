package com.sky.controller.admin;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Category management
 */
@RestController
@RequestMapping("/admin/category")
@Api(tags = "分类相关接口")
@Slf4j
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * Add category
     * @param categoryDTO
     * @return
     */
    @PostMapping
    @ApiOperation("Add category")
    public Result<String> save(@RequestBody CategoryDTO categoryDTO){
        log.info("Add category: {}", categoryDTO);
        categoryService.save(categoryDTO);
        return Result.success();
    }

    /**
     * Category page query
     * @param categoryPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("Category page query")
    public Result<PageResult> page(CategoryPageQueryDTO categoryPageQueryDTO){
        log.info("Page query: {}", categoryPageQueryDTO);
        PageResult pageResult = categoryService.pageQuery(categoryPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * Delete category
     * @param id
     * @return
     */
    @DeleteMapping
    @ApiOperation("Delete category")
    public Result<String> deleteById(Long id){
        log.info("Delete category: {}", id);
        categoryService.deleteById(id);
        return Result.success();
    }

    /**
     * Modify category
     * @param categoryDTO
     * @return
     */
    @PutMapping
    @ApiOperation("Modify category")
    public Result<String> update(@RequestBody CategoryDTO categoryDTO){
        categoryService.update(categoryDTO);
        return Result.success();
    }

    /**
     * Enable/disable category
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("Enable/disable category")
    public Result<String> startOrStop(@PathVariable("status") Integer status, Long id){
        categoryService.startOrStop(status,id);
        return Result.success();
    }

    /**
     * Query category by type
     * @param type
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("Query category by type")
    public Result<List<Category>> list(Integer type){
        List<Category> list = categoryService.list(type);
        return Result.success(list);
    }
}
