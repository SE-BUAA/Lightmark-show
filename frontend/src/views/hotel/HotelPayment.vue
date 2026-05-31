<template>
  <main class="hotel-payment-page">
    <section class="payment-card">
      <el-button class="back-button" @click="goBack">返回上一步</el-button>

      <el-result
        v-if="paid"
        icon="success"
        title="支付成功！"
        sub-title="酒店订单已支付，可在酒店订单中查看或取消。"
      >
        <template #extra>
          <el-button type="primary" @click="goOrders">查看酒店订单</el-button>
          <el-button @click="goSearch">继续搜索酒店</el-button>
        </template>
      </el-result>

      <template v-else>
        <div class="page-title">
          <span>Hotel Payment</span>
          <h1>订单支付</h1>
        </div>
        <el-skeleton v-if="loading" :rows="6" animated />
        <el-descriptions v-else-if="order" :column="1" border>
          <el-descriptions-item label="订单号">{{ order.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="酒店">{{ order.hotelName }}</el-descriptions-item>
          <el-descriptions-item label="房型">{{ order.roomName }}</el-descriptions-item>
          <el-descriptions-item label="入住日期">{{ order.checkInDate }} 至 {{ order.checkOutDate }}</el-descriptions-item>
          <el-descriptions-item label="房间数">{{ order.roomNum }}</el-descriptions-item>
          <el-descriptions-item label="支付方式">{{ paymentMethodText(order.paymentMethod) }}</el-descriptions-item>
          <el-descriptions-item label="应付金额">¥{{ Number(order.payAmount || 0).toFixed(2) }}</el-descriptions-item>
        </el-descriptions>
        <div class="pay-actions">
          <span v-if="order" class="pay-method">将使用 {{ paymentMethodText(order.paymentMethod) }} 支付</span>
          <el-button type="primary" size="large" :loading="paying" @click="handlePay">确认支付</el-button>
        </div>
      </template>
    </section>
  </main>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getOrderDetail, payOrder } from '@/api/hotelApi'
import type { HotelOrderDetailVO } from '@/types/hotel'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const paying = ref(false)
const paid = ref(false)
const order = ref<HotelOrderDetailVO>()

const orderId = Number(route.params.orderId)

const loadOrder = async () => {
  loading.value = true
  try {
    order.value = await getOrderDetail(orderId)
    paid.value = order.value.status === 1 || order.value.status === 2
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '订单加载失败')
  } finally {
    loading.value = false
  }
}

const handlePay = async () => {
  paying.value = true
  try {
    order.value = await payOrder(orderId, order.value?.paymentMethod)
    paid.value = true
    ElMessage.success('支付成功！')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '支付失败')
  } finally {
    paying.value = false
  }
}

const paymentMethodText = (method?: string) => {
  if (method === 'WECHAT') return '微信'
  if (method === 'ALIPAY') return '支付宝'
  if (method === 'POINTS') return '积分'
  return '未选择'
}

const goBack = () => router.back()
const goOrders = () => router.push('/hotels/orders')
const goSearch = () => router.push('/hotels/search')

onMounted(loadOrder)
</script>

<style scoped>
.hotel-payment-page {
  min-height: 100vh;
  padding: 96px 16px 48px;
  background: var(--el-bg-color-page);
}

.payment-card {
  max-width: 760px;
  margin: 0 auto;
  padding: 28px;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
}

.back-button {
  margin-bottom: 18px;
}

.page-title span {
  color: var(--el-color-primary);
  font-weight: 700;
  font-size: 13px;
}

.page-title h1 {
  margin: 8px 0 20px;
}

.pay-actions {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
  margin-top: 22px;
  flex-wrap: wrap;
}

.pay-method {
  color: var(--el-text-color-secondary);
}
</style>
