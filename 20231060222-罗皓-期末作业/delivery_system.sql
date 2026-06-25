DROP DATABASE IF EXISTS delivery_db;
CREATE DATABASE delivery_db CHARACTER SET utf8mb4;
USE delivery_db;

-- 用户表
CREATE TABLE sys_user (
    user_id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(50) NOT NULL COMMENT '账号',
    password VARCHAR(64) NOT NULL COMMENT '密码',
    role INT NOT NULL DEFAULT 2 COMMENT '角色:0管理员,1商家,2学生',
    name VARCHAR(50) COMMENT '昵称',
    
    PRIMARY KEY (user_id),
    UNIQUE KEY uk_username (username),
    CONSTRAINT ck_role CHECK (role IN (0, 1, 2))
) ENGINE=InnoDB COMMENT='用户信息表';

-- 菜品表
CREATE TABLE dish (
    dish_id BIGINT AUTO_INCREMENT COMMENT '菜品ID',
    name VARCHAR(100) NOT NULL COMMENT '菜品名称',
    price DECIMAL(10,2) NOT NULL COMMENT '价格',
    stock INT DEFAULT 100 COMMENT '库存',
    merchant_id BIGINT NOT NULL COMMENT '所属商家ID',
    
    PRIMARY KEY (dish_id),
    CONSTRAINT ck_stock CHECK (stock >= 0),
    CONSTRAINT fk_dish_merchant FOREIGN KEY (merchant_id) REFERENCES sys_user(user_id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='菜品表';

-- 订单表
CREATE TABLE trade_order (
    order_id BIGINT AUTO_INCREMENT COMMENT '订单ID',
    user_id BIGINT NOT NULL COMMENT '下单用户ID',
    dish_name VARCHAR(100) NOT NULL COMMENT '菜品快照名',
    address VARCHAR(255) NOT NULL COMMENT '配送地址',
    status INT DEFAULT 0 COMMENT '状态:0待发货,1配送中,2已送达',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    
    PRIMARY KEY (order_id),
    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES sys_user(user_id)
) ENGINE=InnoDB COMMENT='订单表';


DELIMITER //
CREATE PROCEDURE proc_place_order(
    IN p_user_id BIGINT,     -- 输入：用户ID
    IN p_dish_id BIGINT,     -- 输入：菜品ID
    IN p_address VARCHAR(255), -- 输入：地址
    OUT p_result VARCHAR(50)   -- 输出：结果消息
)
BEGIN
    DECLARE v_stock INT DEFAULT 0;
    DECLARE v_dish_name VARCHAR(100) DEFAULT '';
    
    SELECT stock, name INTO v_stock, v_dish_name 
    FROM dish WHERE dish_id = p_dish_id;
    
    IF v_stock IS NULL THEN
        SET p_result = '失败: 菜品不存在';
    ELSEIF v_stock <= 0 THEN
        SET p_result = '失败: 库存不足';
    ELSE
        START TRANSACTION;
            UPDATE dish SET stock = stock - 1 WHERE dish_id = p_dish_id;
            INSERT INTO trade_order(user_id, dish_name, address, status) 
            VALUES (p_user_id, v_dish_name, p_address, 0);
        COMMIT;
        SET p_result = '成功: 订单已生成';
    END IF;
END //
DELIMITER ;

INSERT INTO sys_user (user_id, username, password, role, name) VALUES 
(101, 'kfc', '123456', 1, '肯德基'),
(201, 'stu1', '123456', 2, '小明');

INSERT INTO dish (dish_id, name, price, stock, merchant_id) VALUES 
(1, '香辣鸡腿堡', 18.00, 2, 101); 


INSERT INTO trade_order (user_id, dish_name, address, status, create_time) VALUES 
(201, '香辣鸡腿堡', '西区宿舍5栋302', 2, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(201, '生椰拿铁', '图书馆2楼阅览室', 2, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(201, '大份薯条', '西区宿舍5栋302', 2, DATE_SUB(NOW(), INTERVAL 1 DAY));

INSERT INTO trade_order (user_id, dish_name, address, status, create_time) VALUES 
(201, '飘香拌面', '教学楼A栋101', 1, DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
(201, '奥利奥麦旋风', '操场看台', 1, DATE_SUB(NOW(), INTERVAL 15 MINUTE));

INSERT INTO trade_order (user_id, dish_name, address, status, create_time) VALUES 
(201, '瓦罐莲藕汤', '西区宿舍5栋302', 0, NOW()),
(201, '加浓美式', '行政楼前台', 0, NOW());

INSERT INTO sys_user (user_id, username, password, role, name) 
VALUES (105, 'mixue', '123456', 1, '蜜雪冰城Mixue');

INSERT INTO dish (name, price, stock, merchant_id) VALUES 
('冰鲜柠檬水', 4.00, 999, 105),
('满杯百香果', 8.00, 200, 105),
('魔天脆脆筒', 3.00, 50, 105),
('草莓摇摇奶昔', 7.00, 100, 105),
('棒打鲜橙', 6.00, 150, 105);

SET SQL_SAFE_UPDATES = 0;
UPDATE dish SET stock = 5 WHERE name = '香辣鸡腿堡';

UPDATE dish SET stock = 888 WHERE name = '冰鲜柠檬水';

UPDATE dish SET stock = 0 WHERE name = '魔天脆脆筒';
SET SQL_SAFE_UPDATES = 1;

INSERT INTO sys_user (username, password, role, name) 
VALUES ('admin', '123456', 0, '超级管理员');


SELECT 
    d.merchant_id, 
    u.name AS 店铺名, 
    COUNT(o.order_id) AS 总订单数
FROM trade_order o
LEFT JOIN dish d ON o.dish_name = d.name 
LEFT JOIN sys_user u ON d.merchant_id = u.user_id
WHERE o.status = 2 
GROUP BY d.merchant_id, u.name;