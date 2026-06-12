package top.ortus.lightmark.backend.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import top.ortus.lightmark.backend.BackendApplication;
import top.ortus.lightmark.backend.dao.UserRepositoryImpl;
import top.ortus.lightmark.backend.dto.module.OrderDTO;

import java.util.List;
import java.util.Map;

@SpringBootTest(
    classes = BackendApplication.class,
    properties = "spring.datasource.url=jdbc:h2:mem:lightmark_flight_points;MODE=MYSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH"
)
@ActiveProfiles("test")
public class FlightPointsIntegrationTest {

    @Autowired
    private FlightSearchService flightSearchService;

    @Autowired
    private UserRepositoryImpl userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void flightPayAndRefundShouldSyncPointsBalanceAndLogs() {
        int initialPoints = userRepository.findById("2").getPoints();

        OrderDTO order = flightSearchService.createOrder(2L, Map.of(
                "productId", "1",
                "adultCount", 1,
                "cabin", "ECONOMY"
        ));

        flightSearchService.payOrder(order.getOrder_no(), Map.of("paymentMethod", "ALIPAY"));
        Assertions.assertEquals(initialPoints + 16, userRepository.findById("2").getPoints());

        List<String> payLogs = jdbcTemplate.query(
                "select source || ':' || amount from points_log where user_id = ? and order_id = ? order by id",
                (rs, rowNum) -> rs.getString(1),
                "2",
                order.getId()
        );
        Assertions.assertTrue(payLogs.contains("FLIGHT_PAY:16"));

        flightSearchService.refundOrder(order.getOrder_no());
        Assertions.assertEquals(initialPoints, userRepository.findById("2").getPoints());

        List<String> refundLogs = jdbcTemplate.query(
                "select source || ':' || amount from points_log where user_id = ? and order_id = ? order by id",
                (rs, rowNum) -> rs.getString(1),
                "2",
                order.getId()
        );
        Assertions.assertTrue(refundLogs.contains("FLIGHT_REFUND:-16"));
    }
}
