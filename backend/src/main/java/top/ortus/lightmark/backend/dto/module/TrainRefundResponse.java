package top.ortus.lightmark.backend.dto.module;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainRefundResponse {
    private String orderNo;
    private Integer status;
    private BigDecimal paidAmount;
    private BigDecimal refundAmount;
    private String refundRule;
}
