-- Mall 订单生命周期数据库变更（MySQL 8）
-- 可重复执行
USE mall;

DELIMITER $$

DROP PROCEDURE IF EXISTS migrate_order_lifecycle$$

CREATE PROCEDURE migrate_order_lifecycle()
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE BINARY table_schema = BINARY DATABASE()
          AND BINARY table_name = BINARY 'oms_order'
          AND BINARY column_name = BINARY 'delivery_company'
    ) THEN
        ALTER TABLE oms_order
            ADD COLUMN delivery_company VARCHAR(64) NULL
                COMMENT '物流公司' AFTER payment_time;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE BINARY table_schema = BINARY DATABASE()
          AND BINARY table_name = BINARY 'oms_order'
          AND BINARY column_name = BINARY 'delivery_sn'
    ) THEN
        ALTER TABLE oms_order
            ADD COLUMN delivery_sn VARCHAR(64) NULL
                COMMENT '物流单号' AFTER delivery_company;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.statistics
        WHERE BINARY table_schema = BINARY DATABASE()
          AND BINARY table_name = BINARY 'oms_order'
          AND BINARY index_name =
              BINARY 'idx_oms_order_timeout_scan'
    ) THEN
        ALTER TABLE oms_order
            ADD INDEX idx_oms_order_timeout_scan (
                status,
                create_time,
                id
            );
    END IF;
END$$

CALL migrate_order_lifecycle()$$
DROP PROCEDURE migrate_order_lifecycle$$

DELIMITER ;

-- order:read
INSERT INTO ums_resource (
    name,
    code,
    url_pattern,
    http_method,
    description,
    status,
    create_time,
    update_time
)
SELECT
    '订单查询',
    'order:read',
    '/api/admin/orders/**',
    'GET',
    '后台订单分页和详情查询',
    1,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1
    FROM ums_resource
    WHERE BINARY code = BINARY 'order:read'
);

UPDATE ums_resource
SET name = '订单查询',
    url_pattern = '/api/admin/orders/**',
    http_method = 'GET',
    description = '后台订单分页和详情查询',
    status = 1,
    update_time = CURRENT_TIMESTAMP
WHERE BINARY code = BINARY 'order:read';

-- order:write
INSERT INTO ums_resource (
    name,
    code,
    url_pattern,
    http_method,
    description,
    status,
    create_time,
    update_time
)
SELECT
    '订单操作',
    'order:write',
    '/api/admin/orders/**',
    'PATCH',
    '后台订单发货操作',
    1,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1
    FROM ums_resource
    WHERE BINARY code = BINARY 'order:write'
);

UPDATE ums_resource
SET name = '订单操作',
    url_pattern = '/api/admin/orders/**',
    http_method = 'PATCH',
    description = '后台订单发货操作',
    status = 1,
    update_time = CURRENT_TIMESTAMP
WHERE BINARY code = BINARY 'order:write';

-- 关联 SUPER_ADMIN 角色
INSERT INTO ums_role_resource_relation (
    role_id,
    resource_id,
    create_time
)
SELECT
    role_data.id,
    resource_data.id,
    CURRENT_TIMESTAMP
FROM ums_role role_data
JOIN ums_resource resource_data
  ON CAST(resource_data.code AS BINARY) IN (
      CAST('order:read' AS BINARY),
      CAST('order:write' AS BINARY)
  )
WHERE CAST(role_data.code AS BINARY) =
      CAST('SUPER_ADMIN' AS BINARY)
  AND NOT EXISTS (
      SELECT 1
      FROM ums_role_resource_relation relation_data
      WHERE relation_data.role_id = role_data.id
        AND relation_data.resource_id = resource_data.id
  );