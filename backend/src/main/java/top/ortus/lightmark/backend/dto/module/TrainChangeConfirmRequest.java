package top.ortus.lightmark.backend.dto.module;

import lombok.Data;

@Data
public class TrainChangeConfirmRequest {
    private String pickupCode;
    private String targetProductId;
}
