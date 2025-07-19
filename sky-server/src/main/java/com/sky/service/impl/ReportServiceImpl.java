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

    // 统计指定日期区间内的营业额
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        //返回dateList-TurnoverReportVO的属性,从begin到end的日期列表
        List<LocalDate> dateList = new ArrayList<>();
        while(!begin.isAfter(end)) {
            dateList.add(begin);
            begin = begin.plusDays(1);
        }

        //返回turnoverList-TurnoverReportVO的属性,每个日期对应的营业额列表
        List<Double> turnoverList = new ArrayList<>();
        //遍历日期列表，查询每个日期的营业额
        for(LocalDate date : dateList) {
            //营业额：状态为已完成的订单总金额
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN); //传入sql的时间点
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();
            map.put("beginTime", beginTime);
            map.put("endTime", endTime);
            map.put("status", Orders.COMPLETED); //状态为已完成
            Double turnover = orderMapper.sumByMap(map);
            // 如果没有订单，则营业额为0
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }

        // 目前返回一个空的 TurnoverReportVO 对象作为占位符
        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ','))
                .turnoverList(StringUtils.join(turnoverList, ','))
                .build();
    }

    // 用户统计
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        //返回dateList-TurnoverReportVO的属性,从begin到end的日期列表
        List<LocalDate> dateList = new ArrayList<>();
        while(!begin.isAfter(end)) {
            dateList.add(begin);
            begin = begin.plusDays(1);
        }

        //存放新增用户和总用户的列表
        List<Integer> newUserList = new ArrayList<>();
        List<Integer> totalUserList = new ArrayList<>();

        for(LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN); //传入sql的时间点
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

    //订单统计
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        //返回dateList-OrderReportVO的属性,从begin到end的日期列表
        List<LocalDate> dateList = new ArrayList<>();
        while(!begin.isAfter(end)) {
            dateList.add(begin);
            begin = begin.plusDays(1);
        }

        //存放每天的订单数和有效订单数
        List<Integer> orderList = new ArrayList<>();
        List<Integer> validOrderList = new ArrayList<>();

        //查询每天订单数和有效订单数
        for(LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN); //传入sql的时间点
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            // 统计每天的订单数量
            Integer orderCount = getOrderCount(beginTime, endTime, null); // 传入null表示统计所有状态的订单
            //统计每天的有效订单数量
            Integer validOrderCount = getOrderCount(beginTime, endTime, Orders.COMPLETED); // 只统计已完成的订单

            orderList.add(orderCount);
            validOrderList.add(validOrderCount);
        }

        //计算区间内的总订单数和总有效订单数
        Integer totalOrderCount = orderList.stream().reduce(Integer::sum).get();
        Integer totalValidOrderCount = validOrderList.stream().reduce(Integer::sum).get();
        //计算订单完成率
        Double completionRate = totalOrderCount == 0 ? 0.0 : (double) totalValidOrderCount / totalOrderCount * 100;

        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ','))
                .orderCountList(StringUtils.join(orderList, ','))
                .validOrderCountList(StringUtils.join(validOrderList, ','))
                .totalOrderCount(totalOrderCount)
                .orderCompletionRate(completionRate)
                .build();
    }

    // 统计销量前十的菜品
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end){
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<GoodsSalesDTO> salesTop10= orderMapper.getSalesTop10(beginTime, endTime);
        //stream流处理，获取商品名称和销量
        List<String> names = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        String nameList = StringUtils.join(names, ',');

        List<Integer> sales = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String salesList = StringUtils.join(sales, ',');
        return SalesTop10ReportVO.builder()
                .nameList(nameList)
                .numberList(salesList)
                .build();
    }

    //根据条件统计订单数量
    private Integer getOrderCount(LocalDateTime beginTime, LocalDateTime endTime, Integer status) {
        Map map = new HashMap<>();
        map.put("begin", beginTime);
        map.put("end", endTime);
        map.put("status", status);
        return orderMapper.countByMap(map);
    }

    //导出报表
    public void exportReport(HttpServletResponse response) {
        //1, 查询数据库，获取最近30天营业数据
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(LocalDateTime.of(dateBegin, LocalTime.MIN), LocalDateTime.of(dateEnd, LocalTime.MAX));
        //2.通过Apache POI导出Excel
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");

        try {
            // 生成Excel文件
            XSSFWorkbook excel = new XSSFWorkbook(inputStream);

            //开始填充数据
            XSSFSheet sheet = excel.getSheet("sheet1"); //获取标签页
            //填充标签页-时间
            sheet.getRow(1).getCell(1).setCellValue("time：" + dateBegin + "至" + dateEnd);
            //填充标签页第4行-营业额,完成率
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessDataVO.getTurnover());
            row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());
            //填充标签页第5行-有效订单数,平均客单价
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());

            //填充标签页第8行及以下行-订单明细
            for(int i = 0; i < 30; i++) {
                LocalDate date = dateBegin.plusDays(i);
                BusinessDataVO businessData = workspaceService.getBusinessData(
                        LocalDateTime.of(date, LocalTime.MIN),
                        LocalDateTime.of(date, LocalTime.MAX));
                row = sheet.getRow(7+i);
                row.getCell(1).setCellValue(date.toString()); //日期
                row.getCell(2).setCellValue(businessData.getTurnover()); //营业额
                row.getCell(3).setCellValue(businessData.getValidOrderCount()); //有效订单数
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate()); //订单完成率
                row.getCell(5).setCellValue(businessData.getUnitPrice()); //平均客单价
                row.getCell(6).setCellValue(businessData.getNewUsers()); //新增用户数
            }


            //3.通过输出流将excel文件写入到响应中
            ServletOutputStream outputStream = response.getOutputStream();
            excel.write(outputStream);
            //4.关闭资源
            outputStream.close();
            excel.close();

        } catch (IOException e) {
            e.printStackTrace();
        }



    }
}
