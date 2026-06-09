package top.ortus.lightmark.backend.dto;

import lombok.Data;

@Data
public class HotelReviewRequest {

    private Integer rating;

    private String content;
}
