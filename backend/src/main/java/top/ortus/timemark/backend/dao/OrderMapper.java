package top.ortus.timemark.backend.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    @Update("UPDATE product SET sold_count = sold_count + #{count} WHERE id = #{productId} AND stock - sold_count >= #{count}")
    int decrementStock(@Param("productId") String productId, @Param("count") int count);

    @Update("UPDATE product SET sold_count = CASE WHEN sold_count >= #{count} THEN sold_count - #{count} ELSE 0 END WHERE id = #{productId}")
    int incrementStock(@Param("productId") String productId, @Param("count") int count);

    @Update("UPDATE orders SET status = 2, cancel_reason = #{reason}, update_time = NOW() WHERE order_no = #{orderNo} AND status = 0")
    int cancelPendingOrder(@Param("orderNo") String orderNo, @Param("reason") String reason);

    @Update("UPDATE orders SET status = 4, cancel_reason = #{reason}, update_time = NOW() WHERE order_no = #{orderNo} AND status = 1")
    int refundPaidOrder(@Param("orderNo") String orderNo, @Param("reason") String reason);

    @Update("UPDATE orders SET status = 5, pickup_code = NULL, changed_once = 1, cancel_reason = #{reason}, update_time = NOW() WHERE order_no = #{orderNo} AND status = 1 AND COALESCE(changed_once, 0) = 0")
    int markChangedOrder(@Param("orderNo") String orderNo, @Param("reason") String reason);

    @Select("SELECT COUNT(1) FROM orders WHERE order_no = #{orderNo}")
    int countByOrderNo(@Param("orderNo") String orderNo);

    @Select("SELECT COUNT(1) FROM orders WHERE pickup_code = #{pickupCode}")
    int countByPickupCode(@Param("pickupCode") String pickupCode);
}
