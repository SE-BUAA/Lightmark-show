import { http } from '@/utils/request'
import type { AIRecommendResultVO, ReviewSummaryVO } from '@/types/hotel'

export function recommendHotel(userInput: string): Promise<AIRecommendResultVO> {
  return http.post<AIRecommendResultVO>('/ai/hotel/recommend', { userInput })
}

export function reviewSummary(hotelId: string | number): Promise<ReviewSummaryVO> {
  return http.get<ReviewSummaryVO>(`/ai/hotel/review-summary/${hotelId}`)
}
