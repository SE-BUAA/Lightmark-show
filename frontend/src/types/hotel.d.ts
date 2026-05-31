export interface PageResult<T> {
  total: number
  records: T[]
}

export interface HotelVO {
  id: string
  name: string
  address?: string
  starLevel?: number
  rating?: number
  priceMin?: number
  distance?: number
  coverImage?: string
  facilities?: string[]
  cancelPolicy?: string
}

export interface HotelSearchParams {
  keyword?: string
  checkInDate?: string
  checkOutDate?: string
  roomNum?: number
  adultNum?: number
  sort?: string
  maxPrice?: number
  page?: number
  size?: number
  starLevel?: number
  brand?: string
  facility?: string
  cancelPolicy?: string
  lat?: number
  lng?: number
  radius?: number
}

export interface RoomDetailVO {
  roomId: number
  roomName: string
  bedType?: string
  area?: string
  breakfast?: number
  cancelPolicy?: string
  pricePerNight: number
  totalPrice: number
  checkInDate: string
  checkOutDate: string
  nights: number
}

export interface GuestInfo {
  name: string
  idCard: string
  phone: string
}

export interface CreateHotelOrderRequest {
  roomId: number
  checkInDate: string
  checkOutDate: string
  roomNum: number
  guestList: GuestInfo[]
  pointsDeduced?: number
  paymentMethod?: string
}

export interface OrderResult {
  orderId: number
  payAmount: number
}

export interface HotelOrderListVO {
  orderId: number
  orderNo: string
  hotelName: string
  roomName: string
  checkInDate: string
  checkOutDate: string
  totalAmount: number
  status: number
  createTime: string
}

export interface HotelOrderDetailVO {
  orderId: number
  orderNo: string
  hotelName: string
  roomId: number
  roomName: string
  checkInDate: string
  checkOutDate: string
  roomNum: number
  guestList: GuestInfo[]
  pricePerNight: number
  totalAmount: number
  pointsDeducted: number
  payAmount: number
  status: number
  paymentMethod?: string
  cancelPolicy?: string
  payDeadline?: string
  createTime?: string
}

export interface HotelReviewVO {
  id: number
  orderId: number
  userId: number
  rating: number
  content: string
  createTime: string
}

export interface HotelReviewRequest {
  rating: number
  content: string
}

export interface InvoiceRequestDTO {
  invoiceType: string
  title: string
  taxNo?: string
}

export interface ReviewSummaryVO {
  pros: string[]
  cons: string[]
  overall: string
}

export interface AIRecommendResultVO {
  recommendText: string
  hotels: HotelVO[]
}
