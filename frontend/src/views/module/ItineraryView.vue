<template>
  <div class="module-page">
    <section class="module-hero">
      <div class="container hero-inner">
        <div>
          <span class="module-kicker">智能行程</span>
          <h1 class="section-title">把旅行想法整理成可执行计划</h1>
          <p class="section-subtitle">输入目的地、天数和偏好，生成每日景点、餐饮、交通与提醒，并保存到我的行程。</p>
        </div>
        <el-button type="primary" size="large" :loading="loadingPlans" @click="loadPlans">刷新行程</el-button>
      </div>
    </section>

    <section class="planner-section">
      <div class="container planner-grid">
        <div class="planner-panel">
          <h2>生成行程</h2>
          <el-form label-position="top">
            <el-form-item label="目的地">
              <el-input v-model="form.destination" placeholder="例如：杭州" />
            </el-form-item>
            <div class="form-row">
              <el-form-item label="天数">
                <el-input-number v-model="form.days" :min="1" :max="14" />
              </el-form-item>
              <el-form-item label="出发日期">
                <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" />
              </el-form-item>
            </div>
            <el-form-item label="预算">
              <el-input v-model="form.budget" placeholder="例如：人均 3000 元" />
            </el-form-item>
            <el-form-item label="偏好">
              <el-input v-model="form.preferences" type="textarea" :rows="4" placeholder="亲子、慢节奏、美食、博物馆、少走路..." />
            </el-form-item>
            <div class="actions">
              <el-button type="primary" :loading="generating" @click="handleGenerate">AI 生成</el-button>
              <el-button :disabled="!currentPlan" :loading="saving" @click="handleSave">{{ currentPlan?.id ? '更新行程' : '保存行程' }}</el-button>
            </div>
          </el-form>
        </div>

        <div class="planner-panel preview-panel">
          <div class="panel-head">
            <div>
              <h2>{{ currentPlan?.title || '行程预览' }}</h2>
              <p>{{ currentPlan?.destination || '生成后将在这里展示每日安排' }}</p>
            </div>
            <el-switch v-model="isPublic" active-text="公开" inactive-text="私密" :disabled="!currentPlan" />
          </div>

          <el-empty v-if="!currentPlan" description="暂无行程" />
          <div v-else class="timeline">
            <div v-for="day in parsedPlan" :key="day.day" class="day-card">
              <div class="day-index">D{{ day.day }}</div>
              <div class="day-content">
                <h3>{{ day.theme }}</h3>
                <ul>
                  <li v-for="item in day.items" :key="item">{{ item }}</li>
                </ul>
                <p v-if="day.tips">{{ day.tips }}</p>
              </div>
            </div>
          </div>
        </div>

        <div class="planner-panel plans-panel">
          <div class="panel-head">
            <div>
              <h2>我的行程</h2>
              <p>保存后的计划可继续分享或导出</p>
            </div>
          </div>
          <el-empty v-if="plans.length === 0" description="暂无已保存行程" />
          <div v-else class="plan-list">
            <button
              v-for="plan in plans"
              :key="plan.id"
              class="plan-item"
              :class="{ active: currentPlan?.id === plan.id }"
              @click="selectPlan(plan)"
            >
              <strong>{{ plan.title }}</strong>
              <span>{{ plan.destination }} · {{ plan.start_date || '未定日期' }}</span>
            </button>
          </div>
          <div class="actions plan-actions">
            <el-button :disabled="!currentPlan" @click="openEditDialog">编辑</el-button>
            <el-button :disabled="!currentPlan?.id" @click="handleShare">分享</el-button>
            <el-button :disabled="!currentPlan?.id" @click="handleExport">导出</el-button>
            <el-button type="danger" plain :disabled="!currentPlan?.id" @click="handleDelete">删除</el-button>
          </div>
        </div>
      </div>
    </section>

    <el-dialog v-model="showEditDialog" title="编辑行程" width="680px">
      <el-form label-position="top">
        <el-form-item label="标题">
          <el-input v-model="editForm.title" />
        </el-form-item>
        <el-form-item label="目的地">
          <el-input v-model="editForm.destination" />
        </el-form-item>
        <div class="form-row edit-date-row">
          <el-form-item label="开始日期">
            <el-date-picker v-model="editForm.start_date" type="date" value-format="YYYY-MM-DD" placeholder="开始日期" />
          </el-form-item>
          <el-form-item label="结束日期">
            <el-date-picker v-model="editForm.end_date" type="date" value-format="YYYY-MM-DD" placeholder="结束日期" />
          </el-form-item>
        </div>
        <el-form-item label="每日安排 JSON">
          <el-input v-model="editForm.plan_data" type="textarea" :rows="8" />
        </el-form-item>
        <el-form-item label="公开状态">
          <el-switch v-model="editForm.is_public" :active-value="1" :inactive-value="0" active-text="公开" inactive-text="私密" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleEditSubmit">保存修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createPlan,
  deletePlan,
  exportPlan,
  generatePlan,
  getMyPlans,
  sharePlan,
  updatePlan,
  type TravelPlan
} from '@/api/itinerary'

