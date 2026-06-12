package top.ortus.lightmark.backend;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminApiIntegrationTests extends BaseIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String adminToken() {
        return bearerToken(1L, "系统管理员", List.of("ADMIN"));
    }

    @Test
    void healthEndpointShouldWork() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("UP"));
    }

    @Test
    void registerAndLoginShouldWork() throws Exception {
        String email = "fresh" + System.currentTimeMillis() + "@qq.com";
        String verificationCode = "123456";

        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update("""
                        insert into auth_verification_code
                        (target, channel, code, expire_time, consumed_time, send_count, create_time, update_time)
                        values (?, 'EMAIL', ?, ?, null, 1, ?, ?)
                        """,
                email,
                verificationCode,
                Timestamp.valueOf(now.plusMinutes(5)),
                Timestamp.valueOf(now),
                Timestamp.valueOf(now)
        );

        MockHttpSession registerSession = new MockHttpSession();
        registerSession.setAttribute("auth:captcha", "ABCD");

        mockMvc.perform(post("/api/auth/register")
                        .session(registerSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"Password!1","nickname":"测试用户","verificationCode":"%s","captchaCode":"ABCD","privacyAccepted":true}
                                """.formatted(email, verificationCode)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.email").value(email));

        MockHttpSession loginSession = new MockHttpSession();
        loginSession.setAttribute("auth:captcha", "QWER");

        String response = mockMvc.perform(post("/api/auth/login")
                        .session(loginSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"account":"%s","password":"Password!1","captchaCode":"QWER","privacyAccepted":true}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode node = objectMapper.readTree(response);
        assertThat(node.path("data").path("token").asText()).isNotBlank();
        assertThat(node.path("data").path("identity").asText()).isEqualTo("USER");

        String token = node.path("data").path("token").asText();
        mockMvc.perform(get("/api/user/current")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.identity").value("USER"))
                .andExpect(jsonPath("$.data.roles[0]").value("USER"));
    }

    @Test
    void dashboardSummaryShouldReturnData() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard/summary")
                        .header("Authorization", adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.totalUsers").value(2))
                .andExpect(jsonPath("$.data.totalOrders").value(3));
    }

    @Test
    void dashboardTrendsAndHotProductsShouldReturnData() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard/trends")
                        .header("Authorization", adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.length()").value(7));

        mockMvc.perform(get("/api/admin/dashboard/hot-products")
                        .header("Authorization", adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].name").value("G1次"));
    }

    @Test
    void productListCanFilterByType() throws Exception {
        mockMvc.perform(get("/api/admin/products")
                        .header("Authorization", adminToken())
                        .param("productType", "HOTEL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].name").value("上海外滩酒店"));
    }

    @Test
    void userListCanFilterByKeyword() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", adminToken())
                        .param("keyword", "普通"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].nickname").value("普通用户"));
    }

    @Test
    void updatingUserStatusShouldWriteAdminLog() throws Exception {
        mockMvc.perform(put("/api/admin/users/2/status")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": 1}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        Integer status = jdbcTemplate.queryForObject("SELECT status FROM `user` WHERE id = 2", Integer.class);
        Integer logCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM admin_log WHERE operation = 'USER_STATUS' AND admin_id = 1",
                Integer.class
        );
        assertThat(status).isEqualTo(1);
        assertThat(logCount).isEqualTo(1);
    }

    @Test
    void updatingUserLevelShouldWriteAdminLog() throws Exception {
        mockMvc.perform(put("/api/admin/users/2/level")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"level": 2}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        Integer level = jdbcTemplate.queryForObject("SELECT level FROM `user` WHERE id = 2", Integer.class);
        Integer logCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM admin_log WHERE operation = 'USER_LEVEL' AND admin_id = 1",
                Integer.class
        );
        assertThat(level).isEqualTo(2);
        assertThat(logCount).isEqualTo(1);
    }

    @Test
    void createUpdateAndDeleteProductShouldWork() throws Exception {
        String response = mockMvc.perform(post("/api/admin/products")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productType":"HOTEL","name":"测试酒店","price":199.00,"stock":11,"soldCount":0,"status":1}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode created = objectMapper.readTree(response).path("data");
        String productId = created.path("id").asText();
        assertThat(productId).isNotBlank();

        mockMvc.perform(put("/api/admin/products/" + productId + "/price")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"price": 299.00}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(put("/api/admin/products/" + productId + "/stock")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"stock": 8}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(put("/api/admin/products/" + productId + "/status")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": 0}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(delete("/api/admin/products/" + productId)
                        .header("Authorization", adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        Integer status = jdbcTemplate.queryForObject("SELECT status FROM product WHERE id = ?", Integer.class, productId);
        assertThat(status).isEqualTo(0);
    }

    @Test
    void orderListCanFilterAndUpdateStatusWithLog() throws Exception {
        mockMvc.perform(get("/api/admin/orders")
                        .header("Authorization", adminToken())
                        .param("status", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1));

        mockMvc.perform(put("/api/admin/orders/ORD202604180003/status")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": 3, "remark": "测试取消"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        Integer status = jdbcTemplate.queryForObject("SELECT status FROM orders WHERE id = 3", Integer.class);
        Integer logCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM admin_log WHERE operation = 'ORDER_STATUS' AND admin_id = 1",
                Integer.class
        );
        assertThat(status).isEqualTo(3);
        assertThat(logCount).isEqualTo(1);
    }

    @Test
    void orderRefundShouldUpdateAndLog() throws Exception {
        mockMvc.perform(post("/api/admin/orders/ORD202604180002/refund")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"remark": "test refund"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        Integer status = jdbcTemplate.queryForObject("SELECT status FROM orders WHERE order_no = ?", Integer.class, "ORD202604180002");
        Integer logCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM admin_log WHERE operation = 'ORDER_REFUND' AND admin_id = 1",
                Integer.class
        );
        assertThat(status).isEqualTo(4);
        assertThat(logCount).isEqualTo(1);
    }
}
