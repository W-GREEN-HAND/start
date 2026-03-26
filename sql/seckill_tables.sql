CREATE TABLE IF NOT EXISTS seckill_activity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '秒杀活动ID',
    goods_id BIGINT NOT NULL COMMENT '商品ID',
    seckill_price DECIMAL(10, 2) NOT NULL COMMENT '秒杀价',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    limit_per_user INT NOT NULL DEFAULT 1 COMMENT '每人限购数量',
    status INT NOT NULL DEFAULT 1 COMMENT '状态（0-下线，1-上线）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_goods_id (goods_id),
    INDEX idx_time (start_time, end_time),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='秒杀活动表';

CREATE TABLE IF NOT EXISTS seckill_stock (
    activity_id BIGINT PRIMARY KEY COMMENT '活动ID',
    initial_stock INT NOT NULL COMMENT '初始库存',
    available_stock INT NOT NULL COMMENT '可用库存',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT fk_seckill_stock_activity_id FOREIGN KEY (activity_id) REFERENCES seckill_activity(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='秒杀库存表';

CREATE TABLE IF NOT EXISTS seckill_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '秒杀订单ID',
    request_id VARCHAR(64) NOT NULL COMMENT '请求ID（幂等键）',
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    order_id BIGINT NULL COMMENT '订单ID（t_order.id）',
    status INT NOT NULL DEFAULT 0 COMMENT '状态（0-处理中，1-成功，2-失败）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_request_id (request_id),
    UNIQUE KEY uk_activity_user (activity_id, user_id),
    INDEX idx_user_id (user_id),
    INDEX idx_activity_id (activity_id),
    CONSTRAINT fk_seckill_order_activity_id FOREIGN KEY (activity_id) REFERENCES seckill_activity(id),
    CONSTRAINT fk_seckill_order_order_id FOREIGN KEY (order_id) REFERENCES t_order(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='秒杀订单映射表';
