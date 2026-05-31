package top.ortus.timemark.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotelOrderDetailVO {

    private Long orderId;

    private String orderNo;

    private String hotelName;

    private Long roomId;

    private String roomName;

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    private Integer roomNum;

    private List<CreateHotelOrderRequest.GuestInfo> guestList;

    private BigDecimal pricePerNight;

    private BigDecimal totalAmount;

    private Integer pointsDeducted;

    private BigDecimal payAmount;

    private Integer status;

    private String paymentMethod;

    private String cancelPolicy;

    private LocalDateTime payDeadline;

    private LocalDateTime createTime;
}
