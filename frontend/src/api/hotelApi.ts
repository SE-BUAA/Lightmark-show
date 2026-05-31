import { http } from '@/utils/request'
import type {
  CreateHotelOrderRequest,
  HotelOrderDetailVO,
  HotelOrderListVO,
  HotelReviewRequest,
  HotelReviewVO,
  HotelSearchParams,
  HotelVO,
  InvoiceRequestDTO,
  OrderResult,
  PageResult,
  RoomDetailVO
} from '@/types/hotel'

export function searchHotels(params: HotelSearchParams): Promise<PageResult<HotelVO>> {
  return http.get<PageResult<HotelVO>>('/hotel/list', { params })
}

export function getRoomDetail(roomId: number, checkIn: string, checkOut: string): Promise<RoomDetailVO> {
  return http.get<RoomDetailVO>(`/hotel/room/${roomId}`, {
    params: { checkIn, checkOut }
  })
}

export function getHotelRooms(hotelId: string | number, checkIn: string, checkOut: string): Promise<RoomDetailVO[]> {
  return http.get<RoomDetailVO[]>(`/hotel/${hotelId}/rooms`, {
    params: { checkIn, checkOut }
  })
}

export function createOrder(data: CreateHotelOrderRequest): Promise<OrderResult> {
  return http.post<OrderResult>('/hotel/order', data)
}

export function getOrders(params: { status?: number; page?: number; size?: number }): Promise<PageResult<HotelOrderListVO>> {
  return http.get<PageResult<HotelOrderListVO>>('/hotel/orders', { params })
}

export function cancelOrder(orderId: number): Promise<void> {
  return http.post<void>(`/hotel/order/${orderId}/cancel`)
}

export function getOrderDetail(orderId: number): Promise<HotelOrderDetailVO> {
  return http.get<HotelOrderDetailVO>(`/hotel/order/${orderId}`)
}

export function payOrder(orderId: number, paymentMethod?: string): Promise<HotelOrderDetailVO> {
  return http.post<HotelOrderDetailVO>(`/hotel/order/${orderId}/pay`, { paymentMethod })
}

export function applyInvoice(orderId: number, data: InvoiceRequestDTO): Promise<void> {
  return http.post<void>(`/hotel/order/${orderId}/invoice`, data)
}

export function getHotelReviews(hotelId: string | number): Promise<HotelReviewVO[]> {
  return http.get<HotelReviewVO[]>(`/hotel/${hotelId}/reviews`)
}

export function createHotelReview(orderId: number, data: HotelReviewRequest): Promise<HotelReviewVO> {
  return http.post<HotelReviewVO>(`/hotel/order/${orderId}/review`, data)
}
