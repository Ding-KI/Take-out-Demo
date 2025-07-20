package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    // Insert order record
    void insert(Orders orders);

    /**
     * Query by order number
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * Modify order information
     * @param orders
     */
    void update(Orders orders);

    // Find orders with pending payment status and order time less than the specified time, return list
    @Select("SELECT * FROM orders WHERE status = #{status} AND order_time < #{orderTime}")
    List<Orders> getByStatusAndOrderTimeLT(Integer status, LocalDateTime orderTime);

    // Query by order ID
    Orders getById(Long id);

    // Count sales by order status and time range
    Double sumByMap(Map map);

    // Query order number by conditions
    Integer countByMap(Map map);

    // Query sales top 10 products by date conditions and order status
    List<GoodsSalesDTO> getSalesTop10(LocalDateTime begin, LocalDateTime end);

    // Page query orders, sort by order time
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * Count orders by status
     */
    @Select("select count(id) from orders where status = #{status}")
    Integer countStatus(Integer toBeConfirmed);
}
