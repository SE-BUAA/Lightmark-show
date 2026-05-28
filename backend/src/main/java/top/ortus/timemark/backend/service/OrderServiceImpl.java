package top.ortus.timemark.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.ortus.timemark.backend.dao.Order;
import top.ortus.timemark.backend.dao.OrderMapper;
import top.ortus.timemark.backend.dao.Product;
import top.ortus.timemark.backend.dao.ProductMapper;
import top.ortus.timemark.backend.dto.module.TrainOrderRequest;
import top.ortus.timemark.backend.dto.module.TrainOrderResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {
    private static final int PENDING = 0;
    private static final int PAID = 1;
    private static final int CANCELED = 2;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TrainOrderResponse createTrainOrder(Long userId, TrainOrderRequest request) {
        validateRequest(request);

        Product product = productMapper.selectById(request.getProductId());
        if (product == null || !"TRAIN".equals(product.getProductType()) || !Integer.valueOf(1).equals(product.getStatus())) {
            throw new IllegalArgumentException("火车票产品不存在或已下架");
        }
        if (product.getCategoryTags() == null || !product.getCategoryTags().contains(request.getSeatType())) {
            throw new IllegalArgumentException("所选座位类型不适用于该车次");
        }

        int updatedCount = orderMapper.decrementStock(request.getProductId(), 1);
        if (updatedCount == 0) {
            throw new IllegalArgumentException("余票不足，下单失败");
        }

        LocalDateTime now = LocalDateTime.now();
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setOrderType("TRAIN");
        BigDecimal originalAmount = BigDecimal.valueOf(product.getPrice()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal payAmount = calculatePayAmount(originalAmount, request);
        order.setTotalAmount(originalAmount);
        order.setPayAmount(payAmount);
        order.setPointsDeduct(0);
        order.setSource("PC");
        order.setStatus(PENDING);
        order.setPayDeadline(now.plusMinutes(10));
        order.setExtraInfo(writeExtraInfo(product, request));
        order.setCreateTime(now);
        order.setUpdateTime(now);
        orderMapper.insert(order);

        return toResponse(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TrainOrderResponse payOrder(String orderNo) {
        Order order = getOrderByNo(orderNo);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (order.getStatus() == CANCELED) {
            throw new IllegalArgumentException("订单已取消，无法支付");
        }
        if (order.getStatus() == PAID) {
            return toResponse(order);
        }
        if (order.getCreateTime().plusMinutes(10).isBefore(LocalDateTime.now())) {
            cancelOrder(orderNo);
            throw new IllegalArgumentException("订单已超时取消");
        }

        order.setStatus(PAID);
        order.setPaymentMethod("MOCK_PAY");
        order.setPayTime(LocalDateTime.now());
        order.setPickupCode(generatePickupCode());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
        return toResponse(order);
    }

    @Override
    public Order getOrderByNo(String orderNo) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderNo, orderNo);
        return orderMapper.selectOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(String orderNo) {
        Order order = getOrderByNo(orderNo);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (order.getStatus() == CANCELED) {
            return;
        }
        if (order.getStatus() != PENDING) {
            throw new IllegalArgumentException("只能取消待支付订单");
        }

        String productId = readProductId(order);
        int changed = orderMapper.cancelPendingOrder(orderNo, "超时或用户主动取消");
        if (changed == 0) {
            return;
        }
        if (productId != null && !productId.isBlank()) {
            orderMapper.incrementStock(productId, 1);
        }
    }

    private void validateRequest(TrainOrderRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("请求参数不能为空");
        }
        if (request.getProductId() == null || request.getProductId().isBlank()) {
            throw new IllegalArgumentException("车次不能为空");
        }
        if (request.getPassengerName() == null || !request.getPassengerName().matches("^[\\u4e00-\\u9fa5]{2,20}$")) {
            throw new IllegalArgumentException("姓名必须为2-20个汉字");
        }
        if (request.getPassengerPhone() == null || !request.getPassengerPhone().matches("^1[3-9]\\d{9}$")) {
            throw new IllegalArgumentException("请输入正确的中国大陆手机号");
        }
        if (request.getPassengerAge() == null || request.getPassengerAge() < 1 || request.getPassengerAge() > 120) {
            throw new IllegalArgumentException("年龄必须为1-120之间的正整数");
        }
        if (request.getSeatType() == null || request.getSeatType().isBlank()) {
            throw new IllegalArgumentException("座位类型不能为空");
        }
    }

    private String writeExtraInfo(Product product, TrainOrderRequest request) {
        Map<String, Object> extraInfo = new LinkedHashMap<>();
        extraInfo.put("trainName", product.getName());
        extraInfo.put("startStation", product.getExtra() == null ? null : product.getExtra().get("start_station"));
        extraInfo.put("endStation", product.getExtra() == null ? null : product.getExtra().get("end_station"));
        extraInfo.put("date", product.getExtra() == null ? null : product.getExtra().get("date"));
        extraInfo.put("seatType", request.getSeatType());
        extraInfo.put("ticketType", resolveTicketType(request));
        extraInfo.put("discountRate", resolveDiscountRate(request));
        extraInfo.put("originalPrice", product.getPrice());
        extraInfo.put("passengerName", request.getPassengerName());
        extraInfo.put("passengerPhone", request.getPassengerPhone());
        extraInfo.put("passengerAge", request.getPassengerAge());
        extraInfo.put("productId", request.getProductId());
        try {
            return objectMapper.writeValueAsString(extraInfo);
        } catch (Exception ex) {
            throw new IllegalArgumentException("订单信息序列化失败");
        }
    }

    private String readProductId(Order order) {
        if (order.getExtraInfo() == null || order.getExtraInfo().isBlank()) {
            return null;
        }
        try {
            Map<String, Object> extraInfo = objectMapper.readValue(order.getExtraInfo(), new TypeReference<>() {});
            Object productId = extraInfo.get("productId");
            return productId == null ? null : String.valueOf(productId);
        } catch (Exception ex) {
            return null;
        }
    }

    private BigDecimal calculatePayAmount(BigDecimal originalAmount, TrainOrderRequest request) {
        return originalAmount.multiply(BigDecimal.valueOf(resolveDiscountRate(request))).setScale(2, RoundingMode.HALF_UP);
    }

    private double resolveDiscountRate(TrainOrderRequest request) {
        if (Boolean.TRUE.equals(request.getIsStudent())) {
            return 0.6;
        }
        if (request.getPassengerAge() != null && request.getPassengerAge() < 18) {
            return 0.8;
        }
        return 1.0;
    }

    private String resolveTicketType(TrainOrderRequest request) {
        if (Boolean.TRUE.equals(request.getIsStudent())) {
            return "STUDENT";
        }
        if (request.getPassengerAge() != null && request.getPassengerAge() < 18) {
            return "CHILD";
        }
        return "ADULT";
    }

    private TrainOrderResponse toResponse(Order order) {
        LocalDateTime createTime = order.getCreateTime();
        return new TrainOrderResponse(
            order.getOrderNo(),
            order.getStatus(),
            order.getPayAmount(),
            createTime,
            order.getPayDeadline() == null ? (createTime == null ? null : createTime.plusMinutes(10)) : order.getPayDeadline(),
            order.getPickupCode()
        );
    }

    private String generateOrderNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String orderNo;
        do {
            orderNo = dateStr + String.format("%06d", RANDOM.nextInt(1_000_000));
        } while (orderMapper.countByOrderNo(orderNo) > 0);
        return orderNo;
    }

    private String generatePickupCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        String code;
        do {
            StringBuilder sb = new StringBuilder(6);
            for (int i = 0; i < 6; i++) {
                sb.append(chars.charAt(RANDOM.nextInt(chars.length())));
            }
            code = sb.toString();
        } while (orderMapper.countByPickupCode(code) > 0);
        return code;
    }
}
