package top.ortus.timemark.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.ortus.timemark.backend.common.ApiResponse;
import top.ortus.timemark.backend.dao.Order;
import top.ortus.timemark.backend.dto.module.TrainOrderRequest;
import top.ortus.timemark.backend.dto.module.TrainOrderResponse;
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

    @PostMapping("/train/{orderNo}/pay")
    public ApiResponse<TrainOrderResponse> payOrder(@PathVariable String orderNo) {
        return ApiResponse.ok(orderService.payOrder(orderNo));
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
