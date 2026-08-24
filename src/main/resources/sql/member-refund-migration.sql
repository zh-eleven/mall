-- 后台会员管理与订单退款迁移（MySQL 8）
-- 可重复执行；不删除表或数据。应在已有 mall 库及基础 RBAC 表上执行。
USE mall;

CREATE TABLE IF NOT EXISTS oms_order_refund (
    id BIGINT NOT NULL AUTO_INCREMENT,
    refund_sn VARCHAR(64) NOT NULL,
    order_id BIGINT NOT NULL,
    order_sn VARCHAR(64) NOT NULL,
    member_id BIGINT NOT NULL,
    refund_amount DECIMAL(12,2) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0待审核 1已退款 2已拒绝',
    admin_note VARCHAR(500) NULL,
    handle_time DATETIME NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_refund_sn (refund_sn),
    KEY idx_refund_order_id (order_id),
    KEY idx_refund_order_sn (order_sn),
    KEY idx_refund_member_status_time (member_id, status, create_time, id),
    KEY idx_refund_status_time (status, create_time, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DELIMITER $$
DROP PROCEDURE IF EXISTS migrate_member_refund_indexes$$
CREATE PROCEDURE migrate_member_refund_indexes()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = 'oms_order_refund'
          AND index_name = 'uk_refund_sn'
    ) THEN
        ALTER TABLE oms_order_refund
            ADD UNIQUE INDEX uk_refund_sn (refund_sn);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = 'ums_member'
          AND index_name = 'idx_member_status_time'
    ) THEN
        ALTER TABLE ums_member
            ADD INDEX idx_member_status_time (status, create_time, id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = 'oms_order'
          AND index_name IN (
              'idx_order_timeout_scan',
              'idx_oms_order_timeout_scan'
          )
    ) THEN
        ALTER TABLE oms_order
            ADD INDEX idx_order_timeout_scan (status, create_time, id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = 'oms_order_refund'
          AND index_name = 'idx_refund_order_id'
    ) THEN
        ALTER TABLE oms_order_refund
            ADD INDEX idx_refund_order_id (order_id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = 'oms_order_refund'
          AND index_name = 'idx_refund_order_sn'
    ) THEN
        ALTER TABLE oms_order_refund
            ADD INDEX idx_refund_order_sn (order_sn);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = 'oms_order_refund'
          AND index_name = 'idx_refund_member_status_time'
    ) THEN
        ALTER TABLE oms_order_refund
            ADD INDEX idx_refund_member_status_time
                (member_id, status, create_time, id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = 'oms_order_refund'
          AND index_name = 'idx_refund_status_time'
    ) THEN
        ALTER TABLE oms_order_refund
            ADD INDEX idx_refund_status_time (status, create_time, id);
    END IF;
END$$
CALL migrate_member_refund_indexes()$$
DROP PROCEDURE migrate_member_refund_indexes$$
DELIMITER ;

ALTER TABLE oms_order
    MODIFY COLUMN status TINYINT NOT NULL DEFAULT 0
    COMMENT '0待支付 1待发货 2已发货 3已完成 4已取消 5退款处理中 6已退款';

INSERT INTO ums_resource
    (name, code, url_pattern, http_method, description, status)
VALUES
    ('会员查询', 'member:read', '/api/admin/members/**', 'GET', '后台会员查询', 1),
    ('会员管理', 'member:write', '/api/admin/members/**', 'PATCH', '后台会员状态维护', 1),
    ('退款查询', 'refund:read', '/api/admin/refunds/**', 'GET', '后台退款查询', 1),
    ('退款审核', 'refund:write', '/api/admin/refunds/**', 'PATCH', '后台退款审核', 1)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    url_pattern = VALUES(url_pattern),
    http_method = VALUES(http_method),
    description = VALUES(description),
    status = VALUES(status);

INSERT INTO ums_role_resource_relation (role_id, resource_id)
SELECT role_data.id, resource_data.id
FROM ums_role role_data
JOIN ums_resource resource_data
  ON resource_data.code IN (
      'member:read', 'member:write',
      'refund:read', 'refund:write'
  )
WHERE role_data.code = 'SUPER_ADMIN'
  AND NOT EXISTS (
      SELECT 1
      FROM ums_role_resource_relation relation_data
      WHERE relation_data.role_id = role_data.id
        AND relation_data.resource_id = resource_data.id
  );

INSERT INTO ums_role_resource_relation (role_id, resource_id)
SELECT role_data.id, resource_data.id
FROM ums_role role_data
JOIN ums_resource resource_data
  ON resource_data.code IN ('member:read', 'refund:read')
WHERE role_data.code = 'READ_ONLY'
  AND NOT EXISTS (
      SELECT 1
      FROM ums_role_resource_relation relation_data
      WHERE relation_data.role_id = role_data.id
        AND relation_data.resource_id = resource_data.id
  );
