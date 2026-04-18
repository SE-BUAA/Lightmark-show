<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { fetchDashboardSummary } from '@/api/admin'
import type { DashboardSummary } from '@/types/app'

const loading = ref(false)
const errorMessage = ref('')
const summary = ref<DashboardSummary>({
  totalUsers: 0,
  totalOrders: 0,
  paidOrders: 0,
  gmv: 0,
  activeProducts: 0,
  hotDestinations: [],
})

async function load(): Promise<void> {
  loading.value = true
  errorMessage.value = ''
  try {
    summary.value = await fetchDashboardSummary()
  } catch (error: unknown) {
    errorMessage.value = error instanceof Error ? error.message : '加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <section>
    <h2>运营仪表盘</h2>
    <p class="hint">基础指标：交易额、订单、用户、热门产品。</p>

    <p v-if="errorMessage" class="error">{{ errorMessage }}</p>
    <p v-if="loading" class="hint">加载中...</p>

    <div class="stats-grid">
      <article class="card stat">
        <h3>总用户</h3>
        <strong>{{ summary.totalUsers }}</strong>
      </article>
      <article class="card stat">
        <h3>总订单</h3>
        <strong>{{ summary.totalOrders }}</strong>
      </article>
      <article class="card stat">
        <h3>已支付订单</h3>
        <strong>{{ summary.paidOrders }}</strong>
      </article>
      <article class="card stat">
        <h3>交易额</h3>
        <strong>{{ summary.gmv }}</strong>
      </article>
      <article class="card stat">
        <h3>在售产品</h3>
        <strong>{{ summary.activeProducts }}</strong>
      </article>
    </div>

    <article class="card">
      <h3>热门产品</h3>
      <table class="table">
        <thead>
          <tr>
            <th>名称</th>
            <th>销量</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in summary.hotDestinations" :key="item.name">
            <td>{{ item.name }}</td>
            <td>{{ item.sold_count }}</td>
          </tr>
        </tbody>
      </table>
    </article>
  </section>
</template>

