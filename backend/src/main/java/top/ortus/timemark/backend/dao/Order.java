package top.ortus.timemark.backend.dao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("orders")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private String orderType;
    private BigDecimal totalAmount;
    private BigDecimal payAmount;
    private String paymentMethod;
    private Integer pointsDeduct;
    private String source;
    private Integer status; // 0: 待支付, 1: 已支付, 2: 已取消
    private LocalDateTime payDeadline;
    private LocalDateTime payTime;
    private String cancelReason;
    private String pickupCode;
    
    // 扩展字段，记录车次座位乘车人等信息
    private String extraInfo; 

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
