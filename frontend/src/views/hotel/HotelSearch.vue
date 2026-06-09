<template>
  <main class="hotel-search-page">
    <section class="search-toolbar">
      <el-button class="back-button" @click="goBack">返回上一步</el-button>
      <el-form class="search-form" :model="store" @submit.prevent>
        <el-input v-model="store.destination" placeholder="目的地 / 酒店关键词" clearable />
        <el-date-picker
          v-model="store.dates"
          type="daterange"
          value-format="YYYY-MM-DD"
          range-separator="至"
          start-placeholder="入住日期"
          end-placeholder="离店日期"
        />
        <label class="number-field">
          <span>入住人数</span>
          <el-input-number v-model="store.adultNum" :min="1" :max="8" controls-position="right" />
        </label>
        <el-button class="search-button" type="primary" :loading="loading" @click="handleSearch">搜索</el-button>
      </el-form>
    </section>

    <section class="content-layout">
      <aside class="filter-sidebar">
        <FilterPanel />
      </aside>

      <div class="result-area">
        <div class="result-bar">
          <el-radio-group v-model="store.sortBy" @change="handleSortChange">
            <el-radio-button label="price_asc">价格优先</el-radio-button>
            <el-radio-button label="rating_desc">评分优先</el-radio-button>
            <el-radio-button label="distance_asc">距离优先</el-radio-button>
          </el-radio-group>
          <div class="bar-actions">
            <el-button class="mobile-filter" @click="filterDrawer = true">筛选</el-button>
            <el-button type="primary" plain @click="openAiRecommend">AI 推荐</el-button>
            <el-button @click="openMapDialog">查看地图</el-button>
          </div>
        </div>

        <el-alert
          v-if="store.destination && store.sortBy === 'distance_asc'"
          class="distance-alert"
          type="info"
          :closable="false"
          show-icon
          :title="`已按距离「${store.destination}」由近到远排序`"
        />

        <el-skeleton v-if="loading" :rows="8" animated />
        <el-empty v-else-if="hotels.length === 0" description="暂无匹配酒店" />
        <div v-else class="hotel-grid">
          <el-card v-for="hotel in hotels" :key="hotel.id" class="hotel-card" shadow="hover">
            <img class="hotel-image" :src="hotel.coverImage || fallbackImage" :alt="hotel.name" />
            <div class="hotel-body">
              <div class="hotel-title-row">
                <h2>{{ hotel.name }}</h2>
                <el-tag v-if="hotel.starLevel">{{ hotel.starLevel }} 星</el-tag>
              </div>
              <p class="address">{{ hotel.address || '地址待补充' }}</p>
              <div class="meta-row">
                <span>{{ hotel.rating || '--' }} 分</span>
                <span v-if="hotel.estimatedDistance !== undefined">距目标 {{ hotel.estimatedDistance }} km</span>
                <strong>¥{{ hotel.priceMin || '--' }} 起</strong>
              </div>
              <div class="room-recommend">
                <span>{{ hotel.recommendedRoomType }}</span>
                <small>按 {{ store.adultNum }} 人入住智能建议</small>
              </div>
              <div class="facility-row">
                <el-tag v-for="facility in hotel.facilities || []" :key="facility" size="small" effect="plain">
                  {{ facility }}
                </el-tag>
              </div>
              <el-button type="primary" @click="openDetail(hotel.id)">查看房型</el-button>
            </div>
          </el-card>
        </div>

        <el-pagination
          class="pagination"
          layout="prev, pager, next"
          :total="total"
          :page-size="store.size"
          :current-page="store.page"
          @current-change="changePage"
        />
      </div>
    </section>

    <el-drawer v-model="filterDrawer" title="筛选" direction="btt" size="70%">
      <FilterPanel />
    </el-drawer>

    <el-dialog v-model="mapVisible" title="15公里内酒店地图" width="760px">
      <el-alert
        v-if="store.destination"
        class="map-source"
        type="info"
        :closable="false"
        show-icon
        :title="`地图中心：${store.destination}`"
      />
      <div v-if="mapCenter" class="mock-map">
        <div class="map-center">目标位置</div>
        <button
          v-for="hotel in mapHotels"
          :key="hotel.id"
          class="map-marker"
          :style="markerStyle(hotel)"
          @click="openDetail(hotel.id)"
        >
          {{ hotel.name }}
          <small>{{ hotel.estimatedDistance }} km</small>
        </button>
      </div>
      <el-empty v-else description="请先在搜索框填写可识别的城市或地标，例如北京王府井、上海外滩、杭州西湖" />
      <div v-if="mapCenter && mapHotels.length === 0" class="map-empty">半径 15 公里内暂无匹配酒店。</div>
    </el-dialog>

    <AIChatFloat ref="aiChatRef" :hide-fab="true" />
  </main>
</template>

