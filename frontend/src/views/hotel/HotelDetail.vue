<template>
  <main class="hotel-detail-page">
    <el-button class="back-button" @click="goBack">返回上一步</el-button>
    <el-skeleton v-if="loading" :rows="8" animated />
    <template v-else>
      <section class="detail-hero">
        <el-carousel height="320px" indicator-position="outside">
          <el-carousel-item v-for="image in images" :key="image">
            <img class="hero-image" :src="image" :alt="hotel?.name" />
          </el-carousel-item>
        </el-carousel>
        <div class="hotel-heading">
          <div>
            <h1>{{ hotel?.name || '酒店详情' }}</h1>
            <p>{{ hotel?.address || '地址待补充' }}</p>
          </div>
          <div class="price-box">
            <span>{{ hotel?.rating || '--' }} 分</span>
            <strong>￥{{ hotel?.priceMin || '--' }} 起</strong>
          </div>
        </div>
      </section>

      <section class="summary-section">
        <div class="summary-column">
          <div class="summary-title-row">
            <div>
              <span class="ai-kicker">AI Review Summary</span>
              <h2>AI 评论摘要</h2>
            </div>
            <el-button size="small" :loading="summaryLoading" @click="loadReviewSummary">重新生成</el-button>
          </div>
          <p>{{ summary.overall }}</p>
          <div class="summary-tags">
            <el-tag v-for="item in summary.pros" :key="item" type="success">{{ item }}</el-tag>
            <el-tag v-for="item in summary.cons" :key="item" type="warning">{{ item }}</el-tag>
          </div>
        </div>
        <div class="summary-column">
          <h2>设施服务</h2>
          <div class="summary-tags">
            <el-tag v-for="item in hotel?.facilities || []" :key="item" effect="plain">{{ item }}</el-tag>
          </div>
        </div>
      </section>

      <section class="room-section">
        <div class="section-title-row">
          <h2>可订房型</h2>
          <span>{{ checkInDate }} 至 {{ checkOutDate }}</span>
        </div>
        <el-empty v-if="rooms.length === 0" description="暂无可订房型" />
        <div v-else class="room-list">
          <el-card v-for="room in rooms" :key="room.roomId" shadow="hover" class="room-card">
            <div class="room-info">
              <h3>{{ room.roomName }}</h3>
              <p>{{ room.bedType || '床型待确认' }} · {{ room.area || '面积待确认' }} · {{ policyText(room.cancelPolicy) }}</p>
              <span>{{ room.breakfast ? '含早餐' : '不含早餐' }}</span>
            </div>
            <div class="room-action">
              <strong>￥{{ room.pricePerNight }}</strong>
              <el-button type="primary" @click="openBooking(room)">预订</el-button>
            </div>
          </el-card>
        </div>
      </section>

      <section class="review-section">
        <div class="section-title-row">
          <h2>住客评价</h2>
          <el-button size="small" @click="loadReviews">刷新评价</el-button>
        </div>
        <el-empty v-if="reviews.length === 0" description="暂无公开评价，完成入住后可在酒店订单页评价" />
        <div v-else class="review-list">
          <article v-for="review in reviews" :key="review.id" class="review-card">
            <el-rate :model-value="review.rating" disabled />
            <p>{{ review.content }}</p>
            <span>{{ formatTime(review.createTime) }}</span>
          </article>
        </div>
      </section>
    </template>

    <el-dialog v-model="bookingVisible" title="填写订单" width="520px">
      <BookingForm
        v-if="selectedRoom"
        :room-id="selectedRoom.roomId"
        :hotel-name="hotel?.name || ''"
        :room-name="selectedRoom.roomName"
        :price-per-night="selectedRoom.pricePerNight"
        :check-in-date="checkInDate"
        :check-out-date="checkOutDate"
        @success="bookingVisible = false"
      />
    </el-dialog>
  </main>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getHotelReviews, getHotelRooms, searchHotels } from '@/api/hotelApi'
import { reviewSummary } from '@/api/aiApi'
import BookingForm from '@/views/hotel/components/BookingForm.vue'
import type { HotelReviewVO, HotelVO, ReviewSummaryVO, RoomDetailVO } from '@/types/hotel'

const route = useRoute()
const router = useRouter()
const hotel = ref<HotelVO>()
const summary = ref<ReviewSummaryVO>({ pros: [], cons: [], overall: 'AI 评论摘要加载中。' })
const rooms = ref<RoomDetailVO[]>([])
const reviews = ref<HotelReviewVO[]>([])
const loading = ref(false)
const summaryLoading = ref(false)
const bookingVisible = ref(false)
const selectedRoom = ref<RoomDetailVO>()

