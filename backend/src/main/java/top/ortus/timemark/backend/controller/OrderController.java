package top.ortus.timemark.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.ortus.timemark.backend.common.ApiResponse;
import top.ortus.timemark.backend.dao.Order;
import top.ortus.timemark.backend.dto.module.TrainChangeConfirmRequest;
import top.ortus.timemark.backend.dto.module.TrainChangeResponse;
import top.ortus.timemark.backend.dto.module.TrainChangePreviewResponse;
import top.ortus.timemark.backend.dto.module.TrainOrderRequest;
import top.ortus.timemark.backend.dto.module.TrainOrderResponse;
import top.ortus.timemark.backend.dto.module.TrainRefundResponse;
import top.ortus.timemark.backend.dto.module.VacationAssistantResponse;
import top.ortus.timemark.backend.dto.module.VacationOrderRequest;
import top.ortus.timemark.backend.dto.module.VacationRefundResponse;
import top.ortus.timemark.backend.service.OrderService;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/train")
    public ApiResponse<TrainOrderResponse> createTrainOrder(@RequestBody TrainOrderRequest request) {
        Long userId = 2L;
        return ApiResponse.ok(orderService.createTrainOrder(userId, request));
    }

    @PostMapping("/vacation")
    public ApiResponse<TrainOrderResponse> createVacationOrder(@RequestBody VacationOrderRequest request) {
        Long userId = 2L;
        return ApiResponse.ok(orderService.createVacationOrder(userId, request));
    }

    @PostMapping("/train/{orderNo}/pay")
    public ApiResponse<TrainOrderResponse> payOrder(@PathVariable String orderNo) {
        return ApiResponse.ok(orderService.payOrder(orderNo));
    }

    @PostMapping("/train/{orderNo}/refund")
    public ApiResponse<TrainRefundResponse> refundTrainOrder(@PathVariable String orderNo) {
        return ApiResponse.ok(orderService.refundTrainOrder(orderNo));
    }

    @PostMapping("/train/refund")
    public ApiResponse<TrainRefundResponse> refundTrainOrderByPickupCode(@RequestParam String pickupCode) {
        return ApiResponse.ok(orderService.refundTrainOrderByPickupCode(pickupCode));
    }

    @PostMapping("/vacation/{orderNo}/refund")
    public ApiResponse<VacationRefundResponse> refundVacationOrder(@PathVariable String orderNo) {
        return ApiResponse.ok(orderService.refundVacationOrder(orderNo));
    }

    @PostMapping("/vacation/refund")
    public ApiResponse<VacationRefundResponse> refundVacationOrderByPickupCode(@RequestParam String pickupCode) {
        return ApiResponse.ok(orderService.refundVacationOrderByPickupCode(pickupCode));
    }

    @GetMapping("/vacation/{orderNo}/assistant")
    public ApiResponse<VacationAssistantResponse> generateVacationAssistant(@PathVariable String orderNo) {
        return ApiResponse.ok(orderService.generateVacationAssistant(orderNo));
    }

    @GetMapping("/train/change")
    public ApiResponse<TrainChangePreviewResponse> previewTrainChange(@RequestParam String pickupCode) {
        return ApiResponse.ok(orderService.previewTrainChange(pickupCode));
    }

    @PostMapping("/train/change")
    public ApiResponse<TrainChangeResponse> changeTrainOrder(@RequestBody TrainChangeConfirmRequest request) {
        return ApiResponse.ok(orderService.changeTrainOrder(request.getPickupCode(), request.getTargetProductId()));
    }

    @GetMapping("/{orderNo}")
    public ApiResponse<Order> getOrderByNo(@PathVariable String orderNo) {
        return ApiResponse.ok(orderService.getOrderByNo(orderNo));
    }

    @PostMapping("/train/{orderNo}/cancel")
    public ApiResponse<Void> cancelOrder(@PathVariable String orderNo) {
        orderService.cancelOrder(orderNo);
        return ApiResponse.ok(null);
    }
}