<script setup lang="ts">
import { defineComponent, h, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElButton, ElCheckbox, ElCheckboxGroup, ElMessage, ElRadioButton, ElRadioGroup } from 'element-plus'
import { hotelPoint, nearbyHotels, resolvePoint, useHotelSearch, type HotelDisplayVO } from '@/composables/useHotelSearch'
import AIChatFloat from '@/views/hotel/components/AIChatFloat.vue'

const router = useRouter()
const { store, loading, hotels, allHotels, total, runSearch } = useHotelSearch()
const fallbackImage = 'https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=900&q=80'
const filterDrawer = ref(false)
const mapVisible = ref(false)
const mapCenter = ref<ReturnType<typeof resolvePoint>>()
const mapHotels = ref<HotelDisplayVO[]>([])
const aiChatRef = ref<InstanceType<typeof AIChatFloat>>()

const handleSearch = async () => {
  store.setSearch({ page: 1 })
  await runSearch()
  if (mapVisible.value) {
    buildMapFromDestination()
  }
}

const handleSortChange = async () => {
  if (store.sortBy === 'distance_asc' && !resolvePoint(store.destination)) {
    ElMessage.warning('请先在搜索框填写可识别的城市或地标，例如北京王府井、上海外滩、杭州西湖')
    store.setSearch({ sortBy: 'rating_desc', page: 1 })
    await runSearch()
    return
  }
  store.setPage(1)
  await runSearch()
}

const openMapDialog = async () => {
  if (!resolvePoint(store.destination)) {
    ElMessage.warning('请先在搜索框填写可识别的城市或地标，例如北京王府井、上海外滩、杭州西湖')
    return
  }
  mapVisible.value = true
  if (!allHotels.value.length) {
    await runSearch()
  }
  buildMapFromDestination()
}

const buildMapFromDestination = () => {
  const address = store.destination.trim()
  const center = resolvePoint(address)
  if (!center) {
    mapCenter.value = undefined
    mapHotels.value = []
    return
  }
  const nearby = nearbyHotels(allHotels.value.length ? allHotels.value : hotels.value, address, 15)
  mapCenter.value = nearby.center
  mapHotels.value = nearby.hotels.slice(0, 8)
}

const markerStyle = (hotel: HotelDisplayVO) => {
  const point = hotelPoint(hotel)
  if (!mapCenter.value || !point) return {}
  const latOffset = (mapCenter.value.lat - point.lat) * 850
  const lngOffset = (point.lng - mapCenter.value.lng) * 650
  const x = Math.max(8, Math.min(84, 46 + lngOffset))
  const y = Math.max(10, Math.min(82, 48 + latOffset))
  return { left: `${x}%`, top: `${y}%` }
}

const openAiRecommend = () => {
  aiChatRef.value?.open()
}

const goBack = () => {
  router.back()
}

const changePage = async (page: number) => {
  store.setPage(page)
  await runSearch()
}

const openDetail = (id: string) => {
  router.push({
    path: `/hotels/detail/${id}`,
    query: {
      checkIn: store.dates[0],
      checkOut: store.dates[1]
    }
  })
}

const FilterPanel = defineComponent({
  setup() {
    const facilities = ['早餐', '停车场', '健身房', '接送机']
    const cancelPolicies = [
      { label: '不限', value: '' },
      { label: '免费取消', value: 'FREE_CANCEL' },
      { label: '不可取消', value: 'NON_REFUNDABLE' }
    ]
    const selectFacility = (values: string[]) => {
      store.setFilters({ facility: values[values.length - 1] })
      runSearch()
    }
    const selectCancel = (value: string) => {
      store.setFilters({ cancelPolicy: value || undefined })
      runSearch()
    }
    const selectStar = (value: string) => {
      store.setFilters({ starLevel: value ? Number(value) : undefined })
      runSearch()
    }
    return () => h('div', { class: 'filter-panel' }, [
      h('h3', '筛选'),
      h('span', { class: 'filter-label' }, '星级'),
      h(ElRadioGroup, { modelValue: String(store.filters.starLevel || ''), 'onUpdate:modelValue': selectStar }, () => [
        h(ElRadioButton, { label: '' }, () => '不限'),
        h(ElRadioButton, { label: '3' }, () => '3星'),
        h(ElRadioButton, { label: '4' }, () => '4星'),
        h(ElRadioButton, { label: '5' }, () => '5星')
      ]),
      h('span', { class: 'filter-label' }, '设施'),
      h(ElCheckboxGroup, { modelValue: store.filters.facility ? [store.filters.facility] : [], onChange: selectFacility }, () =>
        facilities.map(item => h(ElCheckbox, { label: item, key: item }, () => item))
      ),
      h('span', { class: 'filter-label' }, '取消政策'),
      h(ElRadioGroup, { modelValue: store.filters.cancelPolicy || '', 'onUpdate:modelValue': selectCancel }, () =>
        cancelPolicies.map(item => h(ElRadioButton, { label: item.value, key: item.value }, () => item.label))
      ),
      h(ElButton, { onClick: () => { store.resetFilters(); runSearch() } }, () => '重置筛选')
    ])
  }
})

