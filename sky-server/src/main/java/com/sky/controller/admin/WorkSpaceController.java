package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Workbench
 */
@RestController
@RequestMapping("/admin/workspace")
@Slf4j
@Api(tags = "Workbench related interface")
public class WorkSpaceController {

    @Autowired
    private WorkspaceService workspaceService;

    /**
     * Workbench today data query
     * @return
     */
    @GetMapping("/businessData")
    @ApiOperation("Workbench today data query")
    public Result<BusinessDataVO> businessData(){
        // Get the start time of the current day
        LocalDateTime begin = LocalDateTime.now().with(LocalTime.MIN);
        // Get the end time of the current day
        LocalDateTime end = LocalDateTime.now().with(LocalTime.MAX);

        BusinessDataVO businessDataVO = workspaceService.getBusinessData(begin, end);
        return Result.success(businessDataVO);
    }

    /**
     * Query order management data
     * @return
     */
    @GetMapping("/overviewOrders")
    @ApiOperation("Query order management data")
    public Result<OrderOverViewVO> orderOverView(){
        return Result.success(workspaceService.getOrderOverView());
    }

    /**
     * Query dish overview
     * @return
     */
    @GetMapping("/overviewDishes")
    @ApiOperation("Query dish overview")
    public Result<DishOverViewVO> dishOverView(){
        return Result.success(workspaceService.getDishOverView());
    }

    /**
     * Query setmeal overview
     * @return
     */
    @GetMapping("/overviewSetmeals")
    @ApiOperation("Query setmeal overview")
    public Result<SetmealOverViewVO> setmealOverView(){
        return Result.success(workspaceService.getSetmealOverView());
    }
}
