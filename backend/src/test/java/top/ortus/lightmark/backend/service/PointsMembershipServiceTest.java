package top.ortus.lightmark.backend.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import top.ortus.lightmark.backend.BackendApplication;
import top.ortus.lightmark.backend.dao.User;
import top.ortus.lightmark.backend.dao.UserRepositoryImpl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest(
    classes = BackendApplication.class,
    properties = "spring.datasource.url=jdbc:h2:mem:lightmark_points_membership;MODE=MYSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH"
)
@ActiveProfiles("test")
public class PointsMembershipServiceTest {

    @Autowired
    private PointsMembershipService pointsMembershipService;

    @Autowired
    private UserRepositoryImpl userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void awardAndRevokePointsShouldSyncBalanceLevelAndLogs() {
        User user = userRepository.findById("2");
        user.setPoints(490);
        user.setLevel((short) 1);
        user.setUpdate_time(LocalDateTime.now());
        userRepository.update(user);

        pointsMembershipService.awardPoints("2", "9001", "FLIGHT_PAY", new BigDecimal("680.00"));

        User afterAward = userRepository.findById("2");
        Assertions.assertEquals(503, afterAward.getPoints());
        Assertions.assertEquals((short) 2, afterAward.getLevel());

        List<String> awardLogs = jdbcTemplate.query(
                "select source || ':' || amount from points_log where user_id = ? and order_id = ? order by id",
                (rs, rowNum) -> rs.getString(1),
                "2",
                "9001"
        );
        Assertions.assertTrue(awardLogs.contains("FLIGHT_PAY:13"));

        pointsMembershipService.revokePoints("2", "9001", "FLIGHT_REFUND", new BigDecimal("680.00"));

        User afterRefund = userRepository.findById("2");
        Assertions.assertEquals(490, afterRefund.getPoints());
        Assertions.assertEquals((short) 1, afterRefund.getLevel());

        List<String> refundLogs = jdbcTemplate.query(
                "select source || ':' || amount from points_log where user_id = ? and order_id = ? order by id",
                (rs, rowNum) -> rs.getString(1),
                "2",
                "9001"
        );
        Assertions.assertTrue(refundLogs.contains("FLIGHT_REFUND:-13"));
    }
}
