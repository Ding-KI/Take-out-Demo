package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private WebSocketServer webSocketServer;

    @Transactional
    // User submit order
    public OrderSubmitVO submitOrder(OrdersSubmitDTO orderSubmitDTO) {
        // Handle business exceptions: 1. Delivery address is empty 2. Shopping cart is empty - improve code robustness
        AddressBook addressBook = addressBookMapper.getById(orderSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        ShoppingCart shoppingCart = new ShoppingCart();
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        // Get shopping cart data (list)
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        if(shoppingCartList == null || shoppingCartList.isEmpty()) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        // Insert 1 record into the order table
        Orders orders = new Orders();
        BeanUtils.copyProperties(orderSubmitDTO, orders);
        orders.setOrderTime(LocalDateTime.now()); // Set order time
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setPayStatus(Orders.UN_PAID);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));// Generate order number, use current timestamp
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(userId);

        orderMapper.insert(orders); // Insert order record

        // Insert 1 or more records into the order detail table
        // Loop through the shopping cart list, insert each product into the order detail
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for(ShoppingCart cart : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId()); // Set the order ID associated with the current order detail
           // Insert order detail record
            orderDetailList.add(orderDetail);
        }

        orderDetailMapper.insertBatch(orderDetailList); // Batch insert order detail record
        // Clear the user's shopping cart
        shoppingCartMapper.deleteByUserId(userId);

        // Package VO and return
        OrderSubmitVO orderSubmitVO =OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .build();

        return orderSubmitVO; // Return a new order submission view object
    }

    /**
     * Order payment
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // Current logged-in user ID
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        // Call the WeChat payment interface to generate a pre-payment transaction order
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), // Merchant order number
                new BigDecimal(0.01), // Payment amount, in yuan
                "Sky Take-out Order", // Product description
                user.getOpenid() // WeChat user's openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("This order has been paid");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * Payment successful, modify order status
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // Query order by order number
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // Update order status, payment method, payment status, checkout time by order ID
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);

        // Send message to all connected WebSocket clients: type, orderId, content
        Map map = new HashMap();
        map.put("type",1);//1 means order reminder, 2 means user reminder
        map.put("orderId",ordersDB.getId());
        map.put("content", "Order number: " + outTradeNo);

        String json = JSON.toJSONString(map);
        // Send message to all connected WebSocket clients
        webSocketServer.sendToAllClient(json);
    }

    // User reminder
    public void reminder(Long id) {
        // Query order by order ID
        Orders ordersDB = orderMapper.getById(id);
        // Check if the order exists
        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        // Send reminder message to all connected WebSocket clients
        Map map = new HashMap<>();
        map.put("type", 2); // 2 means user reminder
        map.put("orderId", id);
        map.put("content", "User reminder, order number: " + ordersDB.getNumber());

        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);
    }

    /**
     * User order page query
     *
     * @param pageNum
     * @param pageSize
     * @param status
     * @return
     */
    public PageResult pageQuery4User(int pageNum, int pageSize, Integer status) {
        // Set pagination
        PageHelper.startPage(pageNum, pageSize);

        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        ordersPageQueryDTO.setStatus(status);

        // Pagination condition query
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);

        List<OrderVO> list = new ArrayList();

        // Query order details and package into OrderVO for response
        if (page != null && page.getTotal() > 0) {
            for (Orders orders : page) {
                Long orderId = orders.getId();// Order ID

                // Query order details
                List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orderId);

                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                orderVO.setOrderDetailList(orderDetails);

                list.add(orderVO);
            }
        }
        return new PageResult(page.getTotal(), list);
    }

    /**
     * Query order details
     *
     * @param id Order ID
     * @return OrderVO Order details view object
     */
    public OrderVO details(Long id) {
        // Query order by order ID
        Orders orders = orderMapper.getById(id);

        // Query the corresponding dish/setmeal details of the order
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());

        // Package the order and its details into OrderVO and return
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetailList);

        return orderVO;
    }

    /**
     * User cancel order
     *
     * @param id
     */
    public void userCancelById(Long id) throws Exception {
        // Query order by order ID
        Orders ordersDB = orderMapper.getById(id);

        // Check if the order exists
        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        // Order status 1 pending payment 2 pending order 3 confirmed 4 delivery in progress 5 completed 6 cancelled
        if (ordersDB.getStatus() > 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());

        // If the order is cancelled in the pending order state, a refund is required
        if (ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            // Call the WeChat payment refund interface
            weChatPayUtil.refund(
                    ordersDB.getNumber(), // Merchant order number
                    ordersDB.getNumber(), // Merchant refund order number
                    new BigDecimal(0.01),// Refund amount, in yuan
                    new BigDecimal(0.01));// Original order amount

            // Payment status modified to refund
            orders.setPayStatus(Orders.REFUND);
        }

        // Update order status, cancellation reason, cancellation time
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("User cancelled");
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * Repeat order
     *
     * @param id
     */
    public void repetition(Long id) {
        // Query the current user ID
        Long userId = BaseContext.getCurrentId();

        // Query the current order details by order ID
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);

        // Convert the order detail object to a shopping cart object
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map(x -> {
            ShoppingCart shoppingCart = new ShoppingCart();

            // Copy the dish information in the original order detail to the shopping cart object
            BeanUtils.copyProperties(x, shoppingCart, "id");
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());

            return shoppingCart;
        }).collect(Collectors.toList());

        // Add the shopping cart object to the database in batch
        shoppingCartMapper.insertBatch(shoppingCartList);
    }

    /**
     * Order search
     *
     * @param ordersPageQueryDTO
     * @return
     */
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());

        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);

        // Some order statuses need to return additional order dish information, convert Orders to OrderVO
        List<OrderVO> orderVOList = getOrderVOList(page);

        return new PageResult(page.getTotal(), orderVOList);
    }

    private List<OrderVO> getOrderVOList(Page<Orders> page) {
        // Need to return order dish information, custom OrderVO response result
        List<OrderVO> orderVOList = new ArrayList<>();

        List<Orders> ordersList = page.getResult();
        if (!CollectionUtils.isEmpty(ordersList)) {
            for (Orders orders : ordersList) {
                // Copy common fields to OrderVO
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                String orderDishes = getOrderDishesStr(orders);

                // Package the order dish information into orderVO and add it to orderVOList
                orderVO.setOrderDishes(orderDishes);
                orderVOList.add(orderVO);
            }
        }
        return orderVOList;
    }

    /**
     * Get dish information string by order ID
     *
     * @param orders
     * @return
     */
    private String getOrderDishesStr(Orders orders) {
        // Query order dish detail information (dishes and quantities in the order)
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());

        // Concatenate each order dish information into a string (format: Gongbao Chicken*3;)
        List<String> orderDishList = orderDetailList.stream().map(x -> {
            String orderDish = x.getName() + "*" + x.getNumber() + ";";
            return orderDish;
        }).collect(Collectors.toList());

        // Concatenate all dish information corresponding to the order
        return String.join("", orderDishList);
    }

    /**
     * Order quantity statistics by status
     *
     * @return
     */
    public OrderStatisticsVO statistics() {
        // Query the number of orders in the pending order, pending delivery, and delivery in progress states respectively
        Integer toBeConfirmed = orderMapper.countStatus(Orders.TO_BE_CONFIRMED);
        Integer confirmed = orderMapper.countStatus(Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS);

        // Package the queried data into orderStatisticsVO and return
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
        return orderStatisticsVO;
    }

    /**
     * Accept order
     *
     * @param ordersConfirmDTO
     */
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(Orders.CONFIRMED)
                .build();

        orderMapper.update(orders);
    }

    /**
     * Reject order
     *
     * @param ordersRejectionDTO
     */
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        // Query the order by ID
        Orders ordersDB = orderMapper.getById(ordersRejectionDTO.getId());

        // The order must exist and the status must be 2 (pending order) to reject the order
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        // Payment status
        Integer payStatus = ordersDB.getPayStatus();
        if (payStatus == Orders.PAID) {
            // User has paid, need to refund
            String refund = weChatPayUtil.refund(
                    ordersDB.getNumber(),
                    ordersDB.getNumber(),
                    new BigDecimal(0.01),
                    new BigDecimal(0.01));
            log.info("Refund application: {}", refund);
        }

        // Rejecting an order requires a refund, update the order status, rejection reason, and cancellation time by order ID
        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        orders.setCancelTime(LocalDateTime.now());

        orderMapper.update(orders);
    }

    /**
     * Cancel order
     *
     * @param ordersCancelDTO
     */
    public void cancel(OrdersCancelDTO ordersCancelDTO) throws Exception {
            // Query the order by ID
        Orders ordersDB = orderMapper.getById(ordersCancelDTO.getId());

        // Payment status
        Integer payStatus = ordersDB.getPayStatus();
        if (payStatus == 1) {
            // User has paid, need to refund
            String refund = weChatPayUtil.refund(
                    ordersDB.getNumber(),
                    ordersDB.getNumber(),
                    new BigDecimal(0.01),
                    new BigDecimal(0.01));
            log.info("申请退款：{}", refund);
        }

        // Admin canceling an order requires a refund, update the order status, cancellation reason, and cancellation time by order ID
        Orders orders = new Orders();
        orders.setId(ordersCancelDTO.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * Delivery order
     *
     * @param id
     */
    public void delivery(Long id) {
        // Query the order by ID
        Orders ordersDB = orderMapper.getById(id);

        // Check if the order exists and the status is 3
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        // Update the order status, status changed to delivery in progress
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);

        orderMapper.update(orders);
    }

    /**
     * Complete order
     *
     * @param id
     */
    public void complete(Long id) {
        // Query the order by ID
        Orders ordersDB = orderMapper.getById(id);

        // Check if the order exists and the status is 4
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        // Update the order status, status changed to completed
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());

        orderMapper.update(orders);
    }
}
