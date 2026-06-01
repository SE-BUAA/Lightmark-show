package top.ortus.timemark.backend.service;

import top.ortus.timemark.backend.dto.AIRecommendResultVO;
import top.ortus.timemark.backend.dto.ReviewSummaryVO;
import top.ortus.timemark.backend.dto.module.TravelPlanDTO;

import java.util.Map;

public interface AIService {

    AIRecommendResultVO recommendHotel(String userInput);

    ReviewSummaryVO generateReviewSummary(Long hotelId);

    TravelPlanDTO generateTravelPlan(Map<String, Object> payload);
}
