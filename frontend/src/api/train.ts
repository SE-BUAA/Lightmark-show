import { http } from '@/utils/request'

export interface TrainProduct {
  id: string
  name: string
  price: number
  stock: number
  soldCount: number
  categoryTags?: string[]
  extra?: Record<string, unknown>
}

export interface TrainSearchPayload {
  startStation?: string
  endStation?: string
  date?: string
  trainTypes: string[]
  seatTypes: string[]
}

export interface TrainOptions {
  startStations: string[]
  endStations: string[]
  dates: string[]
}

export interface TrainOrderPayload {
  productId: string
  passengerName: string
  passengerPhone: string
  passengerAge: number
  seatType: string
  isStudent: boolean
}

export interface TrainOrderResponse {
  orderNo: string
  status: number
  payAmount: number
  createTime: string
  expireTime: string
  pickupCode?: string
}

export interface TrainRefundResponse {
  orderNo: string
  status: number
  paidAmount: number
  refundAmount: number
  refundRule: string
}

export interface TrainChangePreviewResponse {
  orderNo: string
  trainName: string
  startStation: string
  endStation: string
  seatType: string
  candidates: TrainProduct[]
}

export interface TrainChangeResponse {
  oldOrderNo: string
  newOrderNo: string
  pickupCode: string
  oldPayAmount: number
  newPayAmount: number
  differenceAmount: number
  differenceType: 'PAY' | 'REFUND' | 'NONE'
  message: string
}

export interface TrainOrderDetail {
  orderNo: string
  status: number
  payAmount: number
  pickupCode?: string
  createTime: string
  extraInfo?: string
}

export function getTrainOptions() {
  return http.get<TrainOptions>('/trains/options')
}

export function searchTrains(data: TrainSearchPayload) {
  return http.post<TrainProduct[]>('/trains/search', data)
}

export function createTrainOrder(data: TrainOrderPayload) {
  return http.post<TrainOrderResponse>('/orders/train', data)
}

export function payOrder(orderNo: string) {
  return http.post<TrainOrderResponse>(`/orders/train/${orderNo}/pay`)
}

export function refundTrainOrder(orderNo: string) {
  return http.post<TrainRefundResponse>(`/orders/train/${orderNo}/refund`)
}

export function refundTrainOrderByPickupCode(pickupCode: string) {
  return http.post<TrainRefundResponse>('/orders/train/refund', undefined, {
    params: { pickupCode }
  })
}

export function previewTrainChange(pickupCode: string) {
  return http.get<TrainChangePreviewResponse>('/orders/train/change', {
    params: { pickupCode }
  })
}

export function changeTrainOrder(pickupCode: string, targetProductId: string) {
  return http.post<TrainChangeResponse>('/orders/train/change', {
    pickupCode,
    targetProductId
  })
}

export function getOrder(orderNo: string) {
  return http.get<TrainOrderDetail>(`/orders/${orderNo}`)
}

export function cancelOrder(orderNo: string) {
  return http.post<void>(`/orders/train/${orderNo}/cancel`)
}