watch(() => [store.adultNum, store.destination], () => store.persist())
watch(hotels, () => {
  if (mapVisible.value) {
    buildMapFromDestination()
  }
})

onMounted(runSearch)
</script>

<style scoped>
.hotel-search-page {
  min-height: 100vh;
  padding: 88px clamp(16px, 4vw, 48px) 40px;
  background: var(--el-bg-color-page);
}

.search-toolbar {
  padding: 16px;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
}

.back-button {
  margin-bottom: 12px;
}

.search-form {
  display: grid;
  grid-template-columns: minmax(180px, 1.2fr) minmax(260px, 1.4fr) 132px auto;
  gap: 12px;
  align-items: end;
}

.number-field {
  display: grid;
  gap: 6px;
  min-width: 0;
}

.number-field span {
  color: var(--el-text-color-secondary);
  font-size: 13px;
  line-height: 1;
}

.number-field :deep(.el-input-number) {
  width: 100%;
}

.search-button {
  min-width: 86px;
}

.content-layout {
  display: grid;
  grid-template-columns: 240px minmax(0, 1fr);
  gap: 20px;
  margin-top: 20px;
}

.filter-sidebar {
  align-self: start;
  padding: 16px;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
}

.result-area {
  min-width: 0;
}

.result-bar {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  margin-bottom: 16px;
}

.bar-actions {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.mobile-filter {
  display: none;
}

.distance-alert,
.map-source {
  margin-bottom: 16px;
}

.hotel-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.hotel-card :deep(.el-card__body) {
  padding: 0;
}

.hotel-image {
  width: 100%;
  aspect-ratio: 16 / 10;
  object-fit: cover;
  display: block;
}

.hotel-body {
  display: grid;
  gap: 10px;
  padding: 14px;
}

.hotel-title-row,
.meta-row {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  align-items: center;
}

.hotel-title-row h2 {
  margin: 0;
  font-size: 17px;
  line-height: 1.35;
}

.address {
  margin: 0;
  min-height: 40px;
  color: var(--el-text-color-secondary);
}

.meta-row strong {
  color: var(--el-color-danger);
}

.room-recommend {
  display: grid;
  gap: 2px;
  padding: 10px 12px;
  border-radius: 8px;
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
}

.room-recommend span {
  font-weight: 700;
}

.room-recommend small {
  color: var(--el-text-color-secondary);
}

.facility-row {
  min-height: 24px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.pagination {
  justify-content: center;
  margin-top: 22px;
}

.mock-map {
  position: relative;
  height: 430px;
  overflow: hidden;
  border-radius: 8px;
  border: 1px solid var(--el-border-color-light);
  background:
    linear-gradient(90deg, rgba(64, 158, 255, 0.12) 1px, transparent 1px),
    linear-gradient(rgba(64, 158, 255, 0.12) 1px, transparent 1px),
    linear-gradient(135deg, #eef7f2, #edf4ff);
  background-size: 44px 44px, 44px 44px, 100% 100%;
}

.mock-map::before,
.mock-map::after {
  content: "";
  position: absolute;
  background: rgba(64, 158, 255, 0.22);
}

.mock-map::before {
  width: 100%;
  height: 18px;
  left: 0;
  top: 45%;
  transform: rotate(-8deg);
}

.mock-map::after {
  width: 22px;
  height: 100%;
  left: 58%;
  top: 0;
  transform: rotate(18deg);
}

.map-center,
.map-marker {
  position: absolute;
  z-index: 1;
  transform: translate(-50%, -50%);
}

.map-center {
  left: 50%;
  top: 50%;
  padding: 8px 12px;
  border-radius: 999px;
  color: #fff;
  background: var(--el-color-danger);
  font-weight: 700;
}

.map-marker {
  max-width: 150px;
  padding: 7px 10px;
  border: 0;
  border-radius: 999px;
  color: #fff;
  background: var(--el-color-primary);
  box-shadow: var(--el-box-shadow-light);
  cursor: pointer;
  display: grid;
  gap: 2px;
  font-size: 12px;
}

.map-marker small {
  font-size: 11px;
  opacity: 0.86;
}

.map-empty {
  margin-top: 12px;
  color: var(--el-text-color-secondary);
}

:global(.filter-panel) {
  display: grid;
  gap: 14px;
}

:global(.filter-panel h3) {
  margin: 0;
  font-size: 18px;
}

:global(.filter-label) {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

@media (max-width: 1080px) {
  .search-form {
    grid-template-columns: 1fr 1fr;
  }

  .hotel-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .hotel-search-page {
    padding-inline: 12px;
  }

  .search-form,
  .content-layout {
    grid-template-columns: 1fr;
  }

  .filter-sidebar {
    display: none;
  }

  .result-bar {
    align-items: stretch;
    flex-direction: column;
  }

  .mobile-filter {
    display: inline-flex;
  }

  .hotel-grid {
    grid-template-columns: 1fr;
  }
}
</style>
