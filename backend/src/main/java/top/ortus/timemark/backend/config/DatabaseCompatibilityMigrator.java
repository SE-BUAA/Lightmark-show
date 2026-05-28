package top.ortus.timemark.backend.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class DatabaseCompatibilityMigrator implements ApplicationRunner {
    private final JdbcTemplate jdbcTemplate;

    public DatabaseCompatibilityMigrator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        addColumnIfMissing("orders", "changed_once", "ALTER TABLE orders ADD COLUMN changed_once TINYINT DEFAULT 0");
        addColumnIfMissing("orders", "original_order_no", "ALTER TABLE orders ADD COLUMN original_order_no VARCHAR(32) NULL");
    }

    private void addColumnIfMissing(String tableName, String columnName, String ddl) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(1) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?",
            Integer.class,
            tableName,
            columnName
        );
        if (count == null || count == 0) {
            jdbcTemplate.execute(ddl);
        }
    }
}
