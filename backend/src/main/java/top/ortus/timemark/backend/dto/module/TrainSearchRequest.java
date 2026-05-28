package top.ortus.timemark.backend.dto.module;

import lombok.Data;
import java.util.List;

@Data
public class TrainSearchRequest {
    private String startStation;
    private String endStation;
    private String date; // yyyy-MM-dd
    private List<String> trainTypes; // 高铁 动车 等
    private List<String> seatTypes; // 商务座 一等座 等
}
