<template>
  <div class="module-page">
    <section class="module-hero">
      <div class="container hero-inner">
        <span class="module-icon">度假</span>
        <h1 class="section-title">旅游度假</h1>
        <p class="section-subtitle">按目的地、出发城市、日期、天数和主题筛选度假产品。</p>
        <div class="hero-actions">
          <el-button type="primary" size="large" @click="openSearchDialog">查询度假产品</el-button>
          <el-button size="large" @click="openRefundDialog">退票</el-button>
        </div>
      </div>
    </section>

    <section class="module-content container">
      <el-table v-if="vacationStore.vacationsList.length" :data="vacationStore.vacationsList" stripe class="vacation-table">
        <el-table-column prop="name" label="产品" min-width="180" />
        <el-table-column label="行程" min-width="220">
          <template #default="{ row }">
            {{ row.extra?.depart_city }} 出发 · {{ row.extra?.destination }} · {{ row.extra?.days }}天
          </template>
        </el-table-column>
        <el-table-column label="日期" min-width="120">
          <template #default="{ row }">{{ row.extra?.date }}</template>
        </el-table-column>
        <el-table-column label="标签" min-width="220">
          <template #default="{ row }">
            <el-tag v-for="tag in row.categoryTags" :key="tag" class="tag" size="small">{{ tag }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="价格" min-width="100">
          <template #default="{ row }">￥{{ row.price }}</template>
        </el-table-column>
        <el-table-column label="余量" min-width="80">
          <template #default="{ row }">{{ availableStock(row) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <div class="row-actions">
              <el-button type="primary" :disabled="availableStock(row) <= 0" @click="openConfirmDialog(row)">选择</el-button>
              <el-button :loading="detailLoadingId === row.id" @click="handleShowDetail(row)">详情</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else-if="hasSearched" description="未找到符合条件的度假产品" />
      <el-empty v-else description="点击查询度假产品开始筛选" />
    </section>

    <el-dialog v-model="showSearchDialog" title="度假筛选" width="640px">
      <el-form :model="searchForm" label-width="96px">
        <el-form-item label="目的地">
          <el-select v-model="searchForm.destination" clearable filterable placeholder="请选择目的地">
            <el-option v-for="item in options.destinations" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="出发城市">
          <el-select v-model="searchForm.departCity" clearable filterable placeholder="请选择出发城市">
            <el-option v-for="item in options.departCities" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker v-model="searchForm.date" type="date" value-format="YYYY-MM-DD" placeholder="请选择日期" />
        </el-form-item>
        <el-form-item label="天数">
          <div class="inline-range">
            <el-input-number v-model="searchForm.minDays" :min="1" :max="30" controls-position="right" placeholder="最少" />
            <span>至</span>
            <el-input-number v-model="searchForm.maxDays" :min="1" :max="30" controls-position="right" placeholder="最多" />
          </div>
        </el-form-item>
        <el-form-item label="预算">
          <div class="inline-range">
            <el-input-number v-model="searchForm.minPrice" :min="0" :step="100" controls-position="right" placeholder="最低" />
            <span>至</span>
            <el-input-number v-model="searchForm.maxPrice" :min="0" :step="100" controls-position="right" placeholder="最高" />
          </div>
        </el-form-item>
        <el-form-item label="主题">
          <el-select v-model="searchForm.tags" multiple clearable filterable collapse-tags placeholder="请选择主题">
            <el-option v-for="tag in options.tags" :key="tag" :label="tag" :value="tag" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showSearchDialog = false">取消</el-button>
        <el-button type="primary" :loading="searchLoading" @click="handleSearch">查询</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showRefundDialog" title="度假退票" width="420px">
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

    <el-dialog v-model="showDetailDialog" title="度假产品详情" width="560px">
      <el-skeleton v-if="detailLoading" :rows="4" animated />
      <div v-else class="ai-detail">
        <h3>{{ detailProductName }}</h3>
        <p>{{ detailContent }}</p>
      </div>
      <template #footer>
        <el-button type="primary" @click="showDetailDialog = false">知道了</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showConfirmDialog" title="确认度假订单" width="560px">
      <el-descriptions v-if="vacationStore.selectedVacation" :column="1" border class="confirm-desc">
        <el-descriptions-item label="产品">{{ vacationStore.selectedVacation.name }}</el-descriptions-item>
        <el-descriptions-item label="行程">
          {{ vacationStore.selectedVacation.extra?.depart_city }} - {{ vacationStore.selectedVacation.extra?.destination }}
        </el-descriptions-item>
        <el-descriptions-item label="日期">{{ vacationStore.selectedVacation.extra?.date }}</el-descriptions-item>
        <el-descriptions-item label="单价">￥{{ vacationStore.selectedVacation.price }}</el-descriptions-item>
      </el-descriptions>
      <el-form ref="orderFormRef" :model="orderForm" :rules="orderRules" label-width="96px">
        <el-form-item label="出行人" prop="travelerName">
          <el-input v-model="orderForm.travelerName" maxlength="30" placeholder="请输入出行人姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="travelerPhone">
          <el-input v-model="orderForm.travelerPhone" maxlength="11" placeholder="中国大陆手机号" />
        </el-form-item>
        <el-form-item label="人数" prop="travelerCount">
          <el-input-number v-model="orderForm.travelerCount" :min="1" :max="20" controls-position="right" />
        </el-form-item>
        <el-form-item label="取消险">
          <el-checkbox v-model="orderForm.cancellationInsurance">购买取消险（订单基础价的5%）</el-checkbox>
        </el-form-item>
        <el-alert :closable="false" type="info" :title="`应付金额：￥${payPreview}`" />
      </el-form>
      <template #footer>
        <el-button @click="showConfirmDialog = false">取消</el-button>
        <el-button type="primary" :loading="orderLoading" @click="handleSubmitOrder">确认下单</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showPayDialog" title="订单支付" width="520px" :close-on-click-modal="false">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="订单号">{{ vacationStore.currentOrder?.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="产品">{{ vacationStore.selectedVacation?.name }}</el-descriptions-item>
        <el-descriptions-item label="金额">￥{{ vacationStore.currentOrder?.payAmount }}</el-descriptions-item>
        <el-descriptions-item label="剩余支付时间">
          <el-text type="danger" size="large">{{ countdownText }}</el-text>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button :loading="cancelLoading" @click="handleCancelOrder">取消订单</el-button>
        <el-button type="primary" :loading="payLoading" @click="handlePayOrder">支付</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showSuccessDialog" title="预订成功" width="460px">
      <el-result icon="success" title="支付成功" sub-title="度假订单已确认" />
      <div class="ticket-code">
        <div class="code-label">取票码</div>
        <div class="code-box">{{ vacationPickupCode }}</div>
      </div>
      <div class="success-actions">
        <el-button @click="showSuccessDialog = false">关闭</el-button>
        <el-button type="warning" :loading="refundLoading" @click="handleRefundOrder">退订</el-button>
      </div>
    </el-dialog>

    <el-dialog v-model="showAssistantDialog" title="智能行程助手" width="560px">
      <el-skeleton v-if="assistantLoading" :rows="5" animated />
      <div v-else class="ai-detail">
        <h3>{{ assistantTitle }}</h3>
        <p>{{ assistantContent }}</p>
      </div>
      <template #footer>
        <el-button type="primary" @click="showAssistantDialog = false">知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onUnmounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  cancelVacationOrder,
  createVacationOrder,
  generateVacationAssistant,
  generateVacationDetail,
  getVacationOptions,
  getVacationOrder,
  payVacationOrder,
  refundVacationOrder,
  refundVacationOrderByPickupCode,
  searchVacations
} from '@/api/vacation'
import type { VacationOptions, VacationProduct } from '@/api/vacation'
import { useVacationStore } from '@/stores/vacation'

