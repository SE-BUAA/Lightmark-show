<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { fetchOrders, updateOrderStatus } from '@/api/admin'
import type { OrderRow } from '@/types/app'

const loading = ref(false)
const errorMessage = ref('')
const rows = ref<OrderRow[]>([])

const statusMap: Record<number, string> = {
  0: '待支付',
  1: '已支付',
  2: '已出行',
  3: '已取消',
  4: '退款中',
}

async function load(): Promise<void> {
  loading.value = true
  errorMessage.value = ''
  try {
    const data = await fetchOrders({ page: 1, size: 20 })
    rows.value = data.items || []
  } catch (error: unknown) {
    errorMessage.value = error instanceof Error ? error.message : '加载失败'
  } finally {
    loading.value = false
  }
}

async function cancelOrder(row: OrderRow): Promise<void> {
  try {
    await updateOrderStatus(row.id, 3, '管理员取消')
    await load()
  } catch (error: unknown) {
    errorMessage.value = error instanceof Error ? error.message : '操作失败'
  }
}

onMounted(load)
</script>

<template>
  <section>
    <h2>订单总览与干预</h2>
    <p class="hint">支持查看订单并执行模拟取消。</p>

    <p v-if="errorMessage" class="error">{{ errorMessage }}</p>
    <p v-if="loading" class="hint">加载中...</p>

    <table class="table card">
      <thead>
        <tr>
          <th>ID</th>
          <th>订单号</th>
          <th>用户ID</th>
          <th>类型</th>
          <th>金额</th>
          <th>支付方式</th>
          <th>状态</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="row in rows" :key="row.id">
          <td>{{ row.id }}</td>
          <td>{{ row.order_no }}</td>
          <td>{{ row.user_id }}</td>
          <td>{{ row.order_type }}</td>
          <td>{{ row.pay_amount }}</td>
          <td>{{ row.payment_method }}</td>
          <td>{{ statusMap[row.status] || row.status }}</td>
          <td>
            <button class="btn ghost" type="button" :disabled="row.status === 3" @click="cancelOrder(row)">取消订单</button>
          </td>
        </tr>
      </tbody>
    </table>
  </section>
</template>

