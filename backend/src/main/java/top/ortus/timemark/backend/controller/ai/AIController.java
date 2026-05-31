package top.ortus.timemark.backend.controller.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.ortus.timemark.backend.JwtTokenService;
import top.ortus.timemark.backend.common.ApiResponse;
import top.ortus.timemark.backend.dto.AIHotelRecommendRequest;
import top.ortus.timemark.backend.dto.AIRecommendResultVO;
import top.ortus.timemark.backend.dto.ReviewSummaryVO;
import top.ortus.timemark.backend.exception.ApiException;
import top.ortus.timemark.backend.service.AIService;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;
    private final JwtTokenService jwtTokenService;

    @PostMapping("/hotel/recommend")
    public ApiResponse<AIRecommendResultVO> recommendHotel(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) AIHotelRecommendRequest request) {
        resolveUserId(authorization);
        String userInput = request == null ? "" : request.getUserInput();
        return ApiResponse.ok(aiService.recommendHotel(userInput));
    }

    @GetMapping("/hotel/review-summary/{hotelId}")
    public ApiResponse<ReviewSummaryVO> reviewSummary(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long hotelId) {
        resolveUserId(authorization);
        return ApiResponse.ok(aiService.generateReviewSummary(hotelId));
    }

    private Long resolveUserId(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            throw new ApiException(401, "login required");
        }
        String token = authorization.startsWith("Bearer ")
                ? authorization.substring("Bearer ".length())
                : authorization;
        Long userId = jwtTokenService.resolveUserId(token);
        if (userId == null) {
            throw new ApiException(401, "login expired");
        }
        return userId;
    }
}