const vacationStore = useVacationStore()
const showSearchDialog = ref(false)
const showConfirmDialog = ref(false)
const showPayDialog = ref(false)
const showSuccessDialog = ref(false)
const showRefundDialog = ref(false)
const showDetailDialog = ref(false)
const showAssistantDialog = ref(false)
const searchLoading = ref(false)
const orderLoading = ref(false)
const payLoading = ref(false)
const cancelLoading = ref(false)
const refundLoading = ref(false)
const refundCodeLoading = ref(false)
const detailLoading = ref(false)
const assistantLoading = ref(false)
const detailLoadingId = ref('')
const hasSearched = ref(false)
const refundPickupCode = ref('')
const vacationPickupCode = ref('')
const detailProductName = ref('')
const detailContent = ref('')
const assistantTitle = ref('')
const assistantContent = ref('')
const countdownSeconds = ref(0)
const timer = ref<number>()
const pollTimer = ref<number>()

const options = reactive<VacationOptions>({
  destinations: [],
  departCities: [],
  dates: [],
  tags: []
})

const searchForm = reactive({
  destination: '',
  departCity: '',
  date: '',
  minDays: undefined as number | undefined,
  maxDays: undefined as number | undefined,
  minPrice: undefined as number | undefined,
  maxPrice: undefined as number | undefined,
  tags: [] as string[]
})

