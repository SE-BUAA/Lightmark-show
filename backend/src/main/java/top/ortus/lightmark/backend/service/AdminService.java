package top.ortus.lightmark.backend.service;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import top.ortus.lightmark.backend.common.PageResponse;
import top.ortus.lightmark.backend.dto.UserDTO;
import top.ortus.lightmark.backend.dto.admin.AdminOrderDTO;
import top.ortus.lightmark.backend.dto.admin.AdminProductDTO;
import top.ortus.lightmark.backend.dto.admin.DashboardSummaryDTO;
import top.ortus.lightmark.backend.dto.admin.DashboardTrendDTO;
import top.ortus.lightmark.backend.dto.admin.HotProductDTO;
import top.ortus.lightmark.backend.dto.module.AdminLogDTO;
import top.ortus.lightmark.backend.dto.module.ProductDTO;
import top.ortus.lightmark.backend.exception.ApiException;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 管理员服务类，提供后台管理相关的业务逻辑
 */
@Service
public class AdminService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 构造函数
     * @param jdbcTemplate JDBC 模板
     */
    public AdminService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 获取仪表板摘要信息
     * @return 包含总用户数、总订单数和总收入的摘要信息
     */
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

    /**
     * 近 7 天交易趋势。
     * @return 趋势数据
     */
    public DashboardTrendDTO dashboardTrends() {
        List<String> dates = new ArrayList<>();
        List<Integer> orderCounts = new ArrayList<>();
        List<BigDecimal> revenues = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            dates.add(day.toString());

            Date sqlDate = Date.valueOf(day);
            Integer count = jdbcTemplate.queryForObject(
                    "select count(*) from `orders` where date(create_time) = ?",
                    Integer.class,
                    sqlDate
            );
            BigDecimal revenue = jdbcTemplate.queryForObject(
                    "select coalesce(sum(pay_amount), 0) from `orders` where date(create_time) = ?",
                    BigDecimal.class,
                    sqlDate
            );
            orderCounts.add(count == null ? 0 : count);
            revenues.add(revenue == null ? BigDecimal.ZERO : revenue);
        }

        return new DashboardTrendDTO(dates, orderCounts, revenues);
    }

    /**
     * 热门产品 Top10。
     * @return 热门产品列表
     */
    public List<HotProductDTO> hotProducts() {
        return jdbcTemplate.query(
                "select id, name, sold_count from product order by sold_count desc, id asc limit 10",
                (rs, rowNum) -> new HotProductDTO(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getLong("sold_count")
                )
        );
    }

    /**
     * 获取产品列表。
     * @param productType 产品类型，可选过滤条件
     * @param name 产品名称，可选过滤条件
     * @param status 产品状态，可选过滤条件
     * @return 分页的产品列表
     */
    public PageResponse<AdminProductDTO> listProducts(String productType, String name, Integer status) {
        StringBuilder baseSql = new StringBuilder(" from product where 1 = 1");
        List<Object> params = new ArrayList<>();
        if (productType != null && !productType.isBlank()) {
            baseSql.append(" and product_type = ?");
            params.add(productType);
        }
        if (name != null && !name.isBlank()) {
            baseSql.append(" and name like ?");
            params.add("%" + name + "%");
        }
        if (status != null) {
            baseSql.append(" and status = ?");
            params.add(status);
        }
        Long total = jdbcTemplate.queryForObject("select count(*)" + baseSql, Long.class, params.toArray());
        List<AdminProductDTO> items = jdbcTemplate.query(
                "select *" + baseSql + " order by id",
                new BeanPropertyRowMapper<>(AdminProductDTO.class),
                params.toArray()
        );
        return new PageResponse<>(total == null ? 0 : total, items);
    }

    /**
     * 更新产品状态。
     */
    public boolean updateProductStatus(String productId, int status, Long adminId) {
        int updated = jdbcTemplate.update(
                "update product set status = ?, update_time = ? where id = ?",
                status,
                LocalDateTime.now(),
                productId
        );
        if (updated > 0) {
            insertAdminLog(adminId, "PRODUCT_STATUS", "productId=" + productId + ",status=" + status);
            return true;
        }
        return false;
    }

    /**
     * 更新产品价格。
     */
    public boolean updateProductPrice(String productId, BigDecimal price, Long adminId) {
        int updated = jdbcTemplate.update(
                "update product set price = ?, update_time = ? where id = ?",
                price,
                LocalDateTime.now(),
                productId
        );
        if (updated > 0) {
            insertAdminLog(adminId, "PRODUCT_PRICE", "productId=" + productId + ",price=" + price);
            return true;
        }
        return false;
    }

    /**
     * 调整库存。
     */
    public boolean updateProductStock(String productId, int stock, Long adminId) {
        int updated = jdbcTemplate.update(
                "update product set stock = ?, update_time = ? where id = ?",
                stock,
                LocalDateTime.now(),
                productId
        );
        if (updated > 0) {
            insertAdminLog(adminId, "PRODUCT_STOCK", "productId=" + productId + ",stock=" + stock);
            return true;
        }
        return false;
    }

    /**
     * 新增产品。
     */
    public ProductDTO createProduct(ProductDTO product, Long adminId) {
        if (product == null) {
            throw new ApiException(400, "product is required");
        }
        jdbcTemplate.update(
                "insert into product (product_type, name, price, stock, sold_count, status, update_time) values (?, ?, ?, ?, ?, ?, ?)",
                product.getProduct_type(),
                product.getName(),
                product.getPrice(),
                product.getStock(),
                product.getSold_count(),
                product.getStatus(),
                LocalDateTime.now()
        );
        insertAdminLog(adminId, "PRODUCT_CREATE", "name=" + product.getName());
        List<ProductDTO> created = jdbcTemplate.query(
                "select * from product where name = ? order by id desc limit 1",
                new BeanPropertyRowMapper<>(ProductDTO.class),
                product.getName()
        );
        return created.isEmpty() ? product : created.get(0);
    }

    /**
     * 逻辑删除产品：设置为下架不可见。
     */
    public boolean deleteProduct(String productId, Long adminId) {
        int updated = jdbcTemplate.update(
                "update product set status = 0, update_time = ? where id = ?",
                LocalDateTime.now(),
                productId
        );
        if (updated > 0) {
            insertAdminLog(adminId, "PRODUCT_DELETE", "productId=" + productId);
            return true;
        }
        return false;
    }

    /**
     * 获取用户列表。
     */
    public PageResponse<UserDTO> listUsers(String keyword, Integer status) {
        StringBuilder baseSql = new StringBuilder(" from `user` where deleted = 0");
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            baseSql.append(" and (nickname like ? or phone like ? or email like ?)");
            String like = "%" + keyword + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (status != null) {
            baseSql.append(" and status = ?");
            params.add(status);
        }
        Long total = jdbcTemplate.queryForObject("select count(*)" + baseSql, Long.class, params.toArray());
        List<UserDTO> items = jdbcTemplate.query(
                "select id, phone, email, nickname, points, level, status, deleted, create_time, update_time" +
                        baseSql + " order by id",
                new BeanPropertyRowMapper<>(UserDTO.class),
                params.toArray()
        );
        return new PageResponse<>(total == null ? 0 : total, items);
    }

    /**
     * 更新用户状态。
     */
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

    /**
     * 调整用户等级。
     */
    public boolean updateUserLevel(String userId, short level, Long adminId) {
        int updated = jdbcTemplate.update(
                "update `user` set level = ?, update_time = ? where id = ?",
                level,
                LocalDateTime.now(),
                userId
        );
        if (updated > 0) {
            insertAdminLog(adminId, "USER_LEVEL", "userId=" + userId + ",level=" + level);
            return true;
        }
        return false;
    }

    /**
     * 获取订单列表。
     */
    public PageResponse<AdminOrderDTO> listOrders(Integer status) {
        StringBuilder baseSql = new StringBuilder(" from `orders`");
        List<Object> params = new ArrayList<>();
        if (status != null) {
            baseSql.append(" where status = ?");
            params.add(status);
        }
        Long total = jdbcTemplate.queryForObject("select count(*)" + baseSql, Long.class, params.toArray());
        List<AdminOrderDTO> items = jdbcTemplate.query(
                "select *" + baseSql + " order by id",
                new BeanPropertyRowMapper<>(AdminOrderDTO.class),
                params.toArray()
        );
        return new PageResponse<>(total == null ? 0 : total, items);
    }

    /**
     * 查询单个订单。
     */
    public AdminOrderDTO findOrderByNo(String orderNo) {
        List<AdminOrderDTO> items = jdbcTemplate.query(
                "select * from `orders` where order_no = ?",
                new BeanPropertyRowMapper<>(AdminOrderDTO.class),
                orderNo
        );
        return items.isEmpty() ? null : items.get(0);
    }

    /**
     * 更新订单状态。
     */
    public boolean updateOrderStatus(String orderNo, int status, String remark, Long adminId) {
        int updated = jdbcTemplate.update(
                "update `orders` set status = ?, cancel_reason = ?, update_time = ? where order_no = ?",
                status,
                remark,
                LocalDateTime.now(),
                orderNo
        );
        if (updated > 0) {
            insertAdminLog(adminId, "ORDER_STATUS", "orderNo=" + orderNo + ",status=" + status);
            return true;
        }
        return false;
    }

    /**
     * 强制退款。
     */
    public boolean refundOrder(String orderNo, String remark, Long adminId) {
        int updated = jdbcTemplate.update(
                "update `orders` set status = ?, update_time = ? where order_no = ?",
                4,
                LocalDateTime.now(),
                orderNo
        );
        if (updated > 0) {
            insertAdminLog(adminId, "ORDER_REFUND", "orderNo=" + orderNo + (remark == null ? "" : ",remark=" + remark));
            return true;
        }
        return false;
    }

    /**
     * 管理员操作日志。
     */
    public PageResponse<AdminLogDTO> listAdminLogs(Map<String, String> params) {
        StringBuilder baseSql = new StringBuilder(" from admin_log where 1 = 1");
        List<Object> values = new ArrayList<>();
        if (params != null) {
            if (params.containsKey("admin_id") && params.get("admin_id") != null && !params.get("admin_id").isBlank()) {
                baseSql.append(" and admin_id = ?");
                values.add(params.get("admin_id"));
            }
            if (params.containsKey("operation") && params.get("operation") != null && !params.get("operation").isBlank()) {
                baseSql.append(" and operation = ?");
                values.add(params.get("operation"));
            }
            if (params.containsKey("result") && params.get("result") != null && !params.get("result").isBlank()) {
                baseSql.append(" and result = ?");
                values.add(params.get("result"));
            }
        }
        Long total = jdbcTemplate.queryForObject("select count(*)" + baseSql, Long.class, values.toArray());
        List<AdminLogDTO> items = jdbcTemplate.query(
                "select *" + baseSql + " order by id desc",
                new BeanPropertyRowMapper<>(AdminLogDTO.class),
                values.toArray()
        );
        return new PageResponse<>(total == null ? 0 : total, items);
    }

    /**
     * 插入管理员操作日志。
     */
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