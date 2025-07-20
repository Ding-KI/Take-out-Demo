-- Test data: insert an order that has not been paid for more than 15 minutes
INSERT INTO orders (
    number, status, user_id, address_book_id, order_time,
    pay_status, amount, phone, address, consignee
) VALUES (
    'TEST001', 1, 1, 1, DATE_SUB(NOW(), INTERVAL 20 MINUTE),
    0, 50.00, '13800138000', 'Test address', 'Test consignee'
);

-- Test data: insert an order that has been in delivery for more than 60 minutes
INSERT INTO orders (
    number, status, user_id, address_book_id, order_time,
    pay_status, amount, phone, address, consignee
) VALUES (
    'TEST002', 4, 1, 1, DATE_SUB(NOW(), INTERVAL 90 MINUTE),
    1, 80.00, '13800138001', 'Test address2', 'Test consignee2'
);

-- Query the current test order status
SELECT id, number, status, order_time, 
       TIMESTAMPDIFF(MINUTE, order_time, NOW()) as minutes_ago
FROM orders 
WHERE number IN ('TEST001', 'TEST002'); 