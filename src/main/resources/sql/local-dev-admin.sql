-- 仅本地开发：初始化一个 admin 管理员并授予 SUPER_ADMIN。
-- 密码字段仅保存 BCrypt 哈希；请在首次登录后立即修改密码。
-- 生产环境禁止执行本脚本。
USE mall;

INSERT INTO ums_admin (
    username,
    password,
    nickname,
    status,
    create_time,
    update_time
)
SELECT
    'admin',
    '$2a$10$FAPucWDSIBq4EflqxOHfeOKiFYJ.rLTXAc139LJQbhqVyuVtVduGa',
    '本地开发管理员',
    1,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1
    FROM ums_admin
    WHERE username = 'admin'
);

INSERT INTO ums_admin_role_relation (
    admin_id,
    role_id,
    create_time
)
SELECT
    admin_data.id,
    role_data.id,
    CURRENT_TIMESTAMP
FROM ums_admin admin_data
JOIN ums_role role_data ON role_data.code = 'SUPER_ADMIN'
WHERE admin_data.username = 'admin'
  AND NOT EXISTS (
      SELECT 1
      FROM ums_admin_role_relation relation_data
      WHERE relation_data.admin_id = admin_data.id
        AND relation_data.role_id = role_data.id
  );
