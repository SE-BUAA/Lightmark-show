package top.ortus.timemark.backend.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import top.ortus.timemark.backend.security.AuthGuards;

@Service
public class AdminLogService {
    private final JdbcTemplate jdbcTemplate;

    public AdminLogService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void record(String operation, String params, String result, String ip) {
        Long adminId = AuthGuards.currentUser().userId();
        jdbcTemplate.update(
                """
                INSERT INTO admin_log (admin_id, operation, params, result, ip, create_time)
                VALUES (?, ?, ?, ?, ?, NOW())
                """,
                adminId,
                operation,
                params,
                result,
                ip
        );
    }
}

