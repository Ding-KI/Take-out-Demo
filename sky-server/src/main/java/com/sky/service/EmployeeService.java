package com.sky.service;

import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.result.PageResult;

public interface EmployeeService {

    /**
     * Employee login
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    // Add employee business method
    void save(EmployeeDTO employeeDTO);

    /**
     * Page query employee information
     * @param employeePageQueryDTO
     * @return
     */
    PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * Enable, disable employee account
     * @param id
     * @return
     */
    void startOrStop(Integer status, Long id);

    // Query employee information by ID
    Employee getById(Long id);

    // Modify employee information
    void update(EmployeeDTO employeeDTO);
}
