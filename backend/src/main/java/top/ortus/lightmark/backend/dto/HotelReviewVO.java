package top.ortus.lightmark.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class HotelReviewVO {

    private Long id;

    private Long orderId;

    private Long userId;

    private Integer rating;

    private String content;

    private LocalDateTime createTime;
}
