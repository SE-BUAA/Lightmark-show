package top.ortus.timemark.backend.dto.module;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainChangeResponse {
    private String oldOrderNo;
    private String newOrderNo;
    private String pickupCode;
    private BigDecimal oldPayAmount;
    private BigDecimal newPayAmount;
    private BigDecimal differenceAmount;
    private String differenceType;
    private String message;
}
