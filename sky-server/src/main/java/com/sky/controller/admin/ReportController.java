package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Report management interface
 */

@Component
@RequestMapping("/admin/report")
@Api(tags = "Report management interface")
@Slf4j
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/turnoverStatistics")
    @ApiOperation("Turnover statistics")
    public Result<TurnoverReportVO> turnoverStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("Turnover statistics, start time: {}, end time: {}", begin, end);

        return Result.success(reportService.getTurnoverStatistics(begin, end));
    }

    // User statistics
    @GetMapping("/userStatistics")
    @ApiOperation("User statistics")
    public Result<UserReportVO> userStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end)
    {
        log.info("User statistics");
        return Result.success(reportService.getUserStatistics(begin, end));
    }

    // Order statistics
    @GetMapping("/orderStatistics")
    @ApiOperation("Order statistics")
    public Result<OrderReportVO> orderStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end)
    {
        log.info("Order statistics, start time: {}, end time: {}", begin, end);
        return Result.success(reportService.getOrderStatistics(begin, end));
    }

    // Statistics for the top 10 dishes by sales
    @GetMapping("/top10")
    @ApiOperation("Top 10 dishes by sales")
    public Result<SalesTop10ReportVO> top10(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end)
    {
        log.info("Sales ranking top 10, start time: {}, end time: {}", begin, end);
        return Result.success(reportService.getSalesTop10(begin, end));
    }

    /* Export report
    * */
    @GetMapping("/export")
    @ApiOperation("Export report")
    public void exportReport(HttpServletResponse response) {
        reportService.exportReport(response);
    }
}