interface PlanDay {
  day: number
  theme: string
  items: string[]
  tips?: string
}

const form = reactive({
  destination: '杭州',
  days: 3,
  startDate: '',
  budget: '人均 3000 元',
  preferences: '慢节奏、美食、城市漫步'
})

const plans = ref<TravelPlan[]>([])
const currentPlan = ref<TravelPlan | null>(null)
const loadingPlans = ref(false)
const generating = ref(false)
const saving = ref(false)
const showEditDialog = ref(false)

const editForm = reactive<TravelPlan>({
  title: '',
  destination: '',
  start_date: '',
  end_date: '',
  plan_data: '[]',
  is_public: 0
})

const isPublic = computed({
  get: () => currentPlan.value?.is_public === 1,
  set: (value: boolean) => {
    if (currentPlan.value) currentPlan.value.is_public = value ? 1 : 0
  }
})

const parsedPlan = computed<PlanDay[]>(() => {
  if (!currentPlan.value?.plan_data) return []
  try {
    const parsed = JSON.parse(currentPlan.value.plan_data)
    return Array.isArray(parsed)
      ? parsed.map((item, index) => ({
          day: Number(item.day || index + 1),
          theme: String(item.theme || `第 ${index + 1} 天`),
          items: Array.isArray(item.items) ? item.items.map(String) : [],
          tips: item.tips ? String(item.tips) : ''
        }))
      : []
  } catch {
    return []
  }
})

const loadPlans = async () => {
  loadingPlans.value = true
  try {
    const page = await getMyPlans({ page: 1, size: 20 })
    plans.value = page.list || []
    if (!currentPlan.value && plans.value.length > 0) currentPlan.value = plans.value[0]
  } finally {
    loadingPlans.value = false
  }
}

const handleGenerate = async () => {
  if (!form.destination.trim()) {
    ElMessage.warning('请先填写目的地')
    return
  }
  generating.value = true
  try {
    currentPlan.value = await generatePlan({ ...form })
    currentPlan.value.is_public = 0
    ElMessage.success('行程已生成')
  } finally {
    generating.value = false
  }
}

const handleSave = async () => {
  if (!currentPlan.value) return
  saving.value = true
  try {
    const isUpdating = Boolean(currentPlan.value.id)
    const saved = isUpdating
      ? await updatePlan(currentPlan.value.id, currentPlan.value)
      : await createPlan(currentPlan.value)
    currentPlan.value = saved
    await loadPlans()
    ElMessage.success(isUpdating ? '行程已更新' : '行程已保存')
  } finally {
    saving.value = false
  }
}

const selectPlan = (plan: TravelPlan) => {
  currentPlan.value = { ...plan }
}

const openEditDialog = () => {
  if (!currentPlan.value) return
  Object.assign(editForm, {
    id: currentPlan.value.id,
    user_id: currentPlan.value.user_id,
    title: currentPlan.value.title,
    destination: currentPlan.value.destination,
    start_date: currentPlan.value.start_date || '',
    end_date: currentPlan.value.end_date || '',
    plan_data: currentPlan.value.plan_data || '[]',
    is_public: currentPlan.value.is_public
  })
  showEditDialog.value = true
}

