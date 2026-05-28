package top.ortus.timemark.backend.dto.module;

import lombok.Data;

import java.util.List;

@Data
public class VacationSearchRequest {
    private String destination;
    private String departCity;
    private String date;
    private Integer minDays;
    private Integer maxDays;
    private Double minPrice;
    private Double maxPrice;
    private List<String> tags;
}
