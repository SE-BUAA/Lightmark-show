package top.ortus.timemark.backend.dto.module;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainTicketDTO {
    private String id;
    private String name;
    private Double price;
    private Integer stock;
    private Integer soldCount;
    private List<String> categoryTags;
    private Map<String, Object> extra;
    private Map<String, Integer> seats;
    private Map<String, Double> prices;
}
