USE mall;
INSERT INTO ums_resource
(name, code, url_pattern, http_method, description, status)
VALUES
    (
        '秒杀活动查询',
        'seckill:read',
        '/api/admin/seckill/**',
        'GET',
        '查询秒杀活动',
        1
    ),
    (
        '秒杀活动管理',
        'seckill:write',
        '/api/admin/seckill/**',
        'ALL',
        '维护秒杀活动',
        1
    )
ON DUPLICATE KEY UPDATE
                     name = VALUES(name),
                     url_pattern = VALUES(url_pattern),
                     http_method = VALUES(http_method),
                     description = VALUES(description),
                     status = VALUES(status);

INSERT INTO ums_role_resource_relation (
    role_id,
    resource_id
)
SELECT role_data.id, resource_data.id
FROM ums_role role_data
         CROSS JOIN ums_resource resource_data
WHERE role_data.code = 'SUPER_ADMIN'
  AND resource_data.code IN (
                             'seckill:read',
                             'seckill:write'
    )
  AND NOT EXISTS (
    SELECT 1
    FROM ums_role_resource_relation relation_data
    WHERE relation_data.role_id = role_data.id
      AND relation_data.resource_id = resource_data.id
);

CREATE TABLE IF NOT EXISTS oms_seckill_order (
                                                 id BIGINT NOT NULL AUTO_INCREMENT,
                                                 request_id VARCHAR(64) NOT NULL,
                                                 order_id BIGINT NOT NULL,
                                                 activity_id BIGINT NOT NULL,
                                                 seckill_sku_id BIGINT NOT NULL,
                                                 member_id BIGINT NOT NULL,
                                                 quantity INT UNSIGNED NOT NULL DEFAULT 1,
                                                 create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                                 PRIMARY KEY (id),

                                                 UNIQUE KEY uk_seckill_request (
                                                                                request_id,
                                                                                seckill_sku_id,
                                                                                member_id
                                                     ),

                                                 UNIQUE KEY uk_seckill_order (
                                                                              order_id
                                                     ),

                                                 UNIQUE KEY uk_seckill_member_sku (
                                                                                   member_id,
                                                                                   seckill_sku_id
                                                     ),

                                                 KEY idx_seckill_activity (
                                                                           activity_id
                                                     ),

                                                 CONSTRAINT fk_seckill_order_order
                                                     FOREIGN KEY (order_id)
                                                         REFERENCES oms_order (id),

                                                 CONSTRAINT fk_seckill_order_activity
                                                     FOREIGN KEY (activity_id)
                                                         REFERENCES sms_seckill_activity (id),

                                                 CONSTRAINT fk_seckill_order_sku
                                                     FOREIGN KEY (seckill_sku_id)
                                                         REFERENCES sms_seckill_sku (id),

                                                 CONSTRAINT fk_seckill_order_member
                                                     FOREIGN KEY (member_id)
                                                         REFERENCES ums_member (id),

                                                 CONSTRAINT chk_seckill_order_quantity
                                                     CHECK (quantity = 1)
);


CREATE TABLE IF NOT EXISTS oms_seckill_failure (
                                                   id BIGINT NOT NULL AUTO_INCREMENT,
                                                   request_id VARCHAR(64) NOT NULL,
                                                   seckill_sku_id BIGINT NOT NULL,
                                                   member_id BIGINT NOT NULL,
                                                   failure_reason VARCHAR(1000) NULL,
                                                   status TINYINT NOT NULL DEFAULT 0
                                                       COMMENT '0待补偿 1已补偿',
                                                   retry_count INT UNSIGNED NOT NULL DEFAULT 0,
                                                   last_retry_time DATETIME NULL,
                                                   create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                   update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                                                       ON UPDATE CURRENT_TIMESTAMP,

                                                   PRIMARY KEY (id),

                                                   UNIQUE KEY uk_seckill_failure_request (
                                                                                          request_id,
                                                                                          seckill_sku_id,
                                                                                          member_id
                                                       ),

                                                   KEY idx_seckill_failure_status_time (
                                                                                        status,
                                                                                        update_time,
                                                                                        id
                                                       ),

                                                   CONSTRAINT fk_seckill_failure_sku
                                                       FOREIGN KEY (seckill_sku_id)
                                                           REFERENCES sms_seckill_sku (id),

                                                   CONSTRAINT fk_seckill_failure_member
                                                       FOREIGN KEY (member_id)
                                                           REFERENCES ums_member (id),

                                                   CONSTRAINT chk_seckill_failure_status
                                                       CHECK (status IN (0, 1))
);

-- 兼容已经执行过早期秒杀脚本的数据库：将requestId幂等范围
-- 统一为“请求ID + 秒杀SKU + 会员”，与Redis键和查询接口一致。
SET @seckill_order_request_columns = (
    SELECT GROUP_CONCAT(column_name ORDER BY seq_in_index)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'oms_seckill_order'
      AND index_name = 'uk_seckill_request'
);
SET @seckill_order_request_sql = IF(
    @seckill_order_request_columns = 'request_id',
    'ALTER TABLE oms_seckill_order DROP INDEX uk_seckill_request, ADD UNIQUE KEY uk_seckill_request (request_id, seckill_sku_id, member_id)',
    'SELECT 1'
);
PREPARE seckill_order_request_stmt
    FROM @seckill_order_request_sql;
EXECUTE seckill_order_request_stmt;
DEALLOCATE PREPARE seckill_order_request_stmt;

SET @seckill_failure_request_columns = (
    SELECT GROUP_CONCAT(column_name ORDER BY seq_in_index)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'oms_seckill_failure'
      AND index_name = 'uk_seckill_failure_request'
);
SET @seckill_failure_request_sql = IF(
    @seckill_failure_request_columns = 'request_id',
    'ALTER TABLE oms_seckill_failure DROP INDEX uk_seckill_failure_request, ADD UNIQUE KEY uk_seckill_failure_request (request_id, seckill_sku_id, member_id)',
    'SELECT 1'
);
PREPARE seckill_failure_request_stmt
    FROM @seckill_failure_request_sql;
EXECUTE seckill_failure_request_stmt;
DEALLOCATE PREPARE seckill_failure_request_stmt;
