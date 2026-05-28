package top.ortus.timemark.backend.dto.module;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.ortus.timemark.backend.dao.Product;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainChangePreviewResponse {
    private String orderNo;
    private String trainName;
    private String startStation;
    private String endStation;
    private String seatType;
    private List<Product> candidates;
}
