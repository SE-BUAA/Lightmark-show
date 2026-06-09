package top.ortus.lightmark.backend.controller.placeholder;

import top.ortus.lightmark.backend.common.ApiResponse;
import top.ortus.lightmark.backend.dto.placeholder.PlaceholderDTO;

public class ModulePlaceholderController {

    public ApiResponse<PlaceholderDTO> flightsSearch() {
        return ApiResponse.ok(new PlaceholderDTO("M3-flight", "placeholder endpoint"));
    }

    public ApiResponse<PlaceholderDTO> hotelsSearch() {
        return ApiResponse.ok(new PlaceholderDTO("M4-hotel", "placeholder endpoint"));
    }

    public ApiResponse<PlaceholderDTO> trainsSearch() {
        return ApiResponse.ok(new PlaceholderDTO("M5-train", "placeholder endpoint"));
    }

    public ApiResponse<PlaceholderDTO> vacationsSearch() {
        return ApiResponse.ok(new PlaceholderDTO("M5-vacation", "placeholder endpoint"));
    }
}