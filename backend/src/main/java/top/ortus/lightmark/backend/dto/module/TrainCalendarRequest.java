package top.ortus.lightmark.backend.dto.module;

import lombok.Data;

import java.util.List;

@Data
public class TrainCalendarRequest {
    private String startStation;
    private String endStation;
    private String month; // yyyy-MM
    private List<String> trainTypes;
    private List<String> seatTypes;
}
