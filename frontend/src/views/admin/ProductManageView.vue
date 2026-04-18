<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { fetchProducts, updateProductStatus } from '@/api/admin'
import type { ProductRow } from '@/types/app'

const loading = ref(false)
const errorMessage = ref('')
const rows = ref<ProductRow[]>([])

async function load(): Promise<void> {
  loading.value = true
  errorMessage.value = ''
  try {
    const data = await fetchProducts({ page: 1, size: 20 })
    rows.value = data.items || []
  } catch (error: unknown) {
    errorMessage.value = error instanceof Error ? error.message : '加载失败'
  } finally {
    loading.value = false
  }
}

async function toggleStatus(row: ProductRow): Promise<void> {
  const targetStatus = row.status === 1 ? 0 : 1
  try {
    await updateProductStatus(row.id, targetStatus)
    await load()
  } catch (error: unknown) {
    errorMessage.value = error instanceof Error ? error.message : '操作失败'
  }
}

onMounted(load)
</script>

<template>
  <section>
    <h2>产品管理</h2>
    <p class="hint">支持上架/下架，后续可扩展价格和库存编辑。</p>

    <p v-if="errorMessage" class="error">{{ errorMessage }}</p>
    <p v-if="loading" class="hint">加载中...</p>

    <table class="table card">
      <thead>
        <tr>
          <th>ID</th>
          <th>类型</th>
          <th>名称</th>
          <th>价格</th>
          <th>库存</th>
          <th>状态</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="row in rows" :key="row.id">
          <td>{{ row.id }}</td>
          <td>{{ row.product_type }}</td>
          <td>{{ row.name }}</td>
          <td>{{ row.price }}</td>
          <td>{{ row.stock }}</td>
          <td>{{ row.status === 1 ? '上架' : '下架' }}</td>
          <td><button class="btn ghost" type="button" @click="toggleStatus(row)">{{ row.status === 1 ? '下架' : '上架' }}</button></td>
        </tr>
      </tbody>
    </table>
  </section>
</template>

