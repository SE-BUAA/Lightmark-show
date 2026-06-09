package top.ortus.lightmark.backend.service;

import top.ortus.lightmark.backend.dto.AIRecommendResultVO;
import top.ortus.lightmark.backend.dto.ReviewSummaryVO;
import top.ortus.lightmark.backend.dto.module.TravelPlanDTO;

import java.util.Map;

public interface AIService {

    AIRecommendResultVO recommendHotel(String userInput);

    ReviewSummaryVO generateReviewSummary(Long hotelId);

    TravelPlanDTO generateTravelPlan(Map<String, Object> payload);
}