const hotelId = computed(() => String(route.params.hotelId || ''))
const checkInDate = computed(() => String(route.query.checkIn || new Date().toISOString().slice(0, 10)))
const checkOutDate = computed(() => {
  if (route.query.checkOut) return String(route.query.checkOut)
  const tomorrow = new Date(Date.now() + 86400000)
  return tomorrow.toISOString().slice(0, 10)
})

const images = computed(() => [
  hotel.value?.coverImage || 'https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1200&q=80',
  'https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?auto=format&fit=crop&w=1200&q=80',
  'https://images.unsplash.com/photo-1564501049412-61c2a3083791?auto=format&fit=crop&w=1200&q=80'
])

const policyText = (policy?: string) => {
  if (policy === 'FREE_CANCEL') return '免费取消'
  if (policy === 'LIMITED_CANCEL') return '限时取消'
  if (policy === 'NON_REFUNDABLE') return '不可取消'
  return policy || '按酒店政策'
}

const formatTime = (value: string) => value ? value.replace('T', ' ').slice(0, 16) : ''

const openBooking = (room: RoomDetailVO) => {
  selectedRoom.value = room
  bookingVisible.value = true
}

const goBack = () => router.back()

const loadReviewSummary = async () => {
  summaryLoading.value = true
  try {
    summary.value = await reviewSummary(hotelId.value)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : 'AI 评论摘要加载失败')
  } finally {
    summaryLoading.value = false
  }
}

const loadReviews = async () => {
  try {
    reviews.value = await getHotelReviews(hotelId.value)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '评价加载失败')
  }
}

const loadHotel = async () => {
  loading.value = true
  try {
    const result = await searchHotels({ page: 1, size: 100 })
    hotel.value = result.records.find(item => item.id === hotelId.value) || result.records[0]
    rooms.value = await getHotelRooms(hotelId.value, checkInDate.value, checkOutDate.value)
    await Promise.all([loadReviews(), loadReviewSummary()])
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '酒店详情加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadHotel)
</script>

<style scoped>
.hotel-detail-page {
  min-height: 100vh;
  padding: 88px clamp(16px, 5vw, 64px) 48px;
  background: var(--el-bg-color-page);
}

.back-button {
  max-width: 1180px;
  margin: 0 auto 16px;
  display: block;
}

.detail-hero,
.summary-section,
.room-section,
.review-section {
  max-width: 1180px;
  margin: 0 auto 22px;
}

.hero-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.hotel-heading,
.summary-section,
.section-title-row,
.room-card :deep(.el-card__body) {
  display: flex;
  justify-content: space-between;
  gap: 18px;
}

.hotel-heading {
  align-items: flex-end;
  padding: 18px 0 0;
}

.hotel-heading h1,
.section-title-row h2,
.summary-column h2,
.room-info h3 {
  margin: 0;
}

.hotel-heading p,
.room-info p {
  margin: 8px 0 0;
  color: var(--el-text-color-secondary);
}

.price-box {
  display: grid;
  gap: 6px;
  text-align: right;
}

.price-box strong,
.room-action strong {
  color: var(--el-color-danger);
  font-size: 22px;
}

.summary-section {
  display: grid;
  grid-template-columns: 1.4fr 1fr;
}

.summary-column,
.room-card,
.review-card {
  padding: 18px;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
}

.summary-title-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.ai-kicker {
  color: var(--el-color-primary);
  font-size: 12px;
  font-weight: 700;
}

.summary-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.section-title-row {
  align-items: center;
  margin-bottom: 14px;
}

.section-title-row span,
.review-card span {
  color: var(--el-text-color-secondary);
}

.room-list,
.review-list {
  display: grid;
  gap: 14px;
}

.room-action {
  display: grid;
  gap: 12px;
  justify-items: end;
}

.review-card p {
  margin: 10px 0;
  line-height: 1.7;
}

@media (max-width: 760px) {
  .hotel-heading,
  .summary-section,
  .section-title-row,
  .room-card :deep(.el-card__body) {
    display: grid;
  }

  .summary-section {
    grid-template-columns: 1fr;
  }

  .price-box,
  .room-action {
    text-align: left;
    justify-items: start;
  }
}
</style>
