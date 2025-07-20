package com.sky.service;

import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

public interface ReportService {
    // Count sales by date range
    TurnoverReportVO getTurnoverStatistics (LocalDate begin, LocalDate end);

    UserReportVO getUserStatistics (LocalDate begin, LocalDate end);

    OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end);

    // Get the top 10 products by sales, specified date range
    SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end);


    // Export report
    void exportReport(HttpServletResponse response);
}
