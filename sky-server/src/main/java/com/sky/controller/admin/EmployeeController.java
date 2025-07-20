package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
@Api(tags = "员工管理")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")
    @ApiOperation(value = "员工登录", notes = "员工登录")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * Logout
     *
     * @return
     */
    @PostMapping("/logout")
    @ApiOperation(value = "Employee logout", notes = "Employee logout")
    public Result<String> logout() {
        return Result.success();
    }


    // Add employee
    @PostMapping
    @ApiOperation(value = "Add employee")
    public Result save (@RequestBody EmployeeDTO employeeDTO){
        log.info("Add employee: {}", employeeDTO);
        System.out.println("Current thread ID: " + Thread.currentThread().getId());
        employeeService.save(employeeDTO);
        return Result.success();
    }

    // Employee page query
    @GetMapping("/page")
    @ApiOperation("Employee page query")
    public Result<PageResult> page(EmployeePageQueryDTO employeePageQueryDTO) {
        log.info("Employee page query: {}", employeePageQueryDTO);
        //调用业务方法
        PageResult pageResult = employeeService.pageQuery(employeePageQueryDTO);
        return Result.success(pageResult);
    }

    // Set account status - disable employee
    @PostMapping("/status/{status}")
    @ApiOperation("Enable/disable employee account")
    public Result startOrStop(@PathVariable Integer status, Long id) {
        log.info("Enable/disable employee account: id={}, status={}", id, status);
        employeeService.startOrStop(status, id);
        return Result.success();
    }

    /*
    * Query employee information by ID*/
    @GetMapping("/{id}")
    @ApiOperation("Query employee information by ID")
    public Result<Employee> getById(@PathVariable Long id){
        Employee employee = employeeService.getById(id);
        return Result.success(employee);
    }

    /*
    * */
    @PutMapping
    @ApiOperation("Modify employee information")
    public Result update(@RequestBody EmployeeDTO employeeDTO) {
        log.info("Modify employee information: {}", employeeDTO);
        // Call business method
        employeeService.update(employeeDTO);
        return Result.success();
    }
}
