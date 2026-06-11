package top.ortus.lightmark.backend.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import top.ortus.lightmark.backend.BackendApplication;
import top.ortus.lightmark.backend.dao.UserRepositoryImpl;
import top.ortus.lightmark.backend.dto.module.TrainOrderRequest;
import top.ortus.lightmark.backend.dto.module.TrainOrderResponse;
import top.ortus.lightmark.backend.dto.module.VacationOrderRequest;

import java.util.List;

@SpringBootTest(
    classes = BackendApplication.class,
    properties = "spring.datasource.url=jdbc:h2:mem:lightmark_train_vacation_points;MODE=MYSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH"
)
@ActiveProfiles("test")
public class TrainVacationPointsIntegrationTest {

    @Autowired
    private OrderServiceImpl orderService;

    @Autowired
    private UserRepositoryImpl userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void trainPayAndRefundShouldSyncPointsBalanceAndLogs() {
        int initialPoints = userRepository.findById("2").getPoints();

        jdbcTemplate.update(
                "update orders set extra_info = ? where order_no = ?",
                "{\"productId\":\"3\",\"date\":\"2026-12-31\",\"departTime\":\"08:00\",\"trainName\":\"G1次\",\"startStation\":\"北京南\",\"endStation\":\"上海虹桥\"}",
                "ORD202604180003"
        );

        orderService.payOrder("ORD202604180003");
        Assertions.assertEquals(initialPoints + 11, userRepository.findById("2").getPoints());

        List<String> payLogs = jdbcTemplate.query(
                "select source || ':' || amount from points_log where user_id = ? and order_id = (select id from orders where order_no = ?) order by id",
                (rs, rowNum) -> rs.getString(1),
                "2",
                "ORD202604180003"
        );
        Assertions.assertTrue(payLogs.contains("TRAIN_PAY:11"));

        orderService.refundTrainOrder("ORD202604180003");
        Assertions.assertEquals(initialPoints, userRepository.findById("2").getPoints());

        List<String> refundLogs = jdbcTemplate.query(
                "select source || ':' || amount from points_log where user_id = ? and order_id = (select id from orders where order_no = ?) order by id",
                (rs, rowNum) -> rs.getString(1),
                "2",
                "ORD202604180003"
        );
        Assertions.assertTrue(refundLogs.contains("TRAIN_REFUND:-11"));
    }

    @Test
    void vacationPayAndRefundShouldSyncPointsBalanceAndLogs() {
        int initialPoints = userRepository.findById("2").getPoints();

        VacationOrderRequest request = new VacationOrderRequest();
        request.setProductId("201");
        request.setTravelerName("李四");
        request.setTravelerPhone("13900000000");
        request.setTravelerCount(1);
        request.setCancellationInsurance(false);

        TrainOrderResponse order = orderService.createVacationOrder(2L, request);

        orderService.payOrder(order.getOrderNo());
        Assertions.assertEquals(initialPoints + 59, userRepository.findById("2").getPoints());

        List<String> payLogs = jdbcTemplate.query(
                "select source || ':' || amount from points_log where user_id = ? and order_id = (select id from orders where order_no = ?) order by id",
                (rs, rowNum) -> rs.getString(1),
                "2",
                order.getOrderNo()
        );
        Assertions.assertTrue(payLogs.contains("VACATION_PAY:59"));

        orderService.refundVacationOrder(order.getOrderNo());
        Assertions.assertEquals(initialPoints, userRepository.findById("2").getPoints());

        List<String> refundLogs = jdbcTemplate.query(
                "select source || ':' || amount from points_log where user_id = ? and order_id = (select id from orders where order_no = ?) order by id",
                (rs, rowNum) -> rs.getString(1),
                "2",
                order.getOrderNo()
        );
        Assertions.assertTrue(refundLogs.contains("VACATION_REFUND:-59"));
    }
}
