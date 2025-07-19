package com.sky.service;

import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

public interface ReportService {
    //统计指定日期区间内的营业额
    TurnoverReportVO getTurnoverStatistics (LocalDate begin, LocalDate end);

    UserReportVO getUserStatistics (LocalDate begin, LocalDate end);

    OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end);

    //获取销售额前10的商品，指定日期区间
    SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end);


    //导出报表
    void exportReport(HttpServletResponse response);
}
