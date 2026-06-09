package top.ortus.lightmark.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIHotelSearchIntent {

    private String destination;

    private BigDecimal maxPrice;

    private Integer roomNum;

    private Integer adultNum;

    private List<String> facilities;

    private Integer starLevel;

    private String recommendText;
}
