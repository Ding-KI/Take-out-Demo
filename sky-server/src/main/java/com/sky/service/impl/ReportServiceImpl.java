package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private WorkspaceService workspaceService;

    // Count turnover within a specified date range
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        // Return the dateList attribute of TurnoverReportVO, a list of dates from begin to end
        List<LocalDate> dateList = new ArrayList<>();
        while(!begin.isAfter(end)) {
            dateList.add(begin);
            begin = begin.plusDays(1);
        }

        // Return the turnoverList attribute of TurnoverReportVO, a list of turnovers corresponding to each date
        List<Double> turnoverList = new ArrayList<>();
        // Traverse the date list, query the turnover for each date
        for(LocalDate date : dateList) {
            // Turnover: total amount of completed orders
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN); // Pass in the time point of the sql
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();
            map.put("beginTime", beginTime);
            map.put("endTime", endTime);
            map.put("status", Orders.COMPLETED); // Status is completed
            Double turnover = orderMapper.sumByMap(map);
            // If there are no orders, the turnover is 0
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }

        // Currently return an empty TurnoverReportVO object as a placeholder
        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ','))
                .turnoverList(StringUtils.join(turnoverList, ','))
                .build();
    }

    // User statistics
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        // Return the dateList attribute of TurnoverReportVO, a list of dates from begin to end
        List<LocalDate> dateList = new ArrayList<>();
        while(!begin.isAfter(end)) {
            dateList.add(begin);
            begin = begin.plusDays(1);
        }

        // Store the list of new users and total users
        List<Integer> newUserList = new ArrayList<>();
        List<Integer> totalUserList = new ArrayList<>();

        for(LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN); // Pass in the time point of the sql
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map map = new HashMap();
            map.put("endTime", end);
            Integer totalUser = userMapper.countByMap(map);
            map.put("beginTime", begin);
            Integer newUser = userMapper.countByMap(map);

            totalUserList.add(totalUser);
            newUserList.add(newUser);
        }

        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ','))
                .newUserList(StringUtils.join(newUserList, ','))
                .totalUserList(StringUtils.join(totalUserList, ','))
                .build();
    }

    // Order statistics
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        // Return the dateList attribute of OrderReportVO, a list of dates from begin to end
        List<LocalDate> dateList = new ArrayList<>();
        while(!begin.isAfter(end)) {
            dateList.add(begin);
            begin = begin.plusDays(1);
        }

        // Store the number of orders and valid orders for each day
        List<Integer> orderList = new ArrayList<>();
        List<Integer> validOrderList = new ArrayList<>();

        // Query the number of orders and valid orders for each day
        for(LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN); // Pass in the time point of the sql
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            // Count the number of orders for each day
            Integer orderCount = getOrderCount(beginTime, endTime, null); // Pass in null to represent all status orders
            // Count the number of valid orders for each day
            Integer validOrderCount = getOrderCount(beginTime, endTime, Orders.COMPLETED); // Only count completed orders

            orderList.add(orderCount);
            validOrderList.add(validOrderCount);
        }

        // Calculate the total number of orders and total number of valid orders within the interval
        Integer totalOrderCount = orderList.stream().reduce(Integer::sum).get();
        Integer totalValidOrderCount = validOrderList.stream().reduce(Integer::sum).get();
        // Calculate the order completion rate
        Double completionRate = totalOrderCount == 0 ? 0.0 : (double) totalValidOrderCount / totalOrderCount * 100;

        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ','))
                .orderCountList(StringUtils.join(orderList, ','))
                .validOrderCountList(StringUtils.join(validOrderList, ','))
                .totalOrderCount(totalOrderCount)
                .orderCompletionRate(completionRate)
                .build();
    }

    // Count the top 10 dishes by sales
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end){
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<GoodsSalesDTO> salesTop10= orderMapper.getSalesTop10(beginTime, endTime);
        // Stream processing, get the product name and sales
        List<String> names = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        String nameList = StringUtils.join(names, ',');

        List<Integer> sales = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String salesList = StringUtils.join(sales, ',');
        return SalesTop10ReportVO.builder()
                .nameList(nameList)
                .numberList(salesList)
                .build();
    }

    // Count the number of orders by conditions
    private Integer getOrderCount(LocalDateTime beginTime, LocalDateTime endTime, Integer status) {
        Map map = new HashMap<>();
        map.put("begin", beginTime);
        map.put("end", endTime);
        map.put("status", status);
        return orderMapper.countByMap(map);
    }

    // Export report
    public void exportReport(HttpServletResponse response) {
        //1, Query the database, get the recent 30 days of business data
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(LocalDateTime.of(dateBegin, LocalTime.MIN), LocalDateTime.of(dateEnd, LocalTime.MAX));
        //2.export Excel file
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("template/operation_data_report_template.xlsx");

        try {
            // Generate Excel file
            XSSFWorkbook excel = new XSSFWorkbook(inputStream);

            // Start filling data
            XSSFSheet sheet = excel.getSheet("sheet1"); // Get the label page
            // Fill the label page - time
            sheet.getRow(1).getCell(1).setCellValue("time：" + dateBegin + "至" + dateEnd);
            // Fill the label page - turnover, completion rate
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessDataVO.getTurnover());
            row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());
            // Fill the label page - valid order number, average customer unit price
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());

            // Fill the label page - order details
            for(int i = 0; i < 30; i++) {
                LocalDate date = dateBegin.plusDays(i);
                BusinessDataVO businessData = workspaceService.getBusinessData(
                        LocalDateTime.of(date, LocalTime.MIN),
                        LocalDateTime.of(date, LocalTime.MAX));
                row = sheet.getRow(7+i);
                row.getCell(1).setCellValue(date.toString()); // Date
                row.getCell(2).setCellValue(businessData.getTurnover()); // Turnover
                row.getCell(3).setCellValue(businessData.getValidOrderCount()); // Valid order number
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate()); // Order completion rate
                row.getCell(5).setCellValue(businessData.getUnitPrice()); // Average customer unit price
                row.getCell(6).setCellValue(businessData.getNewUsers()); // New user number
            }


            //3.write the Excel file to the response through the output stream
            ServletOutputStream outputStream = response.getOutputStream();
            excel.write(outputStream);
            //4.close the resource
            outputStream.close();
            excel.close();

        } catch (IOException e) {
            e.printStackTrace();
        }



    }
}
