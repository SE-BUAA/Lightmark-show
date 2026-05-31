package top.ortus.timemark.backend.vo;

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
public class HotelVO {

    private String id;

    private String name;

    private String address;

    private Integer starLevel;

    private Double rating;

    private BigDecimal priceMin;

    private Double distance;

    private String coverImage;

    private List<String> facilities;

    private String cancelPolicy;
}
