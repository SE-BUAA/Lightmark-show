package top.ortus.timemark.backend.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminProductService {
    private final JdbcTemplate jdbcTemplate;
    private final AdminLogService adminLogService;

    public AdminProductService(JdbcTemplate jdbcTemplate, AdminLogService adminLogService) {
        this.jdbcTemplate = jdbcTemplate;
        this.adminLogService = adminLogService;
    }

    public Map<String, Object> list(int page, int size, String keyword, String productType, Integer status) {
        int offset = Math.max(page - 1, 0) * size;
        StringBuilder sql = new StringBuilder(
                """
                SELECT id, product_type, name, price, stock, sold_count, status, update_time
                FROM product
                WHERE 1 = 1
                """
        );
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND name LIKE ?");
            params.add("%" + keyword.trim() + "%");
        }
        if (productType != null && !productType.isBlank()) {
            sql.append(" AND product_type = ?");
            params.add(productType.trim());
        }
        if (status != null) {
            sql.append(" AND status = ?");
            params.add(status);
        }
        sql.append(" ORDER BY id DESC LIMIT ? OFFSET ?");
        params.add(size);
        params.add(offset);
        List<Map<String, Object>> items = jdbcTemplate.queryForList(
                sql.toString(),
                params.toArray()
        );
        StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM product WHERE 1 = 1");
        List<Object> countParams = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            countSql.append(" AND name LIKE ?");
            countParams.add("%" + keyword.trim() + "%");
        }
        if (productType != null && !productType.isBlank()) {
            countSql.append(" AND product_type = ?");
            countParams.add(productType.trim());
        }
        if (status != null) {
            countSql.append(" AND status = ?");
            countParams.add(status);
        }
        Long total = jdbcTemplate.queryForObject(countSql.toString(), Long.class, countParams.toArray());
        return pageData(items, page, size, total == null ? 0L : total);
    }

    public void updateStatus(Long id, Integer status, String ip) {
        jdbcTemplate.update("UPDATE product SET status = ?, update_time = NOW() WHERE id = ?", status, id);
        adminLogService.record("PRODUCT_STATUS", "productId=" + id + ",status=" + status, "success", ip);
    }

    private Map<String, Object> pageData(List<Map<String, Object>> items, int page, int size, long total) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("items", items);
        result.put("page", page);
        result.put("size", size);
        result.put("total", total);
        return result;
    }
}

