package top.ortus.timemark.backend.controller.placeholder;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.ortus.timemark.backend.common.ApiResponse;
import top.ortus.timemark.backend.dto.placeholder.PlaceholderDTO;

@RestController
@RequestMapping("/api")
public class ModulePlaceholderController {

    @GetMapping("/flights/search")
    public ApiResponse<PlaceholderDTO> flightsSearch() {
        return ApiResponse.ok(new PlaceholderDTO("M3-flight", "placeholder endpoint"));
    }

    @GetMapping("/hotels/search")
    public ApiResponse<PlaceholderDTO> hotelsSearch() {
        return ApiResponse.ok(new PlaceholderDTO("M4-hotel", "placeholder endpoint"));
    }

    @GetMapping("/trains/search")
    public ApiResponse<PlaceholderDTO> trainsSearch() {
        return ApiResponse.ok(new PlaceholderDTO("M5-train", "placeholder endpoint"));
    }

    @GetMapping("/vacations/search")
    public ApiResponse<PlaceholderDTO> vacationsSearch() {
        return ApiResponse.ok(new PlaceholderDTO("M5-vacation", "placeholder endpoint"));
    }
}
