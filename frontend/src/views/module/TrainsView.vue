<template>
  <div class="module-page">
    <el-dialog v-model="showChangeCodeDialog" title="改签" width="420px">
      <el-form label-width="80px">
        <el-form-item label="取票码">
          <el-input v-model="changePickupCode" maxlength="6" placeholder="请输入6位取票码" @input="normalizeChangeCode" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showChangeCodeDialog = false">取消</el-button>
        <el-button type="primary" :loading="changePreviewLoading" @click="handlePreviewChange">确认</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showChangeListDialog" title="选择改签车次" width="820px">
      <el-alert
        v-if="changePreview"
        class="change-alert"
        :closable="false"
        type="info"
        :title="`${changePreview.startStation} - ${changePreview.endStation}，原车次 ${changePreview.trainName}，座位 ${changePreview.seatType}`"
      />
      <el-table v-if="changePreview?.candidates.length" :data="changePreview.candidates" stripe>
        <el-table-column prop="name" label="车次" min-width="100" />
        <el-table-column label="日期" min-width="110">
          <template #default="{ row }">{{ row.extra?.date }}</template>
        </el-table-column>
        <el-table-column label="时间" min-width="140">
          <template #default="{ row }">{{ row.extra?.depart_time }} - {{ row.extra?.arrive_time }}</template>
        </el-table-column>
        <el-table-column label="价格" min-width="90">
          <template #default="{ row }">￥{{ row.price }}</template>
        </el-table-column>
        <el-table-column label="余票" min-width="80">
          <template #default="{ row }">{{ availableTickets(row) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" :loading="changeSubmitLoading" @click="handleChangeTrain(row)">选择</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else description="暂无可改签车次" />
    </el-dialog>

    <el-dialog v-model="showChangeResultDialog" title="改签成功" width="480px">
      <el-result icon="success" title="改签成功" :sub-title="changeResult?.message" />
      <el-descriptions v-if="changeResult" :column="1" border>
        <el-descriptions-item label="原订单号">{{ changeResult.oldOrderNo }}</el-descriptions-item>
        <el-descriptions-item label="新订单号">{{ changeResult.newOrderNo }}</el-descriptions-item>
        <el-descriptions-item label="新取票码">{{ changeResult.pickupCode }}</el-descriptions-item>
        <el-descriptions-item label="新票金额">￥{{ changeResult.newPayAmount }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button type="primary" @click="showChangeResultDialog = false">确定</el-button>
      </template>
    </el-dialog>

    <section class="module-hero">
      <div class="container hero-inner">
        <span class="module-icon">火车票</span>
        <h1 class="section-title">火车票</h1>
        <p class="section-subtitle">按站点、日期、车次类型和座位类型筛选，完成下单与模拟支付。</p>
        <div class="hero-actions">
          <el-button type="primary" size="large" @click="openSearchDialog">购票</el-button>
          <el-button size="large" @click="openRefundDialog">退票</el-button>
          <el-button size="large" @click="openChangeDialog">改签</el-button>
        </div>
      </div>
    </section>

    <section class="module-content container">
      <el-table v-if="trainStore.trainsList.length" :data="trainStore.trainsList" stripe class="train-table">
        <el-table-column prop="name" label="车次" min-width="110" />
        <el-table-column label="出发/到达" min-width="180">
          <template #default="{ row }">
            {{ row.extra?.start_station }} - {{ row.extra?.end_station }}
          </template>
        </el-table-column>
        <el-table-column label="日期" min-width="120">
          <template #default="{ row }">{{ row.extra?.date }}</template>
        </el-table-column>
        <el-table-column label="类型/座位" min-width="260">
          <template #default="{ row }">
            <el-tag v-for="tag in row.categoryTags" :key="tag" class="tag" size="small">{{ tag }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="价格" min-width="100">
          <template #default="{ row }">￥{{ row.price }}</template>
        </el-table-column>
        <el-table-column label="余票" min-width="80">
          <template #default="{ row }">{{ availableTickets(row) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" :disabled="availableTickets(row) <= 0" @click="openOrderDialog(row)">选择</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else-if="hasSearched" description="未找到符合条件的车次" />
      <el-empty v-else description="点击购票开始查询车次" />
    </section>

    <el-dialog v-model="showSearchDialog" title="车票筛选" width="640px">
      <el-form :model="searchForm" label-width="96px">
        <el-form-item label="出发站">
          <el-select v-model="searchForm.startStation" clearable filterable placeholder="请选择出发站">
            <el-option v-for="station in options.startStations" :key="station" :label="station" :value="station" />
          </el-select>
        </el-form-item>
        <el-form-item label="到达站">
          <el-select v-model="searchForm.endStation" clearable filterable placeholder="请选择到达站">
            <el-option v-for="station in options.endStations" :key="station" :label="station" :value="station" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker v-model="searchForm.date" type="date" value-format="YYYY-MM-DD" placeholder="请选择日期" />
        </el-form-item>
        <el-form-item label="列车类型">
          <el-checkbox-group v-model="searchForm.trainTypes">
            <el-checkbox v-for="item in trainTypeOptions" :key="item" :label="item" />
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="座位类型">
          <el-checkbox-group v-model="searchForm.seatTypes">
            <el-checkbox v-for="item in seatTypeOptions" :key="item" :label="item" />
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showSearchDialog = false">取消</el-button>
        <el-button type="primary" :loading="searchLoading" @click="handleSearch">查询</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showRefundDialog" title="退票" width="420px">
      <el-form label-width="80px">
        <el-form-item label="取票码">
          <el-input v-model="refundPickupCode" maxlength="6" placeholder="请输入6位取票码" @input="normalizeRefundCode" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRefundDialog = false">取消</el-button>
        <el-button type="warning" :loading="refundCodeLoading" @click="handleRefundByPickupCode">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showOrderDialog" title="填写购票人信息" width="520px">
      <el-form ref="orderFormRef" :model="orderForm" :rules="orderRules" label-width="90px">
        <el-form-item label="姓名" prop="passengerName">
          <el-input v-model="orderForm.passengerName" maxlength="20" placeholder="2-20个汉字" />
        </el-form-item>
        <el-form-item label="手机号" prop="passengerPhone">
          <el-input v-model="orderForm.passengerPhone" maxlength="11" placeholder="中国大陆手机号" />
        </el-form-item>
        <el-form-item label="年龄" prop="passengerAge">
          <el-input-number v-model="orderForm.passengerAge" :min="1" :max="120" controls-position="right" />
        </el-form-item>
        <el-form-item label="学生票">
          <el-checkbox v-model="orderForm.isStudent">我是学生</el-checkbox>
        </el-form-item>
        <el-form-item label="座位类型" prop="seatType">
          <el-select v-model="orderForm.seatType" placeholder="请选择座位类型">
            <el-option v-for="seat in currentSeatOptions" :key="seat" :label="seat" :value="seat" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showOrderDialog = false">取消</el-button>
        <el-button type="primary" :loading="orderLoading" @click="handleSubmitOrder">提交订单</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showPayDialog" title="订单支付" width="520px" :close-on-click-modal="false">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="订单号">{{ trainStore.currentOrder?.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="车次">{{ trainStore.selectedTrain?.name }}</el-descriptions-item>
        <el-descriptions-item label="金额">￥{{ trainStore.currentOrder?.payAmount }}</el-descriptions-item>
        <el-descriptions-item label="剩余支付时间">
          <el-text type="danger" size="large">{{ countdownText }}</el-text>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button :loading="cancelLoading" @click="handleCancelOrder">取消订单</el-button>
        <el-button type="primary" :loading="payLoading" @click="handlePayOrder">支付</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showTicketDialog" title="支付成功" width="420px">
      <div class="ticket-code">
        <el-result icon="success" title="订票成功" sub-title="请妥善保存取票码" />
        <div class="code-box">{{ ticketCode }}</div>
        <div class="ticket-actions">
          <el-button @click="showTicketDialog = false">关闭</el-button>
          <el-button type="warning" :loading="refundLoading" @click="handleRefundOrder">退票</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onUnmounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  changeTrainOrder,
  cancelOrder,
  createTrainOrder,
  getOrder,
  getTrainOptions,
  payOrder,
  previewTrainChange,
  refundTrainOrder,
  refundTrainOrderByPickupCode,
  searchTrains
} from '@/api/train'
import type { TrainChangePreviewResponse, TrainChangeResponse, TrainOptions, TrainProduct } from '@/api/train'
import { useTrainStore } from '@/stores/train'

const trainStore = useTrainStore()
const trainTypeOptions = ['高铁', '动车', '普速']
const seatTypeOptions = ['商务座', '一等座', '二等座', '硬卧', '软卧', '硬座']

const showSearchDialog = ref(false)
const showOrderDialog = ref(false)
const showPayDialog = ref(false)
const showTicketDialog = ref(false)
const showRefundDialog = ref(false)
const showChangeCodeDialog = ref(false)
const showChangeListDialog = ref(false)
const showChangeResultDialog = ref(false)
const searchLoading = ref(false)
const orderLoading = ref(false)
const payLoading = ref(false)
const cancelLoading = ref(false)
const refundLoading = ref(false)
const refundCodeLoading = ref(false)
const changePreviewLoading = ref(false)
const changeSubmitLoading = ref(false)
const hasSearched = ref(false)
const ticketCode = ref('')
const refundPickupCode = ref('')
const changePickupCode = ref('')
const changePreview = ref<TrainChangePreviewResponse | null>(null)
const changeResult = ref<TrainChangeResponse | null>(null)
const countdownSeconds = ref(0)
const timer = ref<number>()
const pollTimer = ref<number>()

const options = reactive<TrainOptions>({
  startStations: [],
  endStations: [],
  dates: []
})

const searchForm = reactive({
  startStation: '',
  endStation: '',
  date: '',
  trainTypes: [] as string[],
  seatTypes: [] as string[]
})

const orderFormRef = ref<FormInstance>()
const orderForm = reactive({
  passengerName: '',
  passengerPhone: '',
  passengerAge: 25,
  seatType: '',
  isStudent: false
})

const orderRules: FormRules = {
  passengerName: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { pattern: /^[\u4e00-\u9fa5]{2,20}$/, message: '姓名必须为2-20个汉字', trigger: 'blur' }
  ],
  passengerPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的中国大陆手机号', trigger: 'blur' }
  ],
  passengerAge: [{ required: true, type: 'number', message: '年龄必须为1-120之间的正整数', trigger: 'change' }],
  seatType: [{ required: true, message: '请选择座位类型', trigger: 'change' }]
}