const orderFormRef = ref<FormInstance>()
const orderForm = reactive({
  travelerName: '',
  travelerPhone: '',
  travelerCount: 1,
  cancellationInsurance: false
})

const orderRules: FormRules = {
  travelerName: [{ required: true, message: '请输入出行人姓名', trigger: 'blur' }],
  travelerPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的中国大陆手机号', trigger: 'blur' }
  ],
  travelerCount: [{ required: true, type: 'number', message: '请选择出行人数', trigger: 'change' }]
}

const availableStock = (item: VacationProduct) => Math.max(0, Number(item.stock || 0) - Number(item.soldCount || 0))
const errorMessage = (error: unknown, fallback: string) => (error instanceof Error ? error.message : fallback)

const payPreview = computed(() => {
  const product = vacationStore.selectedVacation
  if (!product) return '0.00'
  const base = Number(product.price || 0) * orderForm.travelerCount
  const insurance = orderForm.cancellationInsurance ? base * 0.05 : 0
  return (base + insurance).toFixed(2)
})

const countdownText = computed(() => {
  const minutes = Math.floor(countdownSeconds.value / 60).toString().padStart(2, '0')
  const seconds = (countdownSeconds.value % 60).toString().padStart(2, '0')
  return `${minutes}:${seconds}`
})

