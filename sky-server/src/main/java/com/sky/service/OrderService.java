package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {

    // User submit order
    OrderSubmitVO submitOrder(OrdersSubmitDTO orderSubmitDTO);

    /**
     * Order payment
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * Payment successful, modify order status
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);

    // User reminder
    void reminder(Long id);

    // Page query user order
    PageResult pageQuery4User(int page, int pageSize, Integer status);

    // Query order details
    OrderVO details(Long id);

    // User cancel order
    void userCancelById(Long id) throws Exception;

    // User repeat order, add original order goods to shopping cart again
    void repetition(Long id);

    // Admin condition query order
    PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    // Admin query order number statistics by status
    OrderStatisticsVO statistics();

    // Accept order
    void confirm(OrdersConfirmDTO ordersConfirmDTO);

    // Reject order
    void rejection(OrdersRejectionDTO ordersRejectionDTO) throws Exception;

    // Cancel order
    void cancel(OrdersCancelDTO ordersCancelDTO) throws  Exception;

    // Delivery order
    void delivery(Long id);

    // Complete order
    void complete(Long id);
}
