<template>
  <el-form ref="formRef" :model="form" label-position="top" class="booking-form">
    <div class="summary-row">
      <div>
        <strong>{{ hotelName }}</strong>
        <span>{{ roomName }}</span>
      </div>
      <div class="price">¥{{ estimatedPay.toFixed(2) }}</div>
    </div>

    <el-form-item label="常用出行人">
      <el-select v-model="selectedTravelerKey" placeholder="选择后自动填入" clearable @change="fillTraveler">
        <el-option
          v-for="traveler in travelers"
          :key="travelerKey(traveler)"
          :label="traveler.name"
          :value="travelerKey(traveler)"
        >
          <span>{{ traveler.name }}</span>
          <span class="traveler-phone">{{ traveler.phone || '' }}</span>
        </el-option>
      </el-select>
    </el-form-item>

    <el-form-item label="入住人姓名">
      <el-input v-model="form.guest.name" placeholder="请输入入住人姓名" />
    </el-form-item>
    <el-form-item label="证件号">
      <el-input v-model="form.guest.idCard" placeholder="请输入证件号" />
    </el-form-item>
    <el-form-item label="手机号">
      <el-input v-model="form.guest.phone" placeholder="请输入手机号" />
    </el-form-item>

    <div class="form-grid">
      <el-form-item label="房间数">
        <el-input-number v-model="form.roomNum" :min="1" :max="5" />
      </el-form-item>
      <el-form-item label="使用积分">
        <el-input-number v-model="form.pointsDeduced" :min="0" :max="maxUsablePoints" :step="100" />
      </el-form-item>
    </div>

    <el-form-item label="支付方式">
      <el-radio-group v-model="form.paymentMethod">
        <el-radio-button label="WECHAT">微信</el-radio-button>
        <el-radio-button label="ALIPAY">支付宝</el-radio-button>
        <el-radio-button label="POINTS">积分</el-radio-button>
      </el-radio-group>
    </el-form-item>

    <div class="amount-box">
      <span>原价 ¥{{ originalAmount.toFixed(2) }}</span>
      <span>积分抵扣 ¥{{ pointsAmount.toFixed(2) }}</span>
      <strong>应付 ¥{{ estimatedPay.toFixed(2) }}</strong>
    </div>

    <el-button type="primary" :loading="submitting" class="submit-button" @click="submitOrder">
      提交订单
    </el-button>
  </el-form>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createOrder } from '@/api/hotelApi'
import { addTraveler, getCurrentUser, getTravelers, type TravelerDTO } from '@/api/user'

const props = defineProps<{
  roomId: number
  hotelName: string
  roomName: string
  pricePerNight: number
  checkInDate: string
  checkOutDate: string
}>()

const emit = defineEmits<{ (event: 'success'): void }>()
const router = useRouter()
const formRef = ref()
const submitting = ref(false)
const travelers = ref<TravelerDTO[]>([])
const selectedTravelerKey = ref<string>()
const userPoints = ref(0)
const storageKey = ref('lightmark.hotel.travelers.guest')

const form = reactive({
  roomNum: 1,
  pointsDeduced: 0,
  paymentMethod: 'WECHAT',
  guest: {
    name: '',
    idCard: '',
    phone: ''
  }
})

const nights = computed(() => {
  const start = new Date(props.checkInDate).getTime()
  const end = new Date(props.checkOutDate).getTime()
  return Math.max(1, Math.ceil((end - start) / 86400000))
})

const originalAmount = computed(() => props.pricePerNight * nights.value * form.roomNum)
const maxUsablePoints = computed(() => Math.min(userPoints.value, Math.floor(originalAmount.value * 100)))
const pointsAmount = computed(() => Math.min(form.pointsDeduced, maxUsablePoints.value) * 0.01)
const estimatedPay = computed(() => Math.max(0, originalAmount.value - pointsAmount.value))

const travelerKey = (traveler: TravelerDTO) => traveler.id || `${traveler.name}-${traveler.id_card}-${traveler.phone || ''}`

const fillTraveler = () => {
  const traveler = travelers.value.find(item => travelerKey(item) === selectedTravelerKey.value)
  if (!traveler) return
  form.guest.name = traveler.name
  form.guest.idCard = traveler.id_card
  form.guest.phone = traveler.phone || ''
}

const submitOrder = async () => {
  if (!form.guest.name || !form.guest.idCard || !form.guest.phone) {
    ElMessage.warning('请完整填写入住人信息')
    return
  }
  submitting.value = true
  try {
    await saveTravelerIfNeeded()
    const order = await createOrder({
      roomId: props.roomId,
      checkInDate: props.checkInDate,
      checkOutDate: props.checkOutDate,
      roomNum: form.roomNum,
      guestList: [form.guest],
      pointsDeduced: form.pointsDeduced,
      paymentMethod: form.paymentMethod
    })
    ElMessage.success('订单创建成功，请完成支付')
    emit('success')
    router.push(`/hotels/payment/${order.orderId}`)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '创建订单失败')
  } finally {
    submitting.value = false
  }
}

const saveTravelerIfNeeded = async () => {
  const guest = {
    name: form.guest.name.trim(),
    id_card: form.guest.idCard.trim(),
    phone: form.guest.phone.trim()
  }
  const exists = travelers.value.some(item => item.name === guest.name && item.id_card === guest.id_card)
  if (exists) return
  try {
    const saved = await addTraveler(guest)
    travelers.value = mergeTravelers([saved, ...travelers.value])
  } catch {
    travelers.value = mergeTravelers([guest, ...travelers.value])
  }
  saveLocalTravelers()
}

const mergeTravelers = (items: TravelerDTO[]) => {
  const map = new Map<string, TravelerDTO>()
  items.forEach(item => {
    if (item.name && item.id_card) {
      map.set(`${item.name}-${item.id_card}`, item)
    }
  })
  return Array.from(map.values())
}

const loadLocalTravelers = () => {
  try {
    return JSON.parse(localStorage.getItem(storageKey.value) || '[]') as TravelerDTO[]
  } catch {
    return []
  }
}

const saveLocalTravelers = () => {
  localStorage.setItem(storageKey.value, JSON.stringify(travelers.value.slice(0, 10)))
}

onMounted(async () => {
  try {
    const [travelerList, user] = await Promise.all([getTravelers(), getCurrentUser()])
    storageKey.value = `lightmark.hotel.travelers.${user.id || 'guest'}`
    userPoints.value = user.points || 0
    travelers.value = mergeTravelers([...(travelerList || []), ...loadLocalTravelers()])
    saveLocalTravelers()
  } catch {
    travelers.value = mergeTravelers(loadLocalTravelers())
  }
})
</script>

<style scoped>
.booking-form {
  display: grid;
  gap: 12px;
}

.summary-row,
.amount-box {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px;
  background: var(--el-fill-color-light);
  border-radius: 8px;
}

.summary-row span {
  display: block;
  margin-top: 4px;
  color: var(--el-text-color-secondary);
}

.price,
.amount-box strong {
  color: var(--el-color-danger);
}

.traveler-phone {
  float: right;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.submit-button {
  width: 100%;
}

@media (max-width: 640px) {
  .summary-row,
  .amount-box,
  .form-grid {
    grid-template-columns: 1fr;
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
