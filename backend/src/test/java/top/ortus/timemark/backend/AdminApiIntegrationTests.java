package top.ortus.timemark.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import top.ortus.timemark.backend.security.JwtTokenService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminApiIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String adminToken() {
        return jwtTokenService.createToken(1L, "系统管理员", List.of("ADMIN"));
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
        String account = "139" + System.currentTimeMillis();
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"account":"%s","password":"123456","nickname":"测试用户"}
                                """.formatted(account)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"account":"%s","password":"123456"}
                                """.formatted(account)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode node = objectMapper.readTree(response);
        assertThat(node.path("data").path("token").asText()).isNotBlank();
    }

    @Test
    void dashboardSummaryShouldReturnData() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard/summary")
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.totalUsers").value(2))
                .andExpect(jsonPath("$.data.totalOrders").value(3));
    }

    @Test
    void productListCanFilterByType() throws Exception {
        mockMvc.perform(get("/api/admin/products")
                        .header("Authorization", "Bearer " + adminToken())
                        .param("productType", "HOTEL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].name").value("上海外滩酒店"));
    }

    @Test
    void userListCanFilterByKeyword() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken())
                        .param("keyword", "普通"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].nickname").value("普通用户"));
    }

    @Test
    void updatingUserStatusShouldWriteAdminLog() throws Exception {
        mockMvc.perform(patch("/api/admin/users/2/status")
                        .header("Authorization", "Bearer " + adminToken())
                        .param("status", "1"))
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
    void orderListCanFilterAndUpdateStatusWithLog() throws Exception {
        mockMvc.perform(get("/api/admin/orders")
                        .header("Authorization", "Bearer " + adminToken())
                        .param("status", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1));

        mockMvc.perform(patch("/api/admin/orders/3/status")
                        .header("Authorization", "Bearer " + adminToken())
                        .param("status", "3")
                        .param("cancelReason", "测试取消"))
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
}



