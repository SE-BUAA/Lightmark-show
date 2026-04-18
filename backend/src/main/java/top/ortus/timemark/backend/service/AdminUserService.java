package top.ortus.timemark.backend.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminUserService {
    private final JdbcTemplate jdbcTemplate;
    private final AdminLogService adminLogService;

    public AdminUserService(JdbcTemplate jdbcTemplate, AdminLogService adminLogService) {
        this.jdbcTemplate = jdbcTemplate;
        this.adminLogService = adminLogService;
    }

    public Map<String, Object> list(int page, int size, String keyword, Integer status, Integer level) {
        int offset = Math.max(page - 1, 0) * size;
        StringBuilder sql = new StringBuilder(
                """
                SELECT id, phone, email, nickname, points, level, status, create_time
                FROM `user`
                WHERE deleted = 0
                """
        );
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (nickname LIKE ? OR phone LIKE ? OR email LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (status != null) {
            sql.append(" AND status = ?");
            params.add(status);
        }
        if (level != null) {
            sql.append(" AND level = ?");
            params.add(level);
        }
        sql.append(" ORDER BY id DESC LIMIT ? OFFSET ?");
        params.add(size);
        params.add(offset);
        List<Map<String, Object>> items = jdbcTemplate.queryForList(
                sql.toString(),
                params.toArray()
        );
        StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM `user` WHERE deleted = 0");
        List<Object> countParams = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            countSql.append(" AND (nickname LIKE ? OR phone LIKE ? OR email LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            countParams.add(like);
            countParams.add(like);
            countParams.add(like);
        }
        if (status != null) {
            countSql.append(" AND status = ?");
            countParams.add(status);
        }
        if (level != null) {
            countSql.append(" AND level = ?");
            countParams.add(level);
        }
        Long total = jdbcTemplate.queryForObject(countSql.toString(), Long.class, countParams.toArray());
        return pageData(items, page, size, total == null ? 0L : total);
    }

    public void updateStatus(Long id, Integer status, String ip) {
        jdbcTemplate.update("UPDATE `user` SET status = ?, update_time = NOW() WHERE id = ?", status, id);
        adminLogService.record("USER_STATUS", "userId=" + id + ",status=" + status, "success", ip);
    }

    public void updateLevel(Long id, Integer level, String ip) {
        jdbcTemplate.update("UPDATE `user` SET level = ?, update_time = NOW() WHERE id = ?", level, id);
        adminLogService.record("USER_LEVEL", "userId=" + id + ",level=" + level, "success", ip);
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

