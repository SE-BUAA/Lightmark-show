package top.ortus.timemark.backend.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Profile("!test")
public class DatabaseCompatibilityMigrator implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(DatabaseCompatibilityMigrator.class);
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");
    private static final TypeReference<Map<String, Object>> EXTRA_TYPE = new TypeReference<>() {};
    private static final int BULK_TRAIN_TARGET = 12_000;
    private static final int BULK_VACATION_TARGET = 12_000;
    private static final int BULK_BATCH_SIZE = 1_000;

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public DatabaseCompatibilityMigrator(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(ApplicationArguments args) {
        addColumnIfMissing("user", "avatar", "ALTER TABLE `user` ADD COLUMN avatar VARCHAR(500) DEFAULT ''");
        addColumnIfMissing("user", "gender", "ALTER TABLE `user` ADD COLUMN gender TINYINT DEFAULT 0");
        addColumnIfMissing("user", "birth_date", "ALTER TABLE `user` ADD COLUMN birth_date DATE NULL");
        addColumnIfMissing("orders", "changed_once", "ALTER TABLE orders ADD COLUMN changed_once TINYINT DEFAULT 0");
        addColumnIfMissing("orders", "original_order_no", "ALTER TABLE orders ADD COLUMN original_order_no VARCHAR(32) NULL");
        normalizeVacationProducts();
        normalizeVacationDays();
        normalizeBulkTrainNames();
        seedVacationProduct("三亚海岛五日自由行", 2999.00, 30, "三亚", "北京", "2026-06-15", 5, "五星", "海景酒店连住，含接送机", "海岛", "自由行", "亲子");
        seedVacationProduct("云南古城六日跟团游", 2580.00, 24, "丽江", "上海", "2026-06-20", 6, "四星", "丽江大理双城，含经典景点门票", "古城", "跟团游", "摄影");
        seedVacationProduct("成都美食四日私享团", 1880.00, 18, "成都", "广州", "2026-07-02", 4, "四星", "小团出行，城市美食和周边慢游", "美食", "私享团", "城市");
        seedBulkVacationProducts();
        seedBulkTrainProducts();
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

    private void normalizeVacationDays() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT id, extra FROM product WHERE product_type = 'VACATION' AND extra IS NOT NULL"
        );
        for (Map<String, Object> row : rows) {
            Object id = row.get("id");
            Object extraValue = row.get("extra");
            if (id == null || extraValue == null) {
                continue;
            }
            try {
                Map<String, Object> extra = objectMapper.readValue(String.valueOf(extraValue), EXTRA_TYPE);
                Integer days = parseDays(extra.get("days"));
                if (days == null) {
                    continue;
                }
                Object currentDays = extra.get("days");
                if (currentDays instanceof Number number && number.intValue() == days) {
                    continue;
                }
                Map<String, Object> normalizedExtra = new HashMap<>(extra);
                normalizedExtra.put("days", days);
                jdbcTemplate.update(
                    "UPDATE product SET extra = ? WHERE id = ?",
                    objectMapper.writeValueAsString(normalizedExtra),
                    id
                );
            } catch (Exception ignored) {
                // Startup should not fail because one legacy product has malformed JSON.
            }
        }
    }

    private Integer parseDays(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            int days = number.intValue();
            return days > 0 ? days : null;
        }
        Matcher matcher = NUMBER_PATTERN.matcher(String.valueOf(value));
        if (!matcher.find()) {
            return null;
        }
        try {
            int days = Integer.parseInt(matcher.group());
            return days > 0 ? days : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void normalizeBulkTrainNames() {
        jdbcTemplate.update("""
            UPDATE product
            SET name = REPLACE(name, '批量火车票-', '')
            WHERE product_type = 'TRAIN'
              AND name LIKE '批量火车票-%'
            """);
    }

    private void seedBulkTrainProducts() {
        int existing = countProducts("TRAIN");
        if (existing >= BULK_TRAIN_TARGET) {
            log.info("TRAIN seed skipped, existing products: {}, target: {}", existing, BULK_TRAIN_TARGET);
            return;
        }
        log.info("Seeding TRAIN products, existing: {}, target: {}, need: {}", existing, BULK_TRAIN_TARGET, BULK_TRAIN_TARGET - existing);
        String[] startStations = {"北京南", "上海虹桥", "广州南", "深圳北", "杭州东", "南京南", "武汉", "西安北", "成都东", "重庆北", "郑州东", "长沙南", "天津西", "青岛北", "厦门北", "昆明南", "南昌西", "合肥南", "苏州北", "济南西"};
        String[] endStations = {"上海虹桥", "北京南", "深圳北", "广州南", "南京南", "杭州东", "成都东", "西安北", "重庆北", "武汉", "长沙南", "郑州东", "青岛北", "天津西", "昆明南", "厦门北", "合肥南", "南昌西", "济南西", "苏州北"};
        String[] trainTypes = {"高铁", "动车", "普速"};
        String[][] seatTypes = {
            {"商务座", "一等座", "二等座"},
            {"一等座", "二等座"},
            {"硬卧", "软卧", "硬座"}
        };
        List<Object[]> batch = new ArrayList<>(BULK_BATCH_SIZE);
        for (int i = existing; i < BULK_TRAIN_TARGET; i++) {
            int seedIndex = i;
            int typeIndex = seedIndex % trainTypes.length;
            String trainType = trainTypes[typeIndex];
            String start = startStations[seedIndex % startStations.length];
            String end = endStations[(seedIndex * 7 + 3) % endStations.length];
            if (start.equals(end)) {
                end = endStations[(seedIndex + 1) % endStations.length];
            }
            LocalDate date = LocalDate.of(2026, 6, 1).plusDays(seedIndex % 120);
            LocalTime departTime = LocalTime.of(6 + (seedIndex % 16), (seedIndex * 7) % 60);
            LocalTime arriveTime = departTime.plusHours(2 + (seedIndex % 10)).plusMinutes((seedIndex * 11) % 60);
            int soldCount = seedIndex % 40;
            int stock = soldCount + 60 + (seedIndex % 180);
            double price = switch (trainType) {
                case "高铁" -> 180 + (seedIndex % 140) * 4.5;
                case "动车" -> 120 + (seedIndex % 120) * 3.2;
                default -> 55 + (seedIndex % 100) * 1.8;
            };
            String trainCode = switch (trainType) {
                case "高铁" -> "G" + (1000 + seedIndex);
                case "动车" -> "D" + (1000 + seedIndex);
                default -> "K" + (1000 + seedIndex);
            };
            List<String> tags = new ArrayList<>();
            tags.add(trainType);
            tags.addAll(List.of(seatTypes[typeIndex]));
            batch.add(new Object[] {
                "TRAIN",
                trainCode + "次",
                roundPrice(price),
                stock,
                soldCount,
                toJson(tags),
                toJson(Map.of(
                    "start_station", start,
                    "end_station", end,
                    "date", date.toString(),
                    "depart_time", formatTime(departTime),
                    "arrive_time", formatTime(arriveTime)
                )),
                1
            });
            flushProductBatch(batch);
        }
        insertProductBatch(batch);
        log.info("TRAIN seed finished, current products: {}", countProducts("TRAIN"));
    }

    private void seedBulkVacationProducts() {
        int existing = countProducts("VACATION");
        if (existing >= BULK_VACATION_TARGET) {
            log.info("VACATION seed skipped, existing products: {}, target: {}", existing, BULK_VACATION_TARGET);
            return;
        }
        log.info("Seeding VACATION products, existing: {}, target: {}, need: {}", existing, BULK_VACATION_TARGET, BULK_VACATION_TARGET - existing);
        String[] destinations = {"三亚", "丽江", "成都", "厦门", "杭州", "桂林", "大理", "西安", "青岛", "重庆", "张家界", "昆明", "苏州", "哈尔滨", "珠海", "北海", "敦煌", "长沙", "南京", "黄山"};
        String[] departCities = {"北京", "上海", "广州", "深圳", "杭州", "南京", "武汉", "西安", "成都", "重庆"};
        String[] themes = {"海岛", "亲子", "自由行", "跟团游", "美食", "摄影", "古城", "温泉", "滑雪", "山水", "文化", "小众"};
        String[] suffixes = {"轻奢假期", "深度游", "周末慢游", "亲子甄选", "私享团", "自由行", "经典跟团", "美食探索", "摄影之旅", "度假套餐"};
        String[] hotelLevels = {"三星", "四星", "五星", "精品民宿", "度假酒店"};
        List<Object[]> batch = new ArrayList<>(BULK_BATCH_SIZE);
        for (int i = existing; i < BULK_VACATION_TARGET; i++) {
            int seedIndex = i;
            String destination = destinations[seedIndex % destinations.length];
            String departCity = departCities[(seedIndex * 5 + 2) % departCities.length];
            int days = 2 + (seedIndex % 8);
            LocalDate date = LocalDate.of(2026, 6, 1).plusDays(seedIndex % 150);
            int soldCount = seedIndex % 35;
            int stock = soldCount + 20 + (seedIndex % 120);
            double price = 699 + days * 280 + (seedIndex % 90) * 18.5;
            String theme1 = themes[seedIndex % themes.length];
            String theme2 = themes[(seedIndex * 3 + 4) % themes.length];
            String theme3 = themes[(seedIndex * 5 + 7) % themes.length];
            String hotelLevel = hotelLevels[seedIndex % hotelLevels.length];
            String name = destination + days + "日" + suffixes[seedIndex % suffixes.length] + "-" + (10000 + seedIndex);
            batch.add(new Object[] {
                "VACATION",
                name,
                roundPrice(price),
                stock,
                soldCount,
                toJson(List.of(theme1, theme2, theme3)),
                toJson(Map.of(
                    "destination", destination,
                    "depart_city", departCity,
                    "date", date.toString(),
                    "days", days,
                    "hotel_level", hotelLevel,
                    "summary", destination + days + "日行程，覆盖" + theme1 + "、" + theme2 + "主题体验"
                )),
                1
            });
            flushProductBatch(batch);
        }
        insertProductBatch(batch);
        log.info("VACATION seed finished, current products: {}", countProducts("VACATION"));
    }

    private int countProducts(String productType) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(1) FROM product WHERE UPPER(product_type) = ?",
            Integer.class,
            productType.toUpperCase()
        );
        return count == null ? 0 : count;
    }

    private void flushProductBatch(List<Object[]> batch) {
        if (batch.size() < BULK_BATCH_SIZE) {
            return;
        }
        insertProductBatch(batch);
        batch.clear();
    }

    private void insertProductBatch(List<Object[]> batch) {
        if (batch.isEmpty()) {
            return;
        }
        jdbcTemplate.batchUpdate("""
            INSERT INTO product (product_type, name, price, stock, sold_count, category_tags, extra, status)
            VALUES (?, ?, ?, ?, ?, JSON_EXTRACT(?, '$'), JSON_EXTRACT(?, '$'), ?)
            """, batch);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize seed product JSON", ex);
        }
    }

    private double roundPrice(double price) {
        return Math.round(price * 100.0) / 100.0;
    }

    private String formatTime(LocalTime time) {
        return time.getHour() + ":" + String.format("%02d", time.getMinute());
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
