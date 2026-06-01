package top.ortus.timemark.backend.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.ortus.timemark.backend.JwtTokenService;
import top.ortus.timemark.backend.common.ApiResponse;
import top.ortus.timemark.backend.common.PageResponse;
import top.ortus.timemark.backend.dto.module.CommentDTO;
import top.ortus.timemark.backend.dto.module.OrderDTO;
import top.ortus.timemark.backend.dto.module.PostDTO;
import top.ortus.timemark.backend.dto.module.ProductDTO;
import top.ortus.timemark.backend.dto.module.QuestionDTO;
import top.ortus.timemark.backend.dto.module.ReviewDTO;
import top.ortus.timemark.backend.dto.module.TravelPlanDTO;
import top.ortus.timemark.backend.exception.ApiException;
import top.ortus.timemark.backend.security.UserIdentity;
import top.ortus.timemark.backend.service.FlightSearchService;
import top.ortus.timemark.backend.service.CommunityService;
import top.ortus.timemark.backend.service.ItineraryService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PublicApiController {

    private final FlightSearchService flightSearchService;
    private final JwtTokenService jwtTokenService;
    private final ItineraryService itineraryService;
    private final CommunityService communityService;

    public PublicApiController(FlightSearchService flightSearchService,
                               JwtTokenService jwtTokenService,
                               ItineraryService itineraryService,
                               CommunityService communityService) {
        this.flightSearchService = flightSearchService;
        this.jwtTokenService = jwtTokenService;
        this.itineraryService = itineraryService;
        this.communityService = communityService;
    }

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
        return ApiResponse.ok(flightSearchService.search(params));
    }

    @GetMapping("/flights/price-calendar")
    public ApiResponse<Map<String, Object>> flightPriceCalendar(@RequestParam Map<String, String> params) {
        return ApiResponse.ok(flightSearchService.priceCalendar(params));
    }

    @GetMapping("/flights/{productId}")
    public ApiResponse<ProductDTO> flightDetail(@PathVariable String productId) {
        return ApiResponse.ok(flightSearchService.getDetail(productId));
    }

    @PostMapping("/flights/order/preview")
    public ApiResponse<Map<String, Object>> flightOrderPreview(@RequestBody Map<String, Object> payload) {
        return ApiResponse.ok(flightSearchService.previewOrder(payload));
    }

    @PostMapping("/flights/order")
    public ApiResponse<OrderDTO> flightOrder(@RequestHeader(value = "Authorization", required = false) String authorization,
                                             @RequestBody Map<String, Object> payload) {
        return ApiResponse.ok(flightSearchService.createOrder(resolveUserId(authorization), payload));
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

    @GetMapping("/legacy/vacations/search")
    public ApiResponse<PageResponse<ProductDTO>> vacationSearch(@RequestParam Map<String, String> params) {
        return ApiResponse.ok(emptyPage());
    }

    @GetMapping("/legacy/vacations/{productId}")
    public ApiResponse<ProductDTO> vacationDetail(@PathVariable String productId) {
        return ApiResponse.ok(new ProductDTO());
    }

    @PostMapping("/legacy/vacations/order")
    public ApiResponse<OrderDTO> vacationOrder(@RequestBody Map<String, Object> payload) {
        return ApiResponse.ok(new OrderDTO());
    }

    @PostMapping("/orders/{orderNo}/pay")
    public ApiResponse<Map<String, Object>> payOrder(@PathVariable String orderNo, @RequestBody Map<String, Object> payload) {
        return ApiResponse.ok(flightSearchService.payOrder(orderNo, payload));
    }

    @PostMapping("/orders/{orderNo}/cancel")
    public ApiResponse<Boolean> cancelOrder(@PathVariable String orderNo, @RequestBody(required = false) Map<String, Object> payload) {
        String reason = payload == null ? null : String.valueOf(payload.getOrDefault("reason", ""));
        return ApiResponse.ok(flightSearchService.cancelOrder(orderNo, reason));
    }

    @PostMapping("/orders/{orderNo}/refund")
    public ApiResponse<Map<String, Object>> refundOrder(@PathVariable String orderNo) {
        return ApiResponse.ok(flightSearchService.refundOrder(orderNo));
    }

    @GetMapping("/orders/{orderNo}/status")
    public ApiResponse<Map<String, Object>> orderStatus(@PathVariable String orderNo) {
        return ApiResponse.ok(flightSearchService.orderStatus(orderNo));
    }

    @PostMapping("/payment/callback")
    public ApiResponse<Boolean> paymentCallback(@RequestBody Map<String, Object> payload) {
        return ApiResponse.ok(flightSearchService.paymentCallback(payload));
    }

    @GetMapping("/itinerary/my-plans")
    public ApiResponse<PageResponse<TravelPlanDTO>> myPlans(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                            @RequestParam Map<String, String> params) {
        return ApiResponse.ok(itineraryService.listMyPlans(resolveUserId(authorization), params));
    }

    @PostMapping("/itinerary/plans")
    public ApiResponse<TravelPlanDTO> createPlan(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                 @RequestBody TravelPlanDTO payload) {
        return ApiResponse.ok(itineraryService.createPlan(resolveUserId(authorization), payload));
    }

    @PutMapping("/itinerary/plans/{id}")
    public ApiResponse<TravelPlanDTO> updatePlan(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                 @PathVariable Long id,
                                                 @RequestBody TravelPlanDTO payload) {
        return ApiResponse.ok(itineraryService.updatePlan(resolveUserId(authorization), id, payload));
    }

    @DeleteMapping("/itinerary/plans/{id}")
    public ApiResponse<Boolean> deletePlan(@RequestHeader(value = "Authorization", required = false) String authorization,
                                           @PathVariable Long id) {
        return ApiResponse.ok(itineraryService.deletePlan(resolveUserId(authorization), id));
    }

    @PostMapping("/itinerary/ai/generate")
    public ApiResponse<TravelPlanDTO> generatePlan(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                   @RequestBody Map<String, Object> payload) {
        return ApiResponse.ok(itineraryService.generatePlan(resolveUserId(authorization), payload));
    }

    @GetMapping("/itinerary/plans/{id}/share")
    public ApiResponse<Map<String, String>> sharePlan(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                      @PathVariable Long id) {
        return ApiResponse.ok(itineraryService.sharePlan(resolveUserId(authorization), id));
    }

    @GetMapping("/itinerary/plans/{id}/export")
    public ApiResponse<Map<String, String>> exportPlan(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                       @PathVariable Long id) {
        return ApiResponse.ok(itineraryService.exportPlan(resolveUserId(authorization), id));
    }

    @GetMapping("/posts")
    public ApiResponse<PageResponse<PostDTO>> posts(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                    @RequestParam Map<String, String> params) {
        return ApiResponse.ok(communityService.listPosts(resolveUserId(authorization), params));
    }

    @GetMapping("/posts/{id}")
    public ApiResponse<PostDTO> post(@RequestHeader(value = "Authorization", required = false) String authorization,
                                     @PathVariable Long id) {
        return ApiResponse.ok(communityService.getPost(resolveUserId(authorization), id));
    }

    @PostMapping("/posts")
    public ApiResponse<PostDTO> createPost(@RequestHeader(value = "Authorization", required = false) String authorization,
                                           @RequestBody PostDTO payload) {
        return ApiResponse.ok(communityService.createPost(resolveUserId(authorization), payload));
    }

    @PutMapping("/posts/{id}")
    public ApiResponse<PostDTO> updatePost(@RequestHeader(value = "Authorization", required = false) String authorization,
                                           @PathVariable Long id,
                                           @RequestBody PostDTO payload) {
        return ApiResponse.ok(communityService.updatePost(resolveUserId(authorization), id, payload));
    }

    @DeleteMapping("/posts/{id}")
    public ApiResponse<Boolean> deletePost(@RequestHeader(value = "Authorization", required = false) String authorization,
                                           @PathVariable Long id) {
        return ApiResponse.ok(communityService.deletePost(resolveUserId(authorization), resolveAdmin(authorization), id));
    }

    @PostMapping("/posts/{id}/like")
    public ApiResponse<Map<String, Object>> likePost(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                     @PathVariable Long id) {
        return ApiResponse.ok(communityService.togglePostLike(resolveUserId(authorization), id));
    }

    @GetMapping("/posts/{id}/comments")
    public ApiResponse<PageResponse<CommentDTO>> postComments(@PathVariable Long id, @RequestParam Map<String, String> params) {
        return ApiResponse.ok(communityService.listPostComments(id, params));
    }

    @PostMapping("/posts/{id}/comments")
    public ApiResponse<CommentDTO> createComment(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                 @RequestHeader(value = "X-Forwarded-For", required = false) String forwardedFor,
                                                 @PathVariable Long id,
                                                 @RequestBody CommentDTO payload) {
        return ApiResponse.ok(communityService.createPostComment(resolveUserId(authorization), id, payload, forwardedFor));
    }

    @DeleteMapping("/posts/{postId}/comments/{commentId}")
    public ApiResponse<Boolean> deleteComment(@RequestHeader(value = "Authorization", required = false) String authorization,
                                              @PathVariable Long postId,
                                              @PathVariable Long commentId) {
        return ApiResponse.ok(communityService.deletePostComment(resolveUserId(authorization), resolveAdmin(authorization), postId, commentId));
    }

    @GetMapping("/questions")
    public ApiResponse<PageResponse<QuestionDTO>> questions(@RequestParam Map<String, String> params) {
        return ApiResponse.ok(communityService.listQuestions(params));
    }

    @PostMapping("/questions")
    public ApiResponse<QuestionDTO> createQuestion(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                   @RequestBody QuestionDTO payload) {
        return ApiResponse.ok(communityService.createQuestion(resolveUserId(authorization), payload));
    }

    @GetMapping("/questions/{id}")
    public ApiResponse<QuestionDTO> question(@PathVariable Long id) {
        return ApiResponse.ok(communityService.getQuestion(id));
    }

    @PostMapping("/questions/{id}/answer")
    public ApiResponse<QuestionDTO> answerQuestion(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                   @PathVariable Long id,
                                                   @RequestBody Map<String, Object> payload) {
        return ApiResponse.ok(communityService.answerQuestion(resolveUserId(authorization), id, payload));
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

    private Long resolveUserId(String authorization) {
        String token = resolveBearerToken(authorization);
        Long userId = jwtTokenService.resolveUserId(token);
        if (userId == null) {
            throw new ApiException(401, "unauthorized");
        }
        return userId;
    }

    private boolean resolveAdmin(String authorization) {
        String token = resolveBearerToken(authorization);
        return jwtTokenService.resolveIdentity(token) == UserIdentity.ADMIN;
    }

    private String resolveBearerToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new ApiException(401, "unauthorized");
        }
        return authorization.substring("Bearer ".length());
    }
}


