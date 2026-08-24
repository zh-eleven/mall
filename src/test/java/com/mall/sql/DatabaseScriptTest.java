package com.mall.sql;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseScriptTest {

    @Test
    void schemaShouldCoverEveryEntityTableWithoutDestructiveSql()
            throws IOException {
        String sql = resource("sql/schema.sql");

        assertEquals(19, occurrences(sql, "CREATE TABLE IF NOT EXISTS"));
        assertTrue(sql.contains("CREATE TABLE IF NOT EXISTS ums_member"));
        assertTrue(sql.contains("CREATE TABLE IF NOT EXISTS oms_order_refund"));
        assertTrue(sql.contains("CREATE TABLE IF NOT EXISTS pms_sku_stock"));
        assertTrue(sql.contains("idx_order_timeout_scan"));
        assertTrue(sql.contains("idx_refund_member_status_time"));
        assertFalse(sql.toUpperCase().contains("DROP TABLE"));
        assertFalse(sql.toUpperCase().contains("DELETE FROM"));
    }

    @Test
    void rbacAndMigrationShouldContainNewPermissionsAndBeRepeatable()
            throws IOException {
        String rbac = resource("sql/rbac-data.sql");
        String migration = resource("sql/member-refund-migration.sql");

        for (String permission : new String[]{
                "member:read", "member:write",
                "refund:read", "refund:write"
        }) {
            assertTrue(rbac.contains(permission));
            assertTrue(migration.contains(permission));
        }

        assertTrue(rbac.contains("ON DUPLICATE KEY UPDATE"));
        assertTrue(migration.contains("CREATE TABLE IF NOT EXISTS"));
        assertTrue(migration.contains("information_schema.statistics"));
        assertFalse(migration.toUpperCase().contains("DROP TABLE"));
    }

    @Test
    void localDevelopmentAdminShouldOnlyStoreBcryptHash()
            throws IOException {
        String sql = resource("sql/local-dev-admin.sql");

        assertTrue(sql.contains("仅本地开发"));
        assertTrue(sql.contains("$2a$10$"));
        assertFalse(sql.contains("'123456'"));
        assertFalse(sql.toUpperCase().contains("DROP TABLE"));
    }

    private String resource(String name) throws IOException {
        try (var input = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(name)) {
            if (input == null) {
                throw new IOException("资源不存在: " + name);
            }
            return new String(
                    input.readAllBytes(),
                    StandardCharsets.UTF_8
            );
        }
    }

    private int occurrences(String content, String token) {
        int count = 0;
        int index = 0;
        while ((index = content.indexOf(token, index)) >= 0) {
            count++;
            index += token.length();
        }
        return count;
    }
}
