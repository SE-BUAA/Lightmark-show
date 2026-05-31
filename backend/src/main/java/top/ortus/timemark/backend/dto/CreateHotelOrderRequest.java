package top.ortus.timemark.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateHotelOrderRequest {

    private Long roomId;

    private String checkInDate;

    private String checkOutDate;

    private Integer roomNum;

    private List<GuestInfo> guestList;

    private Integer pointsDeduced;

    private String paymentMethod;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GuestInfo {

        private String name;

        private String idCard;

        private String phone;
    }
}
