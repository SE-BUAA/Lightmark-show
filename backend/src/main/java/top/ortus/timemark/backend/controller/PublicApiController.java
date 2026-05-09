package top.ortus.timemark.backend.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.ortus.timemark.backend.common.ApiResponse;
import top.ortus.timemark.backend.common.PageResponse;
import top.ortus.timemark.backend.dto.module.CommentDTO;
import top.ortus.timemark.backend.dto.module.OrderDTO;
import top.ortus.timemark.backend.dto.module.PostDTO;
import top.ortus.timemark.backend.dto.module.ProductDTO;
import top.ortus.timemark.backend.dto.module.QuestionDTO;
import top.ortus.timemark.backend.dto.module.ReviewDTO;
import top.ortus.timemark.backend.dto.module.TravelPlanDTO;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PublicApiController {

    @GetMapping("/products")
    public ApiResponse<PageResponse<ProductDTO>> products(@RequestParam Map<String, String> params) {
        return ApiResponse.ok(emptyPage());
    }

    @GetMapping("/products/{id}")
    public ApiResponse<ProductDTO> product(@PathVariable String id) {
        return ApiResponse.ok(new ProductDTO());
    }

    @GetMapping("/flights/search")
    public ApiResponse<PageResponse<ProductDTO>> flightSearch(@RequestParam Map<String, String> params) {
        return ApiResponse.ok(emptyPage());
    }

    @GetMapping("/flights/price-calendar")
    public ApiResponse<Map<String, Object>> flightPriceCalendar(@RequestParam Map<String, String> params) {
        return ApiResponse.ok(Map.of("dates", List.of(), "prices", List.of()));
    }

    @GetMapping("/flights/{productId}")
    public ApiResponse<ProductDTO> flightDetail(@PathVariable String productId) {
        return ApiResponse.ok(new ProductDTO());
    }

    @PostMapping("/flights/order/preview")
    public ApiResponse<Map<String, Object>> flightOrderPreview(@RequestBody Map<String, Object> payload) {
        return ApiResponse.ok(Map.of("preview", true));
    }

    @PostMapping("/flights/order")
    public ApiResponse<OrderDTO> flightOrder(@RequestBody Map<String, Object> payload) {
        return ApiResponse.ok(new OrderDTO());
    }

    @GetMapping("/hotels/search")
    public ApiResponse<PageResponse<ProductDTO>> hotelSearch(@RequestParam Map<String, String> params) {
        return ApiResponse.ok(emptyPage());
    }

    @GetMapping("/hotels/{hotelId}/rooms")
    public ApiResponse<PageResponse<Map<String, Object>>> hotelRooms(@PathVariable String hotelId) {
        return ApiResponse.ok(emptyMapPage());
    }

    @GetMapping("/hotels/{hotelId}/reviews")
    public ApiResponse<PageResponse<ReviewDTO>> hotelReviews(@PathVariable String hotelId) {
        return ApiResponse.ok(emptyPage());
    }

    @PostMapping("/hotels/order")
    public ApiResponse<OrderDTO> hotelOrder(@RequestBody Map<String, Object> payload) {
        return ApiResponse.ok(new OrderDTO());
    }

    @GetMapping("/trains/search")
    public ApiResponse<PageResponse<ProductDTO>> trainSearch(@RequestParam Map<String, String> params) {
        return ApiResponse.ok(emptyPage());
    }

    @GetMapping("/trains/{productId}")
    public ApiResponse<ProductDTO> trainDetail(@PathVariable String productId) {
        return ApiResponse.ok(new ProductDTO());
    }

    @PostMapping("/trains/order")
    public ApiResponse<OrderDTO> trainOrder(@RequestBody Map<String, Object> payload) {
        return ApiResponse.ok(new OrderDTO());
    }

    @GetMapping("/vacations/search")
    public ApiResponse<PageResponse<ProductDTO>> vacationSearch(@RequestParam Map<String, String> params) {
        return ApiResponse.ok(emptyPage());
    }

    @GetMapping("/vacations/{productId}")
    public ApiResponse<ProductDTO> vacationDetail(@PathVariable String productId) {
        return ApiResponse.ok(new ProductDTO());
    }

    @PostMapping("/vacations/order")
    public ApiResponse<OrderDTO> vacationOrder(@RequestBody Map<String, Object> payload) {
        return ApiResponse.ok(new OrderDTO());
    }

    @PostMapping("/orders/{orderNo}/pay")
    public ApiResponse<Map<String, Object>> payOrder(@PathVariable String orderNo, @RequestBody Map<String, Object> payload) {
        return ApiResponse.ok(Map.of("orderNo", orderNo, "paid", true));
    }

    @PostMapping("/orders/{orderNo}/cancel")
    public ApiResponse<Boolean> cancelOrder(@PathVariable String orderNo) {
        return ApiResponse.ok(true);
    }

    @PostMapping("/orders/{orderNo}/refund")
    public ApiResponse<Map<String, Object>> refundOrder(@PathVariable String orderNo) {
        return ApiResponse.ok(Map.of("orderNo", orderNo, "refundAmount", 0));
    }

    @GetMapping("/orders/{orderNo}/status")
    public ApiResponse<Map<String, Object>> orderStatus(@PathVariable String orderNo) {
        return ApiResponse.ok(Map.of("orderNo", orderNo, "status", "PENDING"));
    }

    @PostMapping("/payment/callback")
    public ApiResponse<Boolean> paymentCallback(@RequestBody Map<String, Object> payload) {
        return ApiResponse.ok(true);
    }

    @GetMapping("/itinerary/my-plans")
    public ApiResponse<PageResponse<TravelPlanDTO>> myPlans(@RequestParam Map<String, String> params) {
        return ApiResponse.ok(emptyPage());
    }

    @PostMapping("/itinerary/plans")
    public ApiResponse<TravelPlanDTO> createPlan(@RequestBody TravelPlanDTO payload) {
        return ApiResponse.ok(payload == null ? new TravelPlanDTO() : payload);
    }

    @PutMapping("/itinerary/plans/{id}")
    public ApiResponse<TravelPlanDTO> updatePlan(@PathVariable String id, @RequestBody TravelPlanDTO payload) {
        if (payload == null) {
            payload = new TravelPlanDTO();
        }
        payload.setId(id);
        return ApiResponse.ok(payload);
    }

    @DeleteMapping("/itinerary/plans/{id}")
    public ApiResponse<Boolean> deletePlan(@PathVariable String id) {
        return ApiResponse.ok(true);
    }

    @PostMapping("/itinerary/ai/generate")
    public ApiResponse<TravelPlanDTO> generatePlan(@RequestBody Map<String, Object> payload) {
        return ApiResponse.ok(new TravelPlanDTO());
    }

    @GetMapping("/itinerary/plans/{id}/share")
    public ApiResponse<Map<String, String>> sharePlan(@PathVariable String id) {
        return ApiResponse.ok(Map.of("shortLink", "/share/" + id));
    }

    @GetMapping("/itinerary/plans/{id}/export")
    public ApiResponse<Map<String, String>> exportPlan(@PathVariable String id) {
        return ApiResponse.ok(Map.of("fileUrl", "/exports/" + id + ".pdf"));
    }

    @GetMapping("/posts")
    public ApiResponse<PageResponse<PostDTO>> posts(@RequestParam Map<String, String> params) {
        return ApiResponse.ok(emptyPage());
    }

    @GetMapping("/posts/{id}")
    public ApiResponse<PostDTO> post(@PathVariable String id) {
        return ApiResponse.ok(new PostDTO());
    }

    @PostMapping("/posts")
    public ApiResponse<PostDTO> createPost(@RequestBody PostDTO payload) {
        return ApiResponse.ok(payload == null ? new PostDTO() : payload);
    }

    @PutMapping("/posts/{id}")
    public ApiResponse<PostDTO> updatePost(@PathVariable String id, @RequestBody PostDTO payload) {
        if (payload == null) {
            payload = new PostDTO();
        }
        return ApiResponse.ok(payload);
    }

    @DeleteMapping("/posts/{id}")
    public ApiResponse<Boolean> deletePost(@PathVariable String id) {
        return ApiResponse.ok(true);
    }

    @PostMapping("/posts/{id}/like")
    public ApiResponse<Boolean> likePost(@PathVariable String id) {
        return ApiResponse.ok(true);
    }

    @GetMapping("/posts/{id}/comments")
    public ApiResponse<PageResponse<CommentDTO>> postComments(@PathVariable String id, @RequestParam Map<String, String> params) {
        return ApiResponse.ok(emptyPage());
    }

    @PostMapping("/posts/{id}/comments")
    public ApiResponse<CommentDTO> createComment(@PathVariable String id, @RequestBody CommentDTO payload) {
        return ApiResponse.ok(payload == null ? new CommentDTO() : payload);
    }

    @GetMapping("/questions")
    public ApiResponse<PageResponse<QuestionDTO>> questions(@RequestParam Map<String, String> params) {
        return ApiResponse.ok(emptyPage());
    }

    @PostMapping("/questions")
    public ApiResponse<QuestionDTO> createQuestion(@RequestBody QuestionDTO payload) {
        return ApiResponse.ok(payload == null ? new QuestionDTO() : payload);
    }

    @GetMapping("/questions/{id}")
    public ApiResponse<QuestionDTO> question(@PathVariable String id) {
        return ApiResponse.ok(new QuestionDTO());
    }

    @PostMapping("/questions/{id}/answer")
    public ApiResponse<Boolean> answerQuestion(@PathVariable String id, @RequestBody Map<String, Object> payload) {
        return ApiResponse.ok(true);
    }

    @GetMapping("/reviews/orders/{orderNo}")
    public ApiResponse<Map<String, Object>> reviewStatus(@PathVariable String orderNo) {
        return ApiResponse.ok(Map.of("orderNo", orderNo, "reviewed", false));
    }

    @PostMapping("/reviews/orders/{orderNo}")
    public ApiResponse<ReviewDTO> createReview(@PathVariable String orderNo, @RequestBody ReviewDTO payload) {
        return ApiResponse.ok(payload == null ? new ReviewDTO() : payload);
    }

    @GetMapping("/reviews/product/{productId}")
    public ApiResponse<PageResponse<ReviewDTO>> productReviews(@PathVariable String productId, @RequestParam Map<String, String> params) {
        return ApiResponse.ok(emptyPage());
    }

    @PostMapping("/ai/post/generate")
    public ApiResponse<Map<String, Object>> aiGeneratePost(@RequestBody Map<String, Object> payload) {
        return ApiResponse.ok(Map.of("title", "", "content", "", "images", List.of()));
    }

    @PostMapping("/ai/review/sentiment")
    public ApiResponse<Map<String, Object>> aiReviewSentiment(@RequestBody Map<String, Object> payload) {
        return ApiResponse.ok(Map.of("sentiment", "neutral", "score", 0));
    }

    @PostMapping("/ai/review/reply")
    public ApiResponse<Map<String, Object>> aiReviewReply(@RequestBody Map<String, Object> payload) {
        return ApiResponse.ok(Map.of("reply", ""));
    }

    @PostMapping("/ai/qa/chat")
    public ApiResponse<Map<String, Object>> aiQaChat(@RequestBody Map<String, Object> payload) {
        return ApiResponse.ok(Map.of("answer", ""));
    }

    @PostMapping("/ai/customer-service/chat")
    public ApiResponse<Map<String, Object>> aiCustomerServiceChat(@RequestBody Map<String, Object> payload) {
        return ApiResponse.ok(Map.of("answer", ""));
    }

    @PostMapping("/ai/customer-service/faq")
    public ApiResponse<Map<String, Object>> aiCustomerServiceFaq(@RequestBody Map<String, Object> payload) {
        return ApiResponse.ok(Map.of("answer", ""));
    }

    @PostMapping("/ai/search/flight")
    public ApiResponse<PageResponse<ProductDTO>> aiSearchFlight(@RequestBody Map<String, Object> payload) {
        return ApiResponse.ok(emptyPage());
    }

    @PostMapping("/ai/search/hotel")
    public ApiResponse<PageResponse<ProductDTO>> aiSearchHotel(@RequestBody Map<String, Object> payload) {
        return ApiResponse.ok(emptyPage());
    }

    @PostMapping("/ai/search/travel")
    public ApiResponse<PageResponse<ProductDTO>> aiSearchTravel(@RequestBody Map<String, Object> payload) {
        return ApiResponse.ok(emptyPage());
    }

    @PostMapping("/ai/explain/refund")
    public ApiResponse<Map<String, Object>> aiExplainRefund(@RequestBody Map<String, Object> payload) {
        return ApiResponse.ok(Map.of("explain", ""));
    }

    @PostMapping("/ai/speech-to-text")
    public ApiResponse<Map<String, Object>> speechToText(@RequestBody Map<String, Object> payload) {
        return ApiResponse.ok(Map.of("text", ""));
    }

    @GetMapping("/search/global")
    public ApiResponse<Map<String, Object>> globalSearch(@RequestParam String q) {
        return ApiResponse.ok(Map.of("q", q, "products", List.of(), "posts", List.of(), "destinations", List.of()));
    }

    @GetMapping("/destinations/hot")
    public ApiResponse<List<Map<String, Object>>> hotDestinations() {
        return ApiResponse.ok(List.of());
    }

    @GetMapping("/destinations/{city}/weather")
    public ApiResponse<Map<String, Object>> destinationWeather(@PathVariable String city) {
        return ApiResponse.ok(Map.of("city", city, "weather", "unknown"));
    }

    @PostMapping("/upload/image")
    public ApiResponse<Map<String, Object>> uploadImage(@RequestParam(required = false) String fileName) {
        return ApiResponse.ok(Map.of("url", "/uploads/" + (fileName == null ? "image" : fileName)));
    }

    private <T> PageResponse<T> emptyPage() {
        return new PageResponse<>(0, 1, 0, List.<T>of());
    }

    private PageResponse<Map<String, Object>> emptyMapPage() {
        return new PageResponse<>(0, 1, 0, List.<Map<String, Object>>of());
    }
}