const currentSeatOptions = computed(() => {
  const tags = trainStore.selectedTrain?.categoryTags || []
  return tags.filter((tag) => seatTypeOptions.includes(tag))
})

const errorMessage = (error: unknown, fallback: string) => {
  return error instanceof Error ? error.message : fallback
}

const availableTickets = (train: TrainProduct) => {
  return Math.max(0, Number(train.stock || 0) - Number(train.soldCount || 0))
}

const countdownText = computed(() => {
  const minutes = Math.floor(countdownSeconds.value / 60).toString().padStart(2, '0')
  const seconds = (countdownSeconds.value % 60).toString().padStart(2, '0')
  return `${minutes}:${seconds}`
})

const openSearchDialog = async () => {
  showSearchDialog.value = true
  try {
    const data = await getTrainOptions()
    options.startStations = data.startStations || []
    options.endStations = data.endStations || []
    options.dates = data.dates || []
  } catch {
    ElMessage.warning('筛选项加载失败，请稍后重试')
  }
}

const openRefundDialog = () => {
  refundPickupCode.value = ''
  showRefundDialog.value = true
}

const normalizeRefundCode = () => {
  refundPickupCode.value = refundPickupCode.value.toUpperCase().replace(/[^A-Z0-9]/g, '')
}

const openChangeDialog = () => {
  changePickupCode.value = ''
  changePreview.value = null
  changeResult.value = null
  showChangeCodeDialog.value = true
}

