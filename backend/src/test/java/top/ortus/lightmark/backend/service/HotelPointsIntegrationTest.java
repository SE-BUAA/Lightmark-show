package top.ortus.lightmark.backend.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import top.ortus.lightmark.backend.BackendApplication;
import top.ortus.lightmark.backend.dao.UserRepositoryImpl;
import top.ortus.lightmark.backend.dto.CreateHotelOrderRequest;
import top.ortus.lightmark.backend.dto.OrderResultVO;
import top.ortus.lightmark.backend.service.impl.HotelServiceImpl;

import java.util.List;

@SpringBootTest(
    classes = BackendApplication.class,
    properties = "spring.datasource.url=jdbc:h2:mem:lightmark_hotel_points;MODE=MYSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH"
)
@ActiveProfiles("test")
public class HotelPointsIntegrationTest {

    @Autowired
    private HotelServiceImpl hotelService;

    @Autowired
    private UserRepositoryImpl userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void hotelPayAndCancelShouldSyncPointsBalanceAndLogs() {
        int initialPoints = userRepository.findById("2").getPoints();

        CreateHotelOrderRequest request = CreateHotelOrderRequest.builder()
                .roomId(-21L)
                .checkInDate("2026-12-20")
                .checkOutDate("2026-12-22")
                .roomNum(1)
                .pointsDeduced(0)
                .paymentMethod("ALIPAY")
                .guestList(List.of(CreateHotelOrderRequest.GuestInfo.builder()
                        .name("张三")
                        .idCard("110101199001011234")
                        .phone("13900000000")
                        .build()))
                .build();

        OrderResultVO order = hotelService.createOrder(2L, request);
        hotelService.payHotelOrder(2L, order.getOrderId(), "ALIPAY");
        Assertions.assertEquals(initialPoints + 34, userRepository.findById("2").getPoints());

        List<String> payLogs = jdbcTemplate.query(
                "select source || ':' || amount from points_log where user_id = ? and order_id = ? order by id",
                (rs, rowNum) -> rs.getString(1),
                "2",
                order.getOrderId()
        );
        Assertions.assertTrue(payLogs.contains("HOTEL_PAY:34"));

        hotelService.cancelHotelOrder(2L, order.getOrderId());
        Assertions.assertEquals(initialPoints, userRepository.findById("2").getPoints());

        List<String> refundLogs = jdbcTemplate.query(
                "select source || ':' || amount from points_log where user_id = ? and order_id = ? order by id",
                (rs, rowNum) -> rs.getString(1),
                "2",
                order.getOrderId()
        );
        Assertions.assertTrue(refundLogs.contains("HOTEL_REFUND:-34"));
    }
}
