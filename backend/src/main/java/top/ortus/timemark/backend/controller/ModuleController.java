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
import top.ortus.timemark.backend.dto.module.AdminLogDTO;
import top.ortus.timemark.backend.dto.module.CommentDTO;
import top.ortus.timemark.backend.dto.module.FlightOrderDetailDTO;
import top.ortus.timemark.backend.dto.module.OrderDTO;
import top.ortus.timemark.backend.dto.module.PaymentRecordDTO;
import top.ortus.timemark.backend.dto.module.PointsLogDTO;
import top.ortus.timemark.backend.dto.module.PostDTO;
import top.ortus.timemark.backend.dto.module.PostLikeDTO;
import top.ortus.timemark.backend.dto.module.ProductDTO;
import top.ortus.timemark.backend.dto.module.ProductViewLogDTO;
import top.ortus.timemark.backend.dto.module.QuestionDTO;
import top.ortus.timemark.backend.dto.module.ReviewDTO;
import top.ortus.timemark.backend.dto.module.RoleDTO;
import top.ortus.timemark.backend.dto.module.RoomTypeDTO;
import top.ortus.timemark.backend.dto.module.TravelPlanDTO;
import top.ortus.timemark.backend.dto.module.TravelerDTO;
import top.ortus.timemark.backend.dto.module.UserLoginLogDTO;
import top.ortus.timemark.backend.dto.module.UserRoleDTO;
import top.ortus.timemark.backend.service.GenericCrudService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModuleController {

    private final GenericCrudService genericCrudService;

    public ModuleController(GenericCrudService genericCrudService) {
        this.genericCrudService = genericCrudService;
    }

    @GetMapping("/flights")
    public ApiResponse<PageResponse<ProductDTO>> listFlights() {
        return listByProductType("FLIGHT");
    }

    @GetMapping("/hotels")
    public ApiResponse<PageResponse<ProductDTO>> listHotels() {
        return listByProductType("HOTEL");
    }

    @GetMapping("/trains")
    public ApiResponse<PageResponse<ProductDTO>> listTrains() {
        return listByProductType("TRAIN");
    }

    @GetMapping("/vacations")
    public ApiResponse<PageResponse<ProductDTO>> listVacations() {
        return listByProductType("VACATION");
    }

    @GetMapping("/travelers")
    public ApiResponse<PageResponse<TravelerDTO>> listTravelers(@RequestParam Map<String, String> params) {
        return list("traveler", params, TravelerDTO.class);
    }

    @PostMapping("/travelers")
    public ApiResponse<TravelerDTO> createTraveler(@RequestBody TravelerDTO payload) {
        return ApiResponse.ok(genericCrudService.createTyped("traveler", payload, TravelerDTO.class));
    }

    @PutMapping("/travelers")
    public ApiResponse<TravelerDTO> updateTraveler(@RequestBody TravelerDTO payload) {
        return ApiResponse.ok(genericCrudService.updateTyped("traveler", payload, TravelerDTO.class));
    }

    @DeleteMapping("/travelers")
    public ApiResponse<Boolean> deleteTraveler(@RequestBody TravelerDTO payload) {
        return ApiResponse.ok(genericCrudService.deleteTyped("traveler", payload));
    }

    @GetMapping("/points-logs")
    public ApiResponse<PageResponse<PointsLogDTO>> listPointsLogs(@RequestParam Map<String, String> params) {
        return list("points_log", params, PointsLogDTO.class);
    }

    @PostMapping("/points-logs")
    public ApiResponse<PointsLogDTO> createPointsLog(@RequestBody PointsLogDTO payload) {
        return ApiResponse.ok(genericCrudService.createTyped("points_log", payload, PointsLogDTO.class));
    }

    @PutMapping("/points-logs")
    public ApiResponse<PointsLogDTO> updatePointsLog(@RequestBody PointsLogDTO payload) {
        return ApiResponse.ok(genericCrudService.updateTyped("points_log", payload, PointsLogDTO.class));
    }

    @DeleteMapping("/points-logs")
    public ApiResponse<Boolean> deletePointsLog(@RequestBody PointsLogDTO payload) {
        return ApiResponse.ok(genericCrudService.deleteTyped("points_log", payload));
    }

    @GetMapping("/user-login-logs")
    public ApiResponse<PageResponse<UserLoginLogDTO>> listLoginLogs(@RequestParam Map<String, String> params) {
        return list("user_login_log", params, UserLoginLogDTO.class);
    }

    @PostMapping("/user-login-logs")
    public ApiResponse<UserLoginLogDTO> createLoginLog(@RequestBody UserLoginLogDTO payload) {
        return ApiResponse.ok(genericCrudService.createTyped("user_login_log", payload, UserLoginLogDTO.class));
    }

    @PutMapping("/user-login-logs")
    public ApiResponse<UserLoginLogDTO> updateLoginLog(@RequestBody UserLoginLogDTO payload) {
        return ApiResponse.ok(genericCrudService.updateTyped("user_login_log", payload, UserLoginLogDTO.class));
    }

    @DeleteMapping("/user-login-logs")
    public ApiResponse<Boolean> deleteLoginLog(@RequestBody UserLoginLogDTO payload) {
        return ApiResponse.ok(genericCrudService.deleteTyped("user_login_log", payload));
    }

    @GetMapping("/room-types")
    public ApiResponse<PageResponse<RoomTypeDTO>> listRoomTypes(@RequestParam Map<String, String> params) {
        return list("room_type", params, RoomTypeDTO.class);
    }

    @PostMapping("/room-types")
    public ApiResponse<RoomTypeDTO> createRoomType(@RequestBody RoomTypeDTO payload) {
        return ApiResponse.ok(genericCrudService.createTyped("room_type", payload, RoomTypeDTO.class));
    }

    @PutMapping("/room-types")
    public ApiResponse<RoomTypeDTO> updateRoomType(@RequestBody RoomTypeDTO payload) {
        return ApiResponse.ok(genericCrudService.updateTyped("room_type", payload, RoomTypeDTO.class));
    }

    @DeleteMapping("/room-types")
    public ApiResponse<Boolean> deleteRoomType(@RequestBody RoomTypeDTO payload) {
        return ApiResponse.ok(genericCrudService.deleteTyped("room_type", payload));
    }

    @GetMapping("/product-view-logs")
    public ApiResponse<PageResponse<ProductViewLogDTO>> listProductViews(@RequestParam Map<String, String> params) {
        return list("product_view_log", params, ProductViewLogDTO.class);
    }

    @PostMapping("/product-view-logs")
    public ApiResponse<ProductViewLogDTO> createProductView(@RequestBody ProductViewLogDTO payload) {
        return ApiResponse.ok(genericCrudService.createTyped("product_view_log", payload, ProductViewLogDTO.class));
    }

    @PutMapping("/product-view-logs")
    public ApiResponse<ProductViewLogDTO> updateProductView(@RequestBody ProductViewLogDTO payload) {
        return ApiResponse.ok(genericCrudService.updateTyped("product_view_log", payload, ProductViewLogDTO.class));
    }

    @DeleteMapping("/product-view-logs")
    public ApiResponse<Boolean> deleteProductView(@RequestBody ProductViewLogDTO payload) {
        return ApiResponse.ok(genericCrudService.deleteTyped("product_view_log", payload));
    }

    @GetMapping("/orders")
    public ApiResponse<PageResponse<OrderDTO>> listOrders(@RequestParam Map<String, String> params) {
        return list("orders", params, OrderDTO.class);
    }

    @PostMapping("/orders")
    public ApiResponse<OrderDTO> createOrder(@RequestBody OrderDTO payload) {
        return ApiResponse.ok(genericCrudService.createTyped("orders", payload, OrderDTO.class));
    }

    @PutMapping("/orders")
    public ApiResponse<OrderDTO> updateOrder(@RequestBody OrderDTO payload) {
        return ApiResponse.ok(genericCrudService.updateTyped("orders", payload, OrderDTO.class));
    }

    @DeleteMapping("/orders")
    public ApiResponse<Boolean> deleteOrder(@RequestBody OrderDTO payload) {
        return ApiResponse.ok(genericCrudService.deleteTyped("orders", payload));
    }

    @GetMapping("/payment-records")
    public ApiResponse<PageResponse<PaymentRecordDTO>> listPaymentRecords(@RequestParam Map<String, String> params) {
        return list("payment_record", params, PaymentRecordDTO.class);
    }

    @PostMapping("/payment-records")
    public ApiResponse<PaymentRecordDTO> createPaymentRecord(@RequestBody PaymentRecordDTO payload) {
        return ApiResponse.ok(genericCrudService.createTyped("payment_record", payload, PaymentRecordDTO.class));
    }

    @PutMapping("/payment-records")
    public ApiResponse<PaymentRecordDTO> updatePaymentRecord(@RequestBody PaymentRecordDTO payload) {
        return ApiResponse.ok(genericCrudService.updateTyped("payment_record", payload, PaymentRecordDTO.class));
    }

    @DeleteMapping("/payment-records")
    public ApiResponse<Boolean> deletePaymentRecord(@RequestBody PaymentRecordDTO payload) {
        return ApiResponse.ok(genericCrudService.deleteTyped("payment_record", payload));
    }

    @GetMapping("/flight-order-details")
    public ApiResponse<PageResponse<FlightOrderDetailDTO>> listFlightOrderDetails(@RequestParam Map<String, String> params) {
        return list("flight_order_detail", params, FlightOrderDetailDTO.class);
    }

    @PostMapping("/flight-order-details")
    public ApiResponse<FlightOrderDetailDTO> createFlightOrderDetail(@RequestBody FlightOrderDetailDTO payload) {
        return ApiResponse.ok(genericCrudService.createTyped("flight_order_detail", payload, FlightOrderDetailDTO.class));
    }

    @PutMapping("/flight-order-details")
    public ApiResponse<FlightOrderDetailDTO> updateFlightOrderDetail(@RequestBody FlightOrderDetailDTO payload) {
        return ApiResponse.ok(genericCrudService.updateTyped("flight_order_detail", payload, FlightOrderDetailDTO.class));
    }

    @DeleteMapping("/flight-order-details")
    public ApiResponse<Boolean> deleteFlightOrderDetail(@RequestBody FlightOrderDetailDTO payload) {
        return ApiResponse.ok(genericCrudService.deleteTyped("flight_order_detail", payload));
    }

    @GetMapping("/travel-plans")
    public ApiResponse<PageResponse<TravelPlanDTO>> listTravelPlans(@RequestParam Map<String, String> params) {
        return list("travel_plan", params, TravelPlanDTO.class);
    }

    @PostMapping("/travel-plans")
    public ApiResponse<TravelPlanDTO> createTravelPlan(@RequestBody TravelPlanDTO payload) {
        return ApiResponse.ok(genericCrudService.createTyped("travel_plan", payload, TravelPlanDTO.class));
    }

    @PutMapping("/travel-plans")
    public ApiResponse<TravelPlanDTO> updateTravelPlan(@RequestBody TravelPlanDTO payload) {
        return ApiResponse.ok(genericCrudService.updateTyped("travel_plan", payload, TravelPlanDTO.class));
    }

    @DeleteMapping("/travel-plans")
    public ApiResponse<Boolean> deleteTravelPlan(@RequestBody TravelPlanDTO payload) {
        return ApiResponse.ok(genericCrudService.deleteTyped("travel_plan", payload));
    }

    @GetMapping("/posts")
    public ApiResponse<PageResponse<PostDTO>> listPosts(@RequestParam Map<String, String> params) {
        return list("post", params, PostDTO.class);
    }

    @PostMapping("/posts")
    public ApiResponse<PostDTO> createPost(@RequestBody PostDTO payload) {
        return ApiResponse.ok(genericCrudService.createTyped("post", payload, PostDTO.class));
    }

    @PutMapping("/posts")
    public ApiResponse<PostDTO> updatePost(@RequestBody PostDTO payload) {
        return ApiResponse.ok(genericCrudService.updateTyped("post", payload, PostDTO.class));
    }

    @DeleteMapping("/posts")
    public ApiResponse<Boolean> deletePost(@RequestBody PostDTO payload) {
        return ApiResponse.ok(genericCrudService.deleteTyped("post", payload));
    }

    @GetMapping("/post-likes")
    public ApiResponse<PageResponse<PostLikeDTO>> listPostLikes(@RequestParam Map<String, String> params) {
        return list("post_like", params, PostLikeDTO.class);
    }

    @PostMapping("/post-likes")
    public ApiResponse<PostLikeDTO> createPostLike(@RequestBody PostLikeDTO payload) {
        return ApiResponse.ok(genericCrudService.createTyped("post_like", payload, PostLikeDTO.class));
    }

    @PutMapping("/post-likes")
    public ApiResponse<PostLikeDTO> updatePostLike(@RequestBody PostLikeDTO payload) {
        return ApiResponse.ok(genericCrudService.updateTyped("post_like", payload, PostLikeDTO.class));
    }

    @DeleteMapping("/post-likes")
    public ApiResponse<Boolean> deletePostLike(@RequestBody PostLikeDTO payload) {
        return ApiResponse.ok(genericCrudService.deleteTyped("post_like", payload));
    }

    @GetMapping("/comments")
    public ApiResponse<PageResponse<CommentDTO>> listComments(@RequestParam Map<String, String> params) {
        return list("comment", params, CommentDTO.class);
    }

    @PostMapping("/comments")
    public ApiResponse<CommentDTO> createComment(@RequestBody CommentDTO payload) {
        return ApiResponse.ok(genericCrudService.createTyped("comment", payload, CommentDTO.class));
    }

    @PutMapping("/comments")
    public ApiResponse<CommentDTO> updateComment(@RequestBody CommentDTO payload) {
        return ApiResponse.ok(genericCrudService.updateTyped("comment", payload, CommentDTO.class));
    }

    @DeleteMapping("/comments")
    public ApiResponse<Boolean> deleteComment(@RequestBody CommentDTO payload) {
        return ApiResponse.ok(genericCrudService.deleteTyped("comment", payload));
    }

    @GetMapping("/reviews")
    public ApiResponse<PageResponse<ReviewDTO>> listReviews(@RequestParam Map<String, String> params) {
        return list("review", params, ReviewDTO.class);
    }

    @PostMapping("/reviews")
    public ApiResponse<ReviewDTO> createReview(@RequestBody ReviewDTO payload) {
        return ApiResponse.ok(genericCrudService.createTyped("review", payload, ReviewDTO.class));
    }

    @PutMapping("/reviews")
    public ApiResponse<ReviewDTO> updateReview(@RequestBody ReviewDTO payload) {
        return ApiResponse.ok(genericCrudService.updateTyped("review", payload, ReviewDTO.class));
    }

    @DeleteMapping("/reviews")
    public ApiResponse<Boolean> deleteReview(@RequestBody ReviewDTO payload) {
        return ApiResponse.ok(genericCrudService.deleteTyped("review", payload));
    }

    @GetMapping("/questions")
    public ApiResponse<PageResponse<QuestionDTO>> listQuestions(@RequestParam Map<String, String> params) {
        return list("question", params, QuestionDTO.class);
    }

    @PostMapping("/questions")
    public ApiResponse<QuestionDTO> createQuestion(@RequestBody QuestionDTO payload) {
        return ApiResponse.ok(genericCrudService.createTyped("question", payload, QuestionDTO.class));
    }

    @PutMapping("/questions")
    public ApiResponse<QuestionDTO> updateQuestion(@RequestBody QuestionDTO payload) {
        return ApiResponse.ok(genericCrudService.updateTyped("question", payload, QuestionDTO.class));
    }

    @DeleteMapping("/questions")
    public ApiResponse<Boolean> deleteQuestion(@RequestBody QuestionDTO payload) {
        return ApiResponse.ok(genericCrudService.deleteTyped("question", payload));
    }

    @GetMapping("/roles")
    public ApiResponse<PageResponse<RoleDTO>> listRoles(@RequestParam Map<String, String> params) {
        return list("role", params, RoleDTO.class);
    }

    @PostMapping("/roles")
    public ApiResponse<RoleDTO> createRole(@RequestBody RoleDTO payload) {
        return ApiResponse.ok(genericCrudService.createTyped("role", payload, RoleDTO.class));
    }

    @PutMapping("/roles")
    public ApiResponse<RoleDTO> updateRole(@RequestBody RoleDTO payload) {
        return ApiResponse.ok(genericCrudService.updateTyped("role", payload, RoleDTO.class));
    }

    @DeleteMapping("/roles")
    public ApiResponse<Boolean> deleteRole(@RequestBody RoleDTO payload) {
        return ApiResponse.ok(genericCrudService.deleteTyped("role", payload));
    }

    @GetMapping("/user-roles")
    public ApiResponse<PageResponse<UserRoleDTO>> listUserRoles(@RequestParam Map<String, String> params) {
        return list("user_role", params, UserRoleDTO.class);
    }

    @PostMapping("/user-roles")
    public ApiResponse<UserRoleDTO> createUserRole(@RequestBody UserRoleDTO payload) {
        return ApiResponse.ok(genericCrudService.createTyped("user_role", payload, UserRoleDTO.class));
    }

    @PutMapping("/user-roles")
    public ApiResponse<UserRoleDTO> updateUserRole(@RequestBody UserRoleDTO payload) {
        return ApiResponse.ok(genericCrudService.updateTyped("user_role", payload, UserRoleDTO.class));
    }

    @DeleteMapping("/user-roles")
    public ApiResponse<Boolean> deleteUserRole(@RequestBody UserRoleDTO payload) {
        return ApiResponse.ok(genericCrudService.deleteTyped("user_role", payload));
    }

    @GetMapping("/admin-logs")
    public ApiResponse<PageResponse<AdminLogDTO>> listAdminLogs(@RequestParam Map<String, String> params) {
        return list("admin_log", params, AdminLogDTO.class);
    }

    @PostMapping("/admin-logs")
    public ApiResponse<AdminLogDTO> createAdminLog(@RequestBody AdminLogDTO payload) {
        return ApiResponse.ok(genericCrudService.createTyped("admin_log", payload, AdminLogDTO.class));
    }

    @PutMapping("/admin-logs")
    public ApiResponse<AdminLogDTO> updateAdminLog(@RequestBody AdminLogDTO payload) {
        return ApiResponse.ok(genericCrudService.updateTyped("admin_log", payload, AdminLogDTO.class));
    }

    @DeleteMapping("/admin-logs")
    public ApiResponse<Boolean> deleteAdminLog(@RequestBody AdminLogDTO payload) {
        return ApiResponse.ok(genericCrudService.deleteTyped("admin_log", payload));
    }

    @GetMapping("/products/{id}")
    public ApiResponse<ProductDTO> getProduct(@PathVariable String id) {
        return ApiResponse.ok(genericCrudService.getByIdTyped("product", id, ProductDTO.class));
    }

    private ApiResponse<PageResponse<ProductDTO>> listByProductType(String productType) {
        Map<String, String> params = new HashMap<>();
        params.put("product_type", productType);
        List<ProductDTO> items = genericCrudService.listTyped("product", params, ProductDTO.class);
        return ApiResponse.ok(new PageResponse<>(items.size(), items));
    }

    private <T> ApiResponse<PageResponse<T>> list(String table, Map<String, String> params, Class<T> type) {
        List<T> items = genericCrudService.listTyped(table, params, type);
        return ApiResponse.ok(new PageResponse<>(items.size(), items));
    }
}