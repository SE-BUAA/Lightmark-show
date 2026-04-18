<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { fetchUsers, updateUserLevel, updateUserStatus } from '@/api/admin'
import type { UserRow } from '@/types/app'

const loading = ref(false)
const errorMessage = ref('')
const rows = ref<UserRow[]>([])

async function load(): Promise<void> {
  loading.value = true
  errorMessage.value = ''
  try {
    const data = await fetchUsers({ page: 1, size: 20 })
    rows.value = data.items || []
  } catch (error: unknown) {
    errorMessage.value = error instanceof Error ? error.message : '加载失败'
  } finally {
    loading.value = false
  }
}

async function toggleStatus(row: UserRow): Promise<void> {
  const next = row.status === 1 ? 0 : 1
  try {
    await updateUserStatus(row.id, next)
    await load()
  } catch (error: unknown) {
    errorMessage.value = error instanceof Error ? error.message : '操作失败'
  }
}

async function promote(row: UserRow): Promise<void> {
  const next = Math.min((row.level || 0) + 1, 3)
  try {
    await updateUserLevel(row.id, next)
    await load()
  } catch (error: unknown) {
    errorMessage.value = error instanceof Error ? error.message : '操作失败'
  }
}

onMounted(load)
</script>

<template>
  <section>
    <h2>用户管理</h2>
    <p class="hint">支持封禁/解封、会员等级调整。</p>

    <p v-if="errorMessage" class="error">{{ errorMessage }}</p>
    <p v-if="loading" class="hint">加载中...</p>

    <table class="table card">
      <thead>
        <tr>
          <th>ID</th>
          <th>昵称</th>
          <th>手机号</th>
          <th>积分</th>
          <th>等级</th>
          <th>状态</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="row in rows" :key="row.id">
          <td>{{ row.id }}</td>
          <td>{{ row.nickname }}</td>
          <td>{{ row.phone }}</td>
          <td>{{ row.points }}</td>
          <td>{{ row.level }}</td>
          <td>{{ row.status === 0 ? '正常' : '封禁' }}</td>
          <td>
            <button class="btn ghost" type="button" @click="toggleStatus(row)">{{ row.status === 0 ? '封禁' : '解封' }}</button>
            <button class="btn ghost" type="button" @click="promote(row)">升级</button>
          </td>
        </tr>
      </tbody>
    </table>
  </section>
</template>

