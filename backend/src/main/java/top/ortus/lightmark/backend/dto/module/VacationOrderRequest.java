package top.ortus.lightmark.backend.dto.module;

import lombok.Data;

@Data
public class VacationOrderRequest {
    private String productId;
    private String travelerName;
    private String travelerPhone;
    private Integer travelerCount;
    private Boolean cancellationInsurance;
}
