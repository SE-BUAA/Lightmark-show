<template>
  <div class="ai-float">
    <el-button v-if="!hideFab" type="primary" circle class="float-button" @click="open">AI</el-button>
    <el-drawer v-model="visible" title="AI 酒店推荐" direction="rtl" size="380px">
      <div class="chat-panel">
        <div class="reply-box">
          {{ recommendText || '告诉我目的地、预算、人数和偏好，我来帮你筛选酒店。' }}
        </div>
        <el-input
          v-model="input"
          type="textarea"
          :rows="4"
          placeholder="例如：北京三里屯附近，两人入住，要早餐，四星以上"
        />
        <el-button type="primary" :loading="loading" @click="send">发送需求</el-button>
        <div class="hotel-list">
          <button v-for="hotel in hotels" :key="hotel.id" class="hotel-link" @click="openHotel(hotel.id)">
            <strong>{{ hotel.name }}</strong>
            <span>￥{{ hotel.priceMin || '--' }} 起 · {{ hotel.rating || '--' }} 分</span>
          </button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { recommendHotel } from '@/api/aiApi'
import type { HotelVO } from '@/types/hotel'

defineProps<{
  hideFab?: boolean
}>()

const router = useRouter()
const visible = ref(false)
const input = ref('')
const loading = ref(false)
const recommendText = ref('')
const hotels = ref<HotelVO[]>([])

const open = () => {
  visible.value = true
}

const send = async () => {
  if (!input.value.trim()) {
    ElMessage.warning('请输入推荐需求')
    return
  }
  loading.value = true
  try {
    const result = await recommendHotel(input.value)
    recommendText.value = result.recommendText
    hotels.value = result.hotels || []
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : 'AI 推荐失败')
  } finally {
    loading.value = false
  }
}

const openHotel = (id: string) => {
  visible.value = false
  router.push(`/hotels/detail/${id}`)
}

defineExpose({ open })
</script>

<style scoped>
.ai-float {
  position: fixed;
  right: 24px;
  bottom: 28px;
  z-index: 20;
}

.float-button {
  width: 56px;
  height: 56px;
  box-shadow: var(--el-box-shadow);
}

.chat-panel {
  display: grid;
  gap: 14px;
}

.reply-box {
  padding: 12px;
  min-height: 72px;
  border-radius: 8px;
  background: var(--el-fill-color-light);
  color: var(--el-text-color-regular);
  line-height: 1.6;
}

.hotel-list {
  display: grid;
  gap: 10px;
}

.hotel-link {
  display: grid;
  gap: 4px;
  width: 100%;
  padding: 12px;
  border: 1px solid var(--el-border-color);
  border-radius: 8px;
  background: var(--el-bg-color);
  text-align: left;
  cursor: pointer;
}

.hotel-link span {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
</style>
