USE mall;

CREATE TABLE IF NOT EXISTS sms_seckill_activity (
                                      id BIGINT NOT NULL AUTO_INCREMENT,
                                      name VARCHAR(100) NOT NULL,
                                      start_time DATETIME NOT NULL,
                                      end_time DATETIME NOT NULL,
                                      status TINYINT NOT NULL DEFAULT 0
                                          COMMENT '0未启用 1已启用 2已结束',
                                      create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                                          ON UPDATE CURRENT_TIMESTAMP,
                                      PRIMARY KEY (id),
                                      KEY idx_status_time (status, start_time, end_time),
                                      CONSTRAINT chk_activity_time
                                          CHECK (end_time > start_time),
                                      CONSTRAINT chk_seckill_activity_status
                                          CHECK (status IN (0, 1, 2))
);

CREATE TABLE IF NOT EXISTS sms_seckill_sku (
                                 id BIGINT NOT NULL AUTO_INCREMENT,
                                 activity_id BIGINT NOT NULL,
                                 product_id BIGINT NOT NULL,
                                 sku_id BIGINT NOT NULL,
                                 seckill_price DECIMAL(10,2) NOT NULL,
                                 total_stock INT UNSIGNED NOT NULL,
                                 available_stock INT UNSIGNED NOT NULL,
                                 per_user_limit INT UNSIGNED NOT NULL DEFAULT 1,
                                 create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                                     ON UPDATE CURRENT_TIMESTAMP,
                                 PRIMARY KEY (id),
                                 UNIQUE KEY uk_activity_sku (activity_id, sku_id),
                                 CONSTRAINT fk_seckill_activity
                                     FOREIGN KEY (activity_id)
                                         REFERENCES sms_seckill_activity (id),
                                 CONSTRAINT fk_seckill_product
                                     FOREIGN KEY (product_id)
                                         REFERENCES pms_product (id),
                                 CONSTRAINT fk_seckill_sku
                                     FOREIGN KEY (sku_id)
                                         REFERENCES pms_sku_stock (id),
                                 CONSTRAINT chk_seckill_stock
                                     CHECK (
                                         total_stock > 0
                                             AND available_stock <= total_stock
                                         ),
                                 CONSTRAINT chk_seckill_limit
                                     CHECK (per_user_limit = 1)
);