const openSearchDialog = async () => {
  showSearchDialog.value = true
  try {
    const data = await getVacationOptions()
    Object.assign(options, data)
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

const handleSearch = async () => {
  searchLoading.value = true
  try {
    vacationStore.setSearchParams({ ...searchForm })
    const list = await searchVacations(vacationStore.searchParams)
    vacationStore.setVacationsList(list)
    hasSearched.value = true
    showSearchDialog.value = false
  } catch (error: unknown) {
    ElMessage.error(errorMessage(error, '查询失败，请稍后重试'))
  } finally {
    searchLoading.value = false
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
    const result = await refundVacationOrderByPickupCode(refundPickupCode.value)
    ElMessage.success(`${result.refundRule}，退款金额：￥${result.refundAmount}`)
    showRefundDialog.value = false
    await handleSearch()
  } catch (error: unknown) {
    ElMessage.error(errorMessage(error, '退票失败，请稍后重试'))
  } finally {
    refundCodeLoading.value = false
  }
}

const handleShowDetail = async (product: VacationProduct) => {
  detailProductName.value = product.name
  detailContent.value = ''
  showDetailDialog.value = true
  detailLoading.value = true
  detailLoadingId.value = product.id
  try {
    const result = await generateVacationDetail(product.id)
    detailContent.value = result.content || '暂未生成详情文案，请稍后再试。'
  } catch (error: unknown) {
    detailContent.value = errorMessage(error, '详情生成失败，请稍后重试')
  } finally {
    detailLoading.value = false
    detailLoadingId.value = ''
  }
}

const openConfirmDialog = (product: VacationProduct) => {
  vacationStore.setSelectedVacation(product)
  orderForm.travelerName = ''
  orderForm.travelerPhone = ''
  orderForm.travelerCount = 1
  orderForm.cancellationInsurance = false
  orderFormRef.value?.clearValidate()
  showConfirmDialog.value = true
}

const handleSubmitOrder = async () => {
  const valid = await orderFormRef.value?.validate().catch(() => false)
  if (!valid || !vacationStore.selectedVacation) return
  orderLoading.value = true
  try {
    const order = await createVacationOrder({
      productId: vacationStore.selectedVacation.id,
      ...orderForm
    })
    vacationStore.setCurrentOrder(order)
    ElMessage.success('下单成功，请在10分钟内支付')
    showConfirmDialog.value = false
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
    if (countdownSeconds.value <= 0) handleTimeout()
  }, 1000)
  pollTimer.value = window.setInterval(pollOrderStatus, 5000)
}

const updateCountdown = (expireTime: string) => {
  countdownSeconds.value = Math.max(0, Math.floor((new Date(expireTime).getTime() - Date.now()) / 1000))
}

const pollOrderStatus = async () => {
  const orderNo = vacationStore.currentOrder?.orderNo
  if (!orderNo) return
  try {
    const order = await getVacationOrder(orderNo)
    if (order.status === 2) {
      showPayDialog.value = false
      clearTimers()
      ElMessage.warning('订单已取消，请重新选择产品')
      await handleSearch()
    }
  } catch {
    // Keep polling quiet while the user is on the payment dialog.
  }
}

const handlePayOrder = async () => {
  const orderNo = vacationStore.currentOrder?.orderNo
  if (!orderNo) return
  payLoading.value = true
  try {
    const result = await payVacationOrder(orderNo)
    vacationStore.setCurrentOrder(result)
    vacationPickupCode.value = result.pickupCode || ''
    showPayDialog.value = false
    showSuccessDialog.value = true
    clearTimers()
    askForAssistant(result.orderNo)
  } catch (error: unknown) {
    ElMessage.error(errorMessage(error, '支付失败，请稍后重试'))
    await pollOrderStatus()
  } finally {
    payLoading.value = false
  }
}

const askForAssistant = (orderNo: string) => {
  ElMessageBox.confirm('是否需要智能行程助手建议？', '智能行程助手', {
    confirmButtonText: '需要',
    cancelButtonText: '暂不需要',
    type: 'info'
  })
    .then(() => loadAssistant(orderNo))
    .catch(() => undefined)
}

const loadAssistant = async (orderNo: string) => {
  assistantTitle.value = '正在准备你的出行建议'
  assistantContent.value = ''
  assistantLoading.value = true
  showAssistantDialog.value = true
  try {
    const result = await generateVacationAssistant(orderNo)
    assistantTitle.value = `${result.destination || '目的地'} · ${result.date || '出行建议'}`
    assistantContent.value = result.content || '暂未生成建议，请稍后再试。'
  } catch (error: unknown) {
    assistantTitle.value = '智能行程助手'
    assistantContent.value = errorMessage(error, '行程建议生成失败，请稍后重试')
  } finally {
    assistantLoading.value = false
  }
}

const handleCancelOrder = async () => {
  const orderNo = vacationStore.currentOrder?.orderNo
  if (!orderNo) return
  cancelLoading.value = true
  try {
    await cancelVacationOrder(orderNo)
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
  const orderNo = vacationStore.currentOrder?.orderNo
  if (!orderNo) return
  refundLoading.value = true
  try {
    const result = await refundVacationOrder(orderNo)
    ElMessage.success(`${result.refundRule}，退款金额：￥${result.refundAmount}`)
    showSuccessDialog.value = false
    await handleSearch()
  } catch (error: unknown) {
    ElMessage.error(errorMessage(error, '退订失败，请稍后重试'))
  } finally {
    refundLoading.value = false
  }
}

const handleTimeout = async () => {
  clearTimers()
  showPayDialog.value = false
  await ElMessageBox.alert('支付超时，订单已自动取消，请重新选择度假产品。', '超时提示', {
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
  background: linear-gradient(135deg, #2f6f68, #d07b54);
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

.module-hero .section-title,
.module-hero .section-subtitle {
  color: #fff;
  margin: 0;
}

.module-icon {
  font-size: 18px;
  font-weight: 600;
}

.module-content {
  padding: 32px 0 60px;
}

.vacation-table {
  width: 100%;
}

.row-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
}

.row-actions :deep(.el-button) {
  margin-left: 0;
}

.tag {
  margin: 2px 6px 2px 0;
}

.inline-range {
  display: flex;
  align-items: center;
  gap: 10px;
}

.confirm-desc {
  margin-bottom: 18px;
}

.ticket-code {
  margin: -8px 0 20px;
  text-align: center;
}

.code-label {
  color: var(--el-text-color-secondary);
  font-size: 13px;
  margin-bottom: 8px;
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

.ai-detail h3 {
  margin: 0 0 12px;
  color: var(--el-text-color-primary);
  font-size: 18px;
}

.ai-detail p {
  margin: 0;
  color: var(--el-text-color-regular);
  line-height: 1.8;
  white-space: pre-wrap;
}

.success-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
}
</style>
