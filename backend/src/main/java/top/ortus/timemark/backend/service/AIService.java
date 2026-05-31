package top.ortus.timemark.backend.service;

import top.ortus.timemark.backend.dto.AIRecommendResultVO;
import top.ortus.timemark.backend.dto.ReviewSummaryVO;

public interface AIService {

    AIRecommendResultVO recommendHotel(String userInput);

    ReviewSummaryVO generateReviewSummary(Long hotelId);
}
