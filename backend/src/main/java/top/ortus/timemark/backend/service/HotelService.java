package top.ortus.timemark.backend.service;

import org.springframework.transaction.annotation.Transactional;
import top.ortus.timemark.backend.common.PageResult;
import top.ortus.timemark.backend.dto.CreateHotelOrderRequest;
import top.ortus.timemark.backend.dto.HotelOrderDetailVO;
import top.ortus.timemark.backend.dto.HotelOrderListVO;
import top.ortus.timemark.backend.dto.HotelReviewRequest;
import top.ortus.timemark.backend.dto.HotelReviewVO;
import top.ortus.timemark.backend.dto.HotelSearchDTO;
import top.ortus.timemark.backend.dto.InvoiceRequestDTO;
import top.ortus.timemark.backend.dto.OrderResultVO;
import top.ortus.timemark.backend.dto.RoomDetailVO;
import top.ortus.timemark.backend.vo.HotelVO;

import java.util.List;

public interface HotelService {

    @Transactional(readOnly = true)
    PageResult<HotelVO> searchHotels(Long userId, HotelSearchDTO query);

    @Transactional(readOnly = true)
    RoomDetailVO getRoomDetail(Long roomId, String checkInDate, String checkOutDate);

    @Transactional(readOnly = true)
    List<RoomDetailVO> listHotelRooms(Long hotelId, String checkInDate, String checkOutDate);

    @Transactional(rollbackFor = Exception.class)
    OrderResultVO createOrder(Long userId, CreateHotelOrderRequest request);

    @Transactional(rollbackFor = Exception.class)
    HotelOrderDetailVO payHotelOrder(Long userId, Long orderId, String paymentMethod);

    @Transactional(readOnly = true)
    PageResult<HotelOrderListVO> listHotelOrders(Long userId, Integer status, Integer page, Integer size);

    @Transactional(readOnly = true)
    HotelOrderDetailVO getHotelOrderDetail(Long userId, Long orderId);

    @Transactional(rollbackFor = Exception.class)
    void cancelHotelOrder(Long userId, Long orderId);

    @Transactional(rollbackFor = Exception.class)
    void applyInvoice(Long userId, Long orderId, InvoiceRequestDTO request);

    @Transactional(readOnly = true)
    List<HotelReviewVO> listHotelReviews(Long hotelId, Integer page, Integer size);

    @Transactional(rollbackFor = Exception.class)
    HotelReviewVO createHotelReview(Long userId, Long orderId, HotelReviewRequest request);
}
