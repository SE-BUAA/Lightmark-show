<template>
  <main class="hotel-orders-page">
    <section class="orders-shell">
      <el-button class="back-button" @click="goBack">返回上一步</el-button>
      <div class="page-title">
        <span>Hotel Orders</span>
        <h1>酒店订单</h1>
      </div>

      <el-table v-loading="loading" :data="orders" stripe>
        <el-table-column prop="orderNo" label="订单号" min-width="170" />
        <el-table-column prop="hotelName" label="酒店" min-width="160" />
        <el-table-column prop="roomName" label="房型" min-width="130" />
        <el-table-column label="入住日期" min-width="190">
          <template #default="{ row }">{{ row.checkInDate }} 至 {{ row.checkOutDate }}</template>
        </el-table-column>
        <el-table-column label="金额" width="110">
          <template #default="{ row }">￥{{ Number(row.totalAmount || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openCancel(row.orderId)">取消/退款</el-button>
            <el-button size="small" type="primary" :disabled="row.status !== 2" @click="openReview(row.orderId)">评价</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="cancelVisible" title="取消订单" width="520px">
      <el-skeleton v-if="detailLoading" :rows="5" animated />
      <template v-else-if="currentOrder">
        <el-alert class="dialog-alert" type="warning" :closable="false" :title="refundPreview" />
        <el-descriptions :column="1" border>
          <el-descriptions-item label="酒店">{{ currentOrder.hotelName }}</el-descriptions-item>
          <el-descriptions-item label="房型">{{ currentOrder.roomName }}</el-descriptions-item>
          <el-descriptions-item label="取消政策">{{ policyText(currentOrder.cancelPolicy) }}</el-descriptions-item>
          <el-descriptions-item label="实付金额">￥{{ Number(currentOrder.payAmount || 0).toFixed(2) }}</el-descriptions-item>
        </el-descriptions>
      </template>
      <template #footer>
        <el-button @click="cancelVisible = false">关闭</el-button>
        <el-button type="danger" :loading="canceling" @click="confirmCancel">确认取消</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="reviewVisible" title="写酒店评价" width="520px">
      <el-form label-position="top">
        <el-form-item label="评分">
          <el-rate v-model="reviewForm.rating" :max="5" />
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input v-model="reviewForm.content" type="textarea" :rows="5" placeholder="说说这次入住体验，例如卫生、位置、服务、噪音等" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewVisible = false">取消</el-button>
        <el-button type="primary" :loading="reviewing" @click="submitReview">提交评价</el-button>
      </template>
    </el-dialog>
  </main>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cancelOrder, createHotelReview, getOrderDetail, getOrders } from '@/api/hotelApi'
import type { HotelOrderDetailVO, HotelOrderListVO } from '@/types/hotel'

const router = useRouter()
const orders = ref<HotelOrderListVO[]>([])
const loading = ref(false)
const detailLoading = ref(false)
const canceling = ref(false)
const reviewing = ref(false)
const cancelVisible = ref(false)
const reviewVisible = ref(false)
const currentOrder = ref<HotelOrderDetailVO>()
const reviewOrderId = ref<number>()
const reviewForm = reactive({ rating: 5, content: '' })

const loadOrders = async () => {
  loading.value = true
  try {
    const result = await getOrders({ page: 1, size: 50 })
    orders.value = result.records
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '订单加载失败')
  } finally {
    loading.value = false
  }
}

const openCancel = async (orderId: number) => {
  cancelVisible.value = true
  detailLoading.value = true
  try {
    currentOrder.value = await getOrderDetail(orderId)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '订单详情加载失败')
  } finally {
    detailLoading.value = false
  }
}

const openReview = (orderId: number) => {
  reviewOrderId.value = orderId
  reviewForm.rating = 5
  reviewForm.content = ''
  reviewVisible.value = true
}

const submitReview = async () => {
  if (!reviewOrderId.value) return
  if (!reviewForm.content.trim()) {
    ElMessage.warning('请填写评价内容')
    return
  }
  reviewing.value = true
  try {
    await createHotelReview(reviewOrderId.value, { rating: reviewForm.rating, content: reviewForm.content.trim() })
    ElMessage.success('评价已提交，其他用户可以在酒店详情页看到')
    reviewVisible.value = false
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '评价提交失败')
  } finally {
    reviewing.value = false
  }
}

const refundPreview = computed(() => {
  const order = currentOrder.value
  if (!order) return ''
  if (order.status === 0) return '待支付订单可直接取消，不收取手续费。'
  if (order.status !== 1) return '当前订单状态不可取消。'
  const days = Math.ceil((new Date(order.checkInDate).getTime() - Date.now()) / 86400000)
  const pay = Number(order.payAmount || 0)
  let feeRate = 1
  if (days > 7) feeRate = 0
  else if (days >= 3) feeRate = 0.3
  else if (days >= 1) feeRate = 0.5
  const fee = pay * feeRate
  const refund = Math.max(0, pay - fee)
  return `距离入住约 ${Math.max(0, days)} 天，预计手续费 ￥${fee.toFixed(2)}，预计退款 ￥${refund.toFixed(2)}。`
})

const confirmCancel = async () => {
  const order = currentOrder.value
  if (!order) return
  try {
    await ElMessageBox.confirm(refundPreview.value, '确认取消订单', {
      confirmButtonText: '确认取消',
      cancelButtonText: '再想想',
      type: 'warning'
    })
  } catch {
    return
  }
  canceling.value = true
  try {
    await cancelOrder(order.orderId)
    ElMessage.success('订单已取消，库存和积分已按规则恢复')
    cancelVisible.value = false
    await loadOrders()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '取消失败')
  } finally {
    canceling.value = false
  }
}

const policyText = (policy?: string) => {
  if (policy === 'FREE_CANCEL') return '免费取消'
  if (policy === 'LIMITED_CANCEL') return '限时取消'
  if (policy === 'NON_REFUNDABLE') return '不可取消'
  return policy || '按酒店政策'
}

const statusText = (status: number) => ({ 0: '待支付', 1: '已支付', 2: '已出行', 3: '已取消' }[status] || '未知')
const statusType = (status: number) => (status === 1 ? 'success' : status === 3 ? 'info' : status === 0 ? 'warning' : 'primary')
const goBack = () => router.back()

onMounted(loadOrders)
</script>

<style scoped>
.hotel-orders-page {
  min-height: 100vh;
  padding: 96px 16px 48px;
  background: var(--el-bg-color-page);
}

.orders-shell {
  max-width: 1160px;
  margin: 0 auto;
  padding: 24px;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
}

.back-button {
  margin-bottom: 16px;
}

.page-title span {
  color: var(--el-color-primary);
  font-size: 13px;
  font-weight: 700;
}

.page-title h1 {
  margin: 8px 0 20px;
}

.dialog-alert {
  margin-bottom: 16px;
}
</style>
