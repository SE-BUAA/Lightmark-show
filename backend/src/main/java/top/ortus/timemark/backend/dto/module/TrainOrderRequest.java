package top.ortus.timemark.backend.dto.module;

import lombok.Data;

@Data
public class TrainOrderRequest {
    private String productId;
    private String passengerName;
    private String passengerPhone;
    private Integer passengerAge;
    private String seatType;
    private Boolean isStudent;
}
