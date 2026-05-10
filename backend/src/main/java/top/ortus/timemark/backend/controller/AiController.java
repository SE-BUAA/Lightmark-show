package top.ortus.timemark.backend.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.ortus.timemark.backend.common.ApiResponse;
import top.ortus.timemark.backend.dto.AiDTO;

@RestController
@RequestMapping("/ai")
public class AiController {
    @PostMapping("/post/generate")
    public ApiResponse<AiDTO> generatePost() {
        return ApiResponse.ok(new AiDTO());
    }

    @PostMapping("/review/sentiment")
    public ApiResponse<AiDTO> reviewSentiment() {
        return ApiResponse.ok(new AiDTO());
    }

    @PostMapping("/review/reply")
    public ApiResponse<AiDTO>  reviewReply() {
        return ApiResponse.ok(new AiDTO());
    }

    @PostMapping("/qa/chat")
    public ApiResponse<AiDTO> chatQA() {

        return ApiResponse.ok(new AiDTO());
    }

    @PostMapping("/customer-service/chat")
    public ApiResponse<AiDTO> chatCustomerService() {
        return ApiResponse.ok(new AiDTO());
    }

    @PostMapping("/customer-service/faq")
    public ApiResponse<AiDTO> getCustomerServiceFAQ() {
        return ApiResponse.ok(new AiDTO());
    }

    @PostMapping("/search/flight")
     public ApiResponse<AiDTO> searchFlights() {
        return ApiResponse.ok(new AiDTO());
    }

    @PostMapping("/search/hotel")
    public ApiResponse<AiDTO> searchHotels() {
        return ApiResponse.ok(new AiDTO());
    }

    @PostMapping("/search/travel")
    public ApiResponse<AiDTO> searchTravel() {
        return ApiResponse.ok(new AiDTO());
    }

    @PostMapping("/explain/refund")
    public ApiResponse<AiDTO> explainRefund() {
        return ApiResponse.ok(new AiDTO());
    }

    @PostMapping("/speech2text")
    public ApiResponse<AiDTO> speech2text() {
        return ApiResponse.ok(new AiDTO());
    }
}
