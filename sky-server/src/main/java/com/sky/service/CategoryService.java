package com.sky.service;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import java.util.List;

public interface CategoryService {

    /**
     * Add category
     * @param categoryDTO
     */
    void save(CategoryDTO categoryDTO);

    /**
     * Page query
     * @param categoryPageQueryDTO
     * @return
     */
    PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);

    /**
     * Delete by ID
     * @param id
     */
    void deleteById(Long id);

    /**
     * Modify category
     * @param categoryDTO
     */
    void update(CategoryDTO categoryDTO);

    /**
     * Enable, disable category
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);

    /**
     * Query by type
     * @param type
     * @return
     */
    List<Category> list(Integer type);
}
