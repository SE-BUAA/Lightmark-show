package top.ortus.timemark.backend.service;

import top.ortus.timemark.backend.dto.module.TrainOrderRequest;
import top.ortus.timemark.backend.dao.Order;
import top.ortus.timemark.backend.dto.module.TrainChangeResponse;
import top.ortus.timemark.backend.dto.module.TrainChangePreviewResponse;
import top.ortus.timemark.backend.dto.module.TrainOrderResponse;
import top.ortus.timemark.backend.dto.module.TrainRefundResponse;

public interface OrderService {
    TrainOrderResponse createTrainOrder(Long userId, TrainOrderRequest request);
    TrainOrderResponse payOrder(String orderNo);
    TrainRefundResponse refundTrainOrder(String orderNo);
    TrainRefundResponse refundTrainOrderByPickupCode(String pickupCode);
    TrainChangePreviewResponse previewTrainChange(String pickupCode);
    TrainChangeResponse changeTrainOrder(String pickupCode, String targetProductId);
    Order getOrderByNo(String orderNo);
    void cancelOrder(String orderNo);
}
