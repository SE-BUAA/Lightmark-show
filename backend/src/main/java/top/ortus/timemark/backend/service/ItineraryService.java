package top.ortus.timemark.backend.service;

import top.ortus.timemark.backend.common.PageResponse;
import top.ortus.timemark.backend.dto.module.TravelPlanDTO;

import java.util.Map;

public interface ItineraryService {

    PageResponse<TravelPlanDTO> listMyPlans(Long userId, Map<String, String> params);

    TravelPlanDTO createPlan(Long userId, TravelPlanDTO payload);

    TravelPlanDTO updatePlan(Long userId, Long id, TravelPlanDTO payload);

    boolean deletePlan(Long userId, Long id);

    TravelPlanDTO generatePlan(Long userId, Map<String, Object> payload);

    Map<String, String> sharePlan(Long userId, Long id);

    Map<String, String> exportPlan(Long userId, Long id);
}
