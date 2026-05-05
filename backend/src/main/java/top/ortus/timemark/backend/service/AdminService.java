package top.ortus.timemark.backend.service;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import top.ortus.timemark.backend.common.PageResponse;
import top.ortus.timemark.backend.dto.UserDTO;
import top.ortus.timemark.backend.dto.admin.AdminOrderDTO;
import top.ortus.timemark.backend.dto.admin.AdminProductDTO;
import top.ortus.timemark.backend.dto.admin.DashboardSummaryDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminService {

    private final JdbcTemplate jdbcTemplate;

    public AdminService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public DashboardSummaryDTO dashboardSummary() {
        Long totalUsers = jdbcTemplate.queryForObject("select count(*) from `user` where deleted = 0", Long.class);
        Long totalOrders = jdbcTemplate.queryForObject("select count(*) from `orders`", Long.class);
        BigDecimal totalRevenue = jdbcTemplate.queryForObject("select coalesce(sum(pay_amount), 0) from `orders`", BigDecimal.class);
        return new DashboardSummaryDTO(
                totalUsers == null ? 0 : totalUsers,
                totalOrders == null ? 0 : totalOrders,
                totalRevenue == null ? BigDecimal.ZERO : totalRevenue
        );
    }

    public PageResponse<AdminProductDTO> listProducts(String productType) {
        String baseSql = "from product";
        Object[] params;
        if (productType == null || productType.isEmpty()) {
            params = new Object[]{};
        } else {
            baseSql += " where product_type = ?";
            params = new Object[]{productType};
        }
        Long total = jdbcTemplate.queryForObject("select count(*) " + baseSql, Long.class, params);
        List<AdminProductDTO> items = jdbcTemplate.query(
                "select * " + baseSql + " order by id",
                new BeanPropertyRowMapper<>(AdminProductDTO.class),
                params
        );
        return new PageResponse<>(total == null ? 0 : total, items);
    }

    public PageResponse<UserDTO> listUsers(String keyword) {
        String baseSql = "from `user` where deleted = 0";
        Object[] params;
        if (keyword == null || keyword.isEmpty()) {
            params = new Object[]{};
        } else {
            baseSql += " and (nickname like ? or phone like ? or email like ?)";
            String like = "%" + keyword + "%";
            params = new Object[]{like, like, like};
        }
        Long total = jdbcTemplate.queryForObject("select count(*) " + baseSql, Long.class, params);
        List<UserDTO> items = jdbcTemplate.query(
                "select id, phone, email, nickname, points, level, status, deleted, create_time, update_time " +
                        baseSql + " order by id",
                new BeanPropertyRowMapper<>(UserDTO.class),
                params
        );
        return new PageResponse<>(total == null ? 0 : total, items);
    }

    public boolean updateUserStatus(String userId, int status, Long adminId) {
        int updated = jdbcTemplate.update(
                "update `user` set status = ?, update_time = ? where id = ?",
                status,
                LocalDateTime.now(),
                userId
        );
        if (updated > 0) {
            insertAdminLog(adminId, "USER_STATUS", "userId=" + userId + ",status=" + status);
            return true;
        }
        return false;
    }

    public PageResponse<AdminOrderDTO> listOrders(Integer status) {
        String baseSql = "from `orders`";
        Object[] params;
        if (status == null) {
            params = new Object[]{};
        } else {
            baseSql += " where status = ?";
            params = new Object[]{status};
        }
        Long total = jdbcTemplate.queryForObject("select count(*) " + baseSql, Long.class, params);
        List<AdminOrderDTO> items = jdbcTemplate.query(
                "select * " + baseSql + " order by id",
                new BeanPropertyRowMapper<>(AdminOrderDTO.class),
                params
        );
        return new PageResponse<>(total == null ? 0 : total, items);
    }

    public boolean updateOrderStatus(String orderId, int status, String cancelReason, Long adminId) {
        int updated = jdbcTemplate.update(
                "update `orders` set status = ?, cancel_reason = ?, update_time = ? where id = ?",
                status,
                cancelReason,
                LocalDateTime.now(),
                orderId
        );
        if (updated > 0) {
            insertAdminLog(adminId, "ORDER_STATUS", "orderId=" + orderId + ",status=" + status);
            return true;
        }
        return false;
    }

    private void insertAdminLog(Long adminId, String operation, String params) {
        jdbcTemplate.update(
                "insert into admin_log (admin_id, operation, params, result, ip, create_time) values (?, ?, ?, ?, ?, ?)",
                adminId == null ? 0 : adminId,
                operation,
                params,
                "success",
                "",
                LocalDateTime.now()
        );
    }
}
