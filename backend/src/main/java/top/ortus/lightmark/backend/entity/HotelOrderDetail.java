package top.ortus.lightmark.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("hotel_order_detail")
public class HotelOrderDetail {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Long roomId;

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    private Integer roomNum;

    private String guestList;

    private BigDecimal totalPrice;

    private Integer pointsDeducted;

    private BigDecimal payAmount;
}
