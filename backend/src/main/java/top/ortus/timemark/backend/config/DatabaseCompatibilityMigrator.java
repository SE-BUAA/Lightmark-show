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
        normalizeVacationProducts();
        seedVacationProduct("三亚海岛五日自由行", 2999.00, 30, "三亚", "北京", "2026-06-15", 5, "五星", "海景酒店连住，含接送机", "海岛", "自由行", "亲子");
        seedVacationProduct("云南古城六日跟团游", 2580.00, 24, "丽江", "上海", "2026-06-20", 6, "四星", "丽江大理双城，含经典景点门票", "古城", "跟团游", "摄影");
        seedVacationProduct("成都美食四日私享团", 1880.00, 18, "成都", "广州", "2026-07-02", 4, "四星", "小团出行，城市美食和周边慢游", "美食", "私享团", "城市");
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

    private void normalizeVacationProducts() {
        jdbcTemplate.update("""
            UPDATE product
            SET extra = JSON_SET(
                    COALESCE(extra, JSON_OBJECT()),
                    '$.destination', COALESCE(JSON_UNQUOTE(JSON_EXTRACT(extra, '$.destination')), name),
                    '$.depart_city', COALESCE(JSON_UNQUOTE(JSON_EXTRACT(extra, '$.depart_city')), '北京'),
                    '$.date', COALESCE(JSON_UNQUOTE(JSON_EXTRACT(extra, '$.date')), '2026-07-15'),
                    '$.days', COALESCE(JSON_EXTRACT(extra, '$.days'), 5),
                    '$.hotel_level', COALESCE(JSON_UNQUOTE(JSON_EXTRACT(extra, '$.hotel_level')), '四星'),
                    '$.summary', COALESCE(JSON_UNQUOTE(JSON_EXTRACT(extra, '$.summary')), '精选度假产品')
                ),
                category_tags = COALESCE(category_tags, JSON_ARRAY('度假', '精选')),
                status = COALESCE(status, 1)
            WHERE product_type = 'VACATION'
            """);
    }

    private void seedVacationProduct(String name, double price, int stock, String destination, String departCity, String date, int days, String hotelLevel, String summary, String tag1, String tag2, String tag3) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(1) FROM product WHERE product_type = 'VACATION' AND name = ?",
            Integer.class,
            name
        );
        if (count != null && count > 0) {
            return;
        }
        jdbcTemplate.update("""
            INSERT INTO product (product_type, name, price, stock, sold_count, category_tags, extra, status)
            VALUES ('VACATION', ?, ?, ?, 0, JSON_ARRAY(?, ?, ?),
                    JSON_OBJECT('destination', ?, 'depart_city', ?, 'date', ?, 'days', ?, 'hotel_level', ?, 'summary', ?),
                    1)
            """,
            name, price, stock, tag1, tag2, tag3, destination, departCity, date, days, hotelLevel, summary
        );
    }
}
