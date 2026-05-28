package top.ortus.timemark.backend.service;

import top.ortus.timemark.backend.dto.module.TrainOrderRequest;
import top.ortus.timemark.backend.dao.Order;
import top.ortus.timemark.backend.dto.module.TrainOrderResponse;

public interface OrderService {
    TrainOrderResponse createTrainOrder(Long userId, TrainOrderRequest request);
    TrainOrderResponse payOrder(String orderNo);
    Order getOrderByNo(String orderNo);
    void cancelOrder(String orderNo);
}