const normalizeChangeCode = () => {
  changePickupCode.value = changePickupCode.value.toUpperCase().replace(/[^A-Z0-9]/g, '')
}

const handlePreviewChange = async () => {
  normalizeChangeCode()
  if (!/^[A-Z0-9]{6}$/.test(changePickupCode.value)) {
    ElMessage.warning('请输入6位取票码')
    return
  }
  changePreviewLoading.value = true
  try {
    const result = await previewTrainChange(changePickupCode.value)
    changePreview.value = result
    showChangeCodeDialog.value = false
    showChangeListDialog.value = true
  } catch (error: unknown) {
    ElMessage.error(errorMessage(error, '改签查询失败，请稍后重试'))
  } finally {
    changePreviewLoading.value = false
  }
}

const handleChangeTrain = async (train: TrainProduct) => {
  try {
    await ElMessageBox.confirm('每张车票只能改签一次，确认后原订单和原取票码将不可用。是否继续？', '改签确认', {
      confirmButtonText: '确认改签',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }

  changeSubmitLoading.value = true
  try {
    const result = await changeTrainOrder(changePickupCode.value, train.id)
    changeResult.value = result
    const message =
      result.differenceType === 'PAY'
        ? `需补差价：￥${result.differenceAmount}`
        : result.differenceType === 'REFUND'
          ? `已退差价：￥${result.differenceAmount}`
          : '无需补差价'
    await ElMessageBox.alert(`${message}。新订单号：${result.newOrderNo}，新取票码：${result.pickupCode}`, '改签订单', {
      confirmButtonText: '确定',
      type: 'success'
    })
    showChangeListDialog.value = false
    showChangeResultDialog.value = true
    await handleSearch()
  } catch (error: unknown) {
    ElMessage.error(errorMessage(error, '改签失败，请稍后重试'))
  } finally {
    changeSubmitLoading.value = false
  }
}

const handleSearch = async () => {
  searchLoading.value = true
  try {
    trainStore.setSearchParams({ ...searchForm })
    const trains = await searchTrains(trainStore.searchParams)
    trainStore.setTrainsList(trains)
    hasSearched.value = true
    showSearchDialog.value = false
  } catch (error: unknown) {
    ElMessage.error(errorMessage(error, '查询失败，请稍后重试'))
  } finally {
    searchLoading.value = false
  }
}

const openOrderDialog = (train: TrainProduct) => {
  trainStore.setSelectedTrain(train)
  orderForm.passengerName = ''
  orderForm.passengerPhone = ''
  orderForm.passengerAge = 25
  orderForm.seatType = currentSeatOptions.value[0] || ''
  orderForm.isStudent = false
  orderFormRef.value?.clearValidate()
  showOrderDialog.value = true
}

const handleSubmitOrder = async () => {
  const valid = await orderFormRef.value?.validate().catch(() => false)
  if (!valid || !trainStore.selectedTrain) return

  orderLoading.value = true
  try {
    const order = await createTrainOrder({
      productId: trainStore.selectedTrain.id,
      ...orderForm
    })
    trainStore.setCurrentOrder(order)
    ElMessage.success('下单成功，请在10分钟内支付')
    showOrderDialog.value = false
    showPayDialog.value = true
    await handleSearch()
    startCountdown(order.expireTime)
  } catch (error: unknown) {
    ElMessage.error(errorMessage(error, '下单失败，请稍后重试'))
  } finally {
    orderLoading.value = false
  }
}

const startCountdown = (expireTime: string) => {
  clearTimers()
  updateCountdown(expireTime)
  timer.value = window.setInterval(() => {
    updateCountdown(expireTime)
    if (countdownSeconds.value <= 0) {
      handleTimeout()
    }
  }, 1000)
  pollTimer.value = window.setInterval(pollOrderStatus, 5000)
}

const updateCountdown = (expireTime: string) => {
  countdownSeconds.value = Math.max(0, Math.floor((new Date(expireTime).getTime() - Date.now()) / 1000))
}

const pollOrderStatus = async () => {
  const orderNo = trainStore.currentOrder?.orderNo
  if (!orderNo) return
  try {
    const order = await getOrder(orderNo)
    if (order.status === 2) {
      showPayDialog.value = false
      clearTimers()
      ElMessage.warning('订单已取消，请重新选择车次')
      await handleSearch()
    }
  } catch {
    // Polling failures should not interrupt the visible payment flow.
  }
}

const handlePayOrder = async () => {
  const orderNo = trainStore.currentOrder?.orderNo
  if (!orderNo) return
  payLoading.value = true
  try {
    const result = await payOrder(orderNo)
    trainStore.setCurrentOrder(result)
    ticketCode.value = result.pickupCode || ''
    showPayDialog.value = false
    showTicketDialog.value = true
    clearTimers()
  } catch (error: unknown) {
    ElMessage.error(errorMessage(error, '支付失败，请稍后重试'))
    await pollOrderStatus()
  } finally {
    payLoading.value = false
  }
}

const handleCancelOrder = async () => {
  const orderNo = trainStore.currentOrder?.orderNo
  if (!orderNo) return
  cancelLoading.value = true
  try {
    await cancelOrder(orderNo)
    ElMessage.success('订单已取消')
    showPayDialog.value = false
    clearTimers()
    await handleSearch()
  } catch (error: unknown) {
    ElMessage.error(errorMessage(error, '取消失败，请稍后重试'))
  } finally {
    cancelLoading.value = false
  }
}

const handleRefundOrder = async () => {
  const currentOrder = trainStore.currentOrder
  if (!currentOrder?.orderNo) return
  const orderNo = currentOrder.orderNo
  refundLoading.value = true
  try {
    const result = await refundTrainOrder(orderNo)
    ElMessage.success(`${result.refundRule}，退款金额：￥${result.refundAmount}`)
    showTicketDialog.value = false
    trainStore.setCurrentOrder({
      ...currentOrder,
      status: result.status
    })
    await handleSearch()
  } catch (error: unknown) {
    ElMessage.error(errorMessage(error, '退票失败，请稍后重试'))
  } finally {
    refundLoading.value = false
  }
}

const handleRefundByPickupCode = async () => {
  normalizeRefundCode()
  if (!/^[A-Z0-9]{6}$/.test(refundPickupCode.value)) {
    ElMessage.warning('请输入6位取票码')
    return
  }
  refundCodeLoading.value = true
  try {
    const result = await refundTrainOrderByPickupCode(refundPickupCode.value)
    ElMessage.success(`${result.refundRule}，退款金额：￥${result.refundAmount}`)
    showRefundDialog.value = false
    await handleSearch()
  } catch (error: unknown) {
    ElMessage.error(errorMessage(error, '退票失败，请稍后重试'))
  } finally {
    refundCodeLoading.value = false
  }
}

const handleTimeout = async () => {
  clearTimers()
  showPayDialog.value = false
  await ElMessageBox.alert('支付超时，订单已自动取消，请重新选择车次。', '超时提示', {
    confirmButtonText: '确定',
    type: 'warning'
  })
  await handleSearch()
}

const clearTimers = () => {
  if (timer.value) window.clearInterval(timer.value)
  if (pollTimer.value) window.clearInterval(pollTimer.value)
  timer.value = undefined
  pollTimer.value = undefined
}

onUnmounted(clearTimers)
</script>

<style scoped>
.module-page {
  padding-top: 64px;
}

.module-hero {
  padding: 56px 0 36px;
  background: linear-gradient(135deg, #23415f, #4f7a93);
  color: #fff;
  text-align: center;
}

.hero-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14px;
}

.hero-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  flex-wrap: wrap;
}

.module-hero .section-title {
  color: #fff;
  margin: 0;
}

.module-hero .section-subtitle {
  color: rgba(255, 255, 255, 0.82);
  margin: 0;
}

.module-icon {
  font-size: 18px;
  font-weight: 600;
}

.module-content {
  padding: 32px 0 60px;
}

.train-table {
  width: 100%;
}

.tag {
  margin: 2px 6px 2px 0;
}

.ticket-code {
  text-align: center;
}

.code-box {
  display: inline-block;
  min-width: 156px;
  padding: 12px 18px;
  border: 1px solid var(--el-border-color);
  border-radius: 6px;
  background: var(--el-fill-color-light);
  color: var(--el-color-primary);
  font-size: 30px;
  font-weight: 700;
  letter-spacing: 4px;
}

.ticket-actions {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  gap: 12px;
}

.change-alert {
  margin-bottom: 16px;
}
</style>
