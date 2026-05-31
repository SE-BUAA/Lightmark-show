package top.ortus.timemark.backend.dto;

import lombok.Data;

@Data
public class HotelReviewRequest {

    private Integer rating;

    private String content;
}
