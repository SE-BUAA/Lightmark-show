package top.ortus.lightmark.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.ortus.lightmark.backend.JwtTokenService;
import top.ortus.lightmark.backend.common.ApiResponse;
import top.ortus.lightmark.backend.common.PageResult;
import top.ortus.lightmark.backend.dto.CreateHotelOrderRequest;
import top.ortus.lightmark.backend.dto.HotelOrderDetailVO;
import top.ortus.lightmark.backend.dto.HotelOrderListVO;
import top.ortus.lightmark.backend.dto.HotelReviewRequest;
import top.ortus.lightmark.backend.dto.HotelReviewVO;
import top.ortus.lightmark.backend.dto.HotelSearchDTO;
import top.ortus.lightmark.backend.dto.InvoiceRequestDTO;
import top.ortus.lightmark.backend.dto.OrderResultVO;
import top.ortus.lightmark.backend.dto.RoomDetailVO;
import top.ortus.lightmark.backend.exception.ApiException;
import top.ortus.lightmark.backend.service.HotelService;
import top.ortus.lightmark.backend.vo.HotelVO;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hotel")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;
    private final JwtTokenService jwtTokenService;

    @GetMapping("/list")
    public ApiResponse<PageResult<HotelVO>> listHotels(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @ModelAttribute HotelSearchDTO query) {
        Long userId = resolveOptionalUserId(authorization);
        return ApiResponse.ok(hotelService.searchHotels(userId, query));
    }

    @GetMapping("/{hotelId:\\d+}")
    public ApiResponse<HotelVO> getHotel(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long hotelId) {
        resolveUserId(authorization);
        return ApiResponse.ok(hotelService.getHotel(hotelId));
    }

    @GetMapping("/rooms")
    public ApiResponse<List<?>> noopRooms() {
        return ApiResponse.ok(List.of());
    }

    @GetMapping("/room/{roomId}")
    public ApiResponse<RoomDetailVO> getRoomDetail(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long roomId,
            @RequestParam String checkIn,
            @RequestParam String checkOut) {
        resolveUserId(authorization);
        return ApiResponse.ok(hotelService.getRoomDetail(roomId, checkIn, checkOut));
    }

    @GetMapping("/{hotelId}/rooms")
    public ApiResponse<List<RoomDetailVO>> listHotelRooms(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long hotelId,
            @RequestParam String checkIn,
            @RequestParam String checkOut) {
        resolveUserId(authorization);
        return ApiResponse.ok(hotelService.listHotelRooms(hotelId, checkIn, checkOut));
    }

    @PostMapping("/order")
    public ApiResponse<OrderResultVO> createHotelOrder(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody CreateHotelOrderRequest request) {
        Long userId = resolveUserId(authorization);
        return ApiResponse.ok(hotelService.createOrder(userId, request));
    }

    @PostMapping("/order/{orderId}/pay")
    public ApiResponse<HotelOrderDetailVO> payHotelOrder(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long orderId,
            @RequestBody(required = false) Map<String, String> request) {
        Long userId = resolveUserId(authorization);
        String paymentMethod = request == null ? null : request.get("paymentMethod");
        return ApiResponse.ok(hotelService.payHotelOrder(userId, orderId, paymentMethod));
    }

    @GetMapping("/orders")
    public ApiResponse<PageResult<HotelOrderListVO>> listHotelOrders(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = resolveUserId(authorization);
        return ApiResponse.ok(hotelService.listHotelOrders(userId, status, page, size));
    }

    @GetMapping("/order/{orderId}")
    public ApiResponse<HotelOrderDetailVO> getHotelOrderDetail(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long orderId) {
        Long userId = resolveUserId(authorization);
        return ApiResponse.ok(hotelService.getHotelOrderDetail(userId, orderId));
    }

    @PostMapping("/order/{orderId}/cancel")
    public ApiResponse<Void> cancelHotelOrder(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long orderId) {
        Long userId = resolveUserId(authorization);
        hotelService.cancelHotelOrder(userId, orderId);
        return ApiResponse.ok(null);
    }

    @PostMapping("/order/{orderId}/invoice")
    public ApiResponse<Void> applyInvoice(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long orderId,
            @RequestBody InvoiceRequestDTO request) {
        Long userId = resolveUserId(authorization);
        hotelService.applyInvoice(userId, orderId, request);
        return ApiResponse.ok(null);
    }

    @GetMapping("/{hotelId}/reviews")
    public ApiResponse<List<HotelReviewVO>> listHotelReviews(
            @PathVariable Long hotelId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        return ApiResponse.ok(hotelService.listHotelReviews(hotelId, page, size));
    }

    @PostMapping("/order/{orderId}/review")
    public ApiResponse<HotelReviewVO> createHotelReview(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long orderId,
            @RequestBody HotelReviewRequest request) {
        Long userId = resolveUserId(authorization);
        return ApiResponse.ok(hotelService.createHotelReview(userId, orderId, request));
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

    private Long resolveOptionalUserId(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            return null;
        }
        String token = authorization.startsWith("Bearer ")
                ? authorization.substring("Bearer ".length())
                : authorization;
        return jwtTokenService.resolveUserId(token);
    }
}
