package top.ortus.timemark.backend.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class AdminDashboardService {
    private final JdbcTemplate jdbcTemplate;

    public AdminDashboardService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> summary() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalUsers", queryLong("SELECT COUNT(*) FROM `user` WHERE deleted = 0"));
        data.put("totalOrders", queryLong("SELECT COUNT(*) FROM orders"));
        data.put("paidOrders", queryLong("SELECT COUNT(*) FROM orders WHERE status = 1"));
        data.put("gmv", queryDecimal("SELECT COALESCE(SUM(pay_amount), 0) FROM orders WHERE status IN (1,2)"));
        data.put("activeProducts", queryLong("SELECT COUNT(*) FROM product WHERE status = 1"));
        data.put("hotDestinations", jdbcTemplate.queryForList(
                "SELECT name, sold_count FROM product ORDER BY sold_count DESC LIMIT 5"
        ));
        return data;
    }

    private Long queryLong(String sql) {
        Number value = jdbcTemplate.queryForObject(sql, Number.class);
        return value == null ? 0L : value.longValue();
    }

    private BigDecimal queryDecimal(String sql) {
        BigDecimal value = jdbcTemplate.queryForObject(sql, BigDecimal.class);
        return value == null ? BigDecimal.ZERO : value;
    }
}

