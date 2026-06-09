package top.ortus.lightmark.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotelSearchDTO {

    private String keyword;

    private String checkInDate;

    private String checkOutDate;

    private Integer roomNum;

    private Integer adultNum;

    private String sort;

    private java.math.BigDecimal maxPrice;

    @Builder.Default
    private Integer page = 1;

    @Builder.Default
    private Integer size = 10;

    private Integer starLevel;

    private String brand;

    private String facility;

    private String cancelPolicy;

    private Double lat;

    private Double lng;

    private Double radius;
}