const handleEditSubmit = async () => {
  if (!editForm.title.trim() || !editForm.destination.trim()) {
    ElMessage.warning('请填写标题和目的地')
    return
  }
  try {
    JSON.parse(editForm.plan_data || '[]')
  } catch {
    ElMessage.warning('每日安排 JSON 格式不正确')
    return
  }
  saving.value = true
  try {
    const saved = editForm.id ? await updatePlan(editForm.id, editForm) : await createPlan(editForm)
    currentPlan.value = saved
    showEditDialog.value = false
    await loadPlans()
    ElMessage.success('行程已更新')
  } finally {
    saving.value = false
  }
}

const handleShare = async () => {
  if (!currentPlan.value?.id) return
  const result = await sharePlan(currentPlan.value.id)
  currentPlan.value.is_public = 1
  ElMessage.success(`分享链接：${result.shortLink}`)
  await loadPlans()
}

const handleExport = async () => {
  if (!currentPlan.value?.id) return
  const result = await exportPlan(currentPlan.value.id)
  ElMessage.success(`导出地址：${result.fileUrl}`)
}

const handleDelete = async () => {
  if (!currentPlan.value?.id) return
  await ElMessageBox.confirm('确认删除该行程？', '删除行程', { type: 'warning' })
  await deletePlan(currentPlan.value.id)
  currentPlan.value = null
  await loadPlans()
  ElMessage.success('已删除')
}

onMounted(loadPlans)
</script>

<style scoped>
.module-page { padding-top: 64px; }
.module-hero {
  background: linear-gradient(135deg, #0a1628, #1e3f66 55%, #c9953d);
  color: #fff;
  padding: 48px 0 36px;
}
.module-hero .section-title,
.module-hero .section-subtitle { color: #fff; }
.hero-inner {
  align-items: center;
  display: flex;
  justify-content: space-between;
  gap: 24px;
}
.module-kicker {
  color: var(--gold-300);
  display: block;
  font-weight: 700;
  margin-bottom: 10px;
}
.planner-section { padding: 32px 0 56px; }
.planner-grid {
  display: grid;
  gap: 18px;
  grid-template-columns: 360px 1fr;
}
.planner-panel {
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  border-radius: 8px;
  box-shadow: var(--shadow-sm);
  padding: 22px;
}
.planner-panel h2 {
  color: var(--text-primary);
  font-size: 20px;
  margin-bottom: 14px;
}
.form-row {
  display: grid;
  gap: 14px;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1.35fr);
}
.form-row :deep(.el-input-number),
.form-row :deep(.el-date-editor.el-input) {
  width: 100%;
}
.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.preview-panel { min-height: 520px; }
.panel-head {
  align-items: flex-start;
  display: flex;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}
.panel-head p {
  color: var(--text-secondary);
  font-size: 14px;
}
.timeline {
  display: grid;
  gap: 14px;
}
.day-card {
  border: 1px solid var(--border-light);
  border-radius: 8px;
  display: grid;
  gap: 14px;
  grid-template-columns: 54px 1fr;
  padding: 16px;
}
.day-index {
  align-items: center;
  background: var(--gold-200);
  border-radius: 8px;
  color: var(--navy-900);
  display: flex;
  font-weight: 800;
  height: 42px;
  justify-content: center;
}
.day-content h3 {
  font-size: 17px;
  margin-bottom: 8px;
}
.day-content ul {
  display: grid;
  gap: 6px;
  list-style: disc;
  padding-left: 18px;
}
.day-content p {
  color: var(--text-secondary);
  margin-top: 8px;
}
.plans-panel {
  grid-column: 1 / -1;
}
.plan-list {
  display: grid;
  gap: 10px;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
}
.plan-item {
  border: 1px solid var(--border-light);
  border-radius: 8px;
  color: var(--text-primary);
  padding: 14px;
  text-align: left;
}
.plan-item:hover {
  border-color: var(--accent);
}
.plan-item.active {
  background: var(--cream-100);
  border-color: var(--accent);
}
.plan-item strong,
.plan-item span {
  display: block;
}
.plan-item span {
  color: var(--text-secondary);
  font-size: 13px;
  margin-top: 4px;
}
.plan-actions { margin-top: 16px; }
.edit-date-row { grid-template-columns: minmax(0, 1fr) minmax(0, 1fr); }
@media (max-width: 900px) {
  .hero-inner,
  .planner-grid { grid-template-columns: 1fr; }
  .hero-inner { align-items: flex-start; flex-direction: column; }
  .form-row { grid-template-columns: 1fr; }
}
</style>
