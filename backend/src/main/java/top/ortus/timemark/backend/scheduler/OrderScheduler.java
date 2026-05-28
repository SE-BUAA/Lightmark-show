package top.ortus.timemark.backend.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import top.ortus.timemark.backend.dao.Order;
import top.ortus.timemark.backend.dao.OrderMapper;
import top.ortus.timemark.backend.service.OrderService;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class OrderScheduler {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderService orderService;

    @Scheduled(cron = "0 * * * * ?")
    public void cancelTimeoutOrders() {
        LocalDateTime timeoutTime = LocalDateTime.now().minusMinutes(10);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getStatus, 0)
            .lt(Order::getCreateTime, timeoutTime);

        List<Order> timeoutOrders = orderMapper.selectList(wrapper);
        for (Order order : timeoutOrders) {
            try {
                orderService.cancelOrder(order.getOrderNo());
            } catch (Exception ex) {
                System.err.println("Cancel timeout train order failed: " + order.getOrderNo() + ", reason: " + ex.getMessage());
            }
        }
    }
}
