package top.ortus.timemark.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomDetailVO {

    private Long roomId;

    private String roomName;

    private String bedType;

    private String area;

    private Integer breakfast;

    private String cancelPolicy;

    private BigDecimal pricePerNight;

    private BigDecimal totalPrice;

    private String checkInDate;

    private String checkOutDate;

    private Long nights;
}
