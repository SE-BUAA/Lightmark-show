package top.ortus.lightmark.backend.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import top.ortus.lightmark.backend.BaseIntegrationTest;
import top.ortus.lightmark.backend.exception.ApiException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class GenericCrudServiceTest extends BaseIntegrationTest {

    @Autowired
    private GenericCrudService genericCrudService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void listShouldFilterByValidColumnsAndIgnoreUnknownFilters() {
        List<Map<String, Object>> all = genericCrudService.list("user", Map.of());
        assertThat(all).hasSize(2);

        List<Map<String, Object>> filtered = genericCrudService.list("user", Map.of("nickname", "普通用户", "unknown", "x"));
        assertThat(filtered).hasSize(1);
        assertThat(filtered.get(0)).containsEntry("nickname", "普通用户");
    }

    @Test
    void createUpdateAndDeleteShouldWork() {
        Map<String, Object> created = genericCrudService.create("product", Map.of(
                "id", 9999,
                "product_type", "HOTEL",
                "name", "通用CRUD测试酒店",
                "price", 199.00,
                "stock", 2,
                "status", 1,
                "unknown", "x"
        ));
        assertThat(created).containsEntry("id", 9999).containsEntry("name", "通用CRUD测试酒店");
        assertThat(created).doesNotContainKey("unknown");

        Integer count = jdbcTemplate.queryForObject("select count(*) from product where id = 9999", Integer.class);
        assertThat(count).isEqualTo(1);

        Map<String, Object> updated = genericCrudService.update("product", Map.of(
                "id", 9999,
                "price", 299.00,
                "stock", 5
        ));
        assertThat(updated).containsEntry("id", 9999).containsEntry("price", 299.00).containsEntry("stock", 5);

        Double price = jdbcTemplate.queryForObject("select price from product where id = 9999", Double.class);
        Integer stock = jdbcTemplate.queryForObject("select stock from product where id = 9999", Integer.class);
        assertThat(price).isEqualTo(299.00);
        assertThat(stock).isEqualTo(5);

        boolean deleted = genericCrudService.delete("product", Map.of("id", 9999));
        assertThat(deleted).isTrue();

        Integer deletedCount = jdbcTemplate.queryForObject("select count(*) from product where id = 9999", Integer.class);
        assertThat(deletedCount).isEqualTo(0);
    }

    @Test
    void deleteShouldSoftDeleteWhenDeletedColumnExists() {
        boolean deleted = genericCrudService.delete("user", Map.of("id", 2));
        assertThat(deleted).isTrue();

        Integer flag = jdbcTemplate.queryForObject("select deleted from `user` where id = 2", Integer.class);
        assertThat(flag).isEqualTo(1);
    }

    @Test
    void unknownTableShouldFail() {
        assertThatThrownBy(() -> genericCrudService.list("unknown_table", Map.of()))
                .isInstanceOf(ApiException.class)
                .hasMessage("unknown table");
    }
}

