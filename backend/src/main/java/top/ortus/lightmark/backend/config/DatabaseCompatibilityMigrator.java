package top.ortus.lightmark.backend.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Profile("!test")
public class DatabaseCompatibilityMigrator implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(DatabaseCompatibilityMigrator.class);

    private final JdbcTemplate jdbcTemplate;
    private final lightmarkAuthProperties authProperties;

    public DatabaseCompatibilityMigrator(JdbcTemplate jdbcTemplate, lightmarkAuthProperties authProperties) {
        this.jdbcTemplate = jdbcTemplate;
        this.authProperties = authProperties;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("Auth mail config, username={}, fromEmail={}, passwordLength={}",
                authProperties.getMail().getUsername(),
                authProperties.getMail().getFromEmail(),
                authProperties.getMail().getPassword() == null ? null : authProperties.getMail().getPassword().length());
        runStep("add user.avatar column", () -> addColumnIfMissing("user", "avatar", "ALTER TABLE `user` ADD COLUMN avatar VARCHAR(500) DEFAULT ''"));
        runStep("add user.gender column", () -> addColumnIfMissing("user", "gender", "ALTER TABLE `user` ADD COLUMN gender TINYINT DEFAULT 0"));
        runStep("add user.birth_date column", () -> addColumnIfMissing("user", "birth_date", "ALTER TABLE `user` ADD COLUMN birth_date DATE NULL"));
        runStep("add orders.changed_once column", () -> addColumnIfMissing("orders", "changed_once", "ALTER TABLE orders ADD COLUMN changed_once TINYINT DEFAULT 0"));
        runStep("add orders.original_order_no column", () -> addColumnIfMissing("orders", "original_order_no", "ALTER TABLE orders ADD COLUMN original_order_no VARCHAR(32) NULL"));
    }

    private void runStep(String stepName, Runnable step) {
        try {
            step.run();
        } catch (Exception ex) {
            log.warn("Database compatibility step '{}' failed; continuing startup. Cause: {}", stepName, ex.getMessage(), ex);
        }
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
