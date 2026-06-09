package top.ortus.lightmark.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.ortus.lightmark.backend.vo.HotelVO;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIRecommendResultVO {

    private String recommendText;

    private List<HotelVO> hotels;
}
