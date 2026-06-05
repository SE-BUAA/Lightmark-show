package top.ortus.timemark.backend.dto.module;

import lombok.Data;
import java.util.List;

@Data
public class TrainOrderRequest {
    private String productId;
    private String passengerName;
    private String passengerPhone;
    private Integer passengerAge;
    private String seatType;
    private List<String> transferSeatTypes;
    private Boolean isStudent;
}
