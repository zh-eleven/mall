-- 基础 RBAC 数据。先执行 schema.sql；本脚本不创建管理员账号。
USE mall;

INSERT INTO ums_role (name, code, description, status, sort)
VALUES
    ('超级管理员', 'SUPER_ADMIN', '拥有全部后台权限', 1, 0),
    ('后台只读管理员', 'READ_ONLY', '只能查看后台数据', 1, 20)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    description = VALUES(description),
    status = VALUES(status),
    sort = VALUES(sort);

INSERT INTO ums_resource
    (name, code, url_pattern, http_method, description, status)
VALUES
    ('查看管理员', 'admin:read', '/api/admin/users/**', 'GET', '后台管理员查询', 1),
    ('管理管理员', 'admin:write', '/api/admin/users/**', 'ALL', '后台管理员维护', 1),
    ('查看角色', 'role:read', '/api/admin/roles/**', 'GET', '角色查询', 1),
    ('管理角色', 'role:write', '/api/admin/roles/**', 'ALL', '角色维护', 1),
    ('查看资源', 'resource:read', '/api/admin/resources/**', 'GET', '权限资源查询', 1),
    ('管理资源', 'resource:write', '/api/admin/resources/**', 'ALL', '权限资源维护', 1),
    ('品牌查询', 'brand:read', '/api/admin/brands/**', 'GET', '商品品牌查询', 1),
    ('品牌编辑', 'brand:write', '/api/admin/brands/**', 'ALL', '商品品牌维护', 1),
    ('商品分类查询', 'category:read', '/api/admin/product-categories/**', 'GET', '商品分类查询', 1),
    ('商品分类编辑', 'category:write', '/api/admin/product-categories/**', 'ALL', '商品分类维护', 1),
    ('商品属性查看', 'attribute:read', '/api/admin/**', 'GET', '商品属性查询', 1),
    ('商品属性编辑', 'attribute:write', '/api/admin/**', 'ALL', '商品属性维护', 1),
    ('商品查看', 'product:read', '/api/admin/products/**', 'GET', '商品和SKU查询', 1),
    ('商品编辑', 'product:write', '/api/admin/products/**', 'ALL', '商品和SKU维护', 1),
    ('订单查询', 'order:read', '/api/admin/orders/**', 'GET', '后台订单查询', 1),
    ('订单操作', 'order:write', '/api/admin/orders/**', 'PATCH', '后台订单发货', 1),
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
CROSS JOIN ums_resource resource_data
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
  ON resource_data.code LIKE '%:read'
WHERE role_data.code = 'READ_ONLY'
  AND NOT EXISTS (
      SELECT 1
      FROM ums_role_resource_relation relation_data
      WHERE relation_data.role_id = role_data.id
        AND relation_data.resource_id = resource_data.id
  );
