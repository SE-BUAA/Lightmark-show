<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { login, register } from '@/api/auth'
import { saveSession } from '@/utils/auth'
import type { LoginRequest, RegisterRequest } from '@/types/app'

const router = useRouter()
const loading = ref(false)
const errorMessage = ref('')

const form = reactive<LoginRequest>({
  account: '13800000000',
  password: '123456',
})

async function submit(): Promise<void> {
  errorMessage.value = ''
  loading.value = true
  try {
    const data = await login(form)
    saveSession(data.token, {
      userId: data.userId,
      nickname: data.nickname,
      roles: data.roles,
    })
    router.push('/admin/dashboard')
  } catch (error: unknown) {
    errorMessage.value = error instanceof Error ? error.message : '登录失败'
  } finally {
    loading.value = false
  }
}

async function quickRegister(): Promise<void> {
  loading.value = true
  errorMessage.value = ''
  try {
    const suffix = Date.now().toString().slice(-8)
    const payload: RegisterRequest = {
      account: `139${suffix}`,
      password: '123456',
      nickname: `新用户${suffix.slice(-4)}`,
    }
    await register(payload)
    form.account = `139${suffix}`
    form.password = '123456'
  } catch (error: unknown) {
    errorMessage.value = error instanceof Error ? error.message : '注册失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <form class="card" @submit.prevent="submit">
      <h2>拾光旅行后台登录</h2>
      <p class="hint">模块一：身份认证 + 路由守卫</p>

      <label class="field">
        <span>手机号/邮箱</span>
        <input v-model="form.account" type="text" placeholder="请输入账号" autocomplete="username" />
      </label>

      <label class="field">
        <span>密码</span>
        <input v-model="form.password" type="password" placeholder="请输入密码" autocomplete="current-password" />
      </label>

      <p v-if="errorMessage" class="error">{{ errorMessage }}</p>

      <button class="btn" type="submit" :disabled="loading">
        {{ loading ? '登录中...' : '登录' }}
      </button>
      <button class="btn ghost" type="button" :disabled="loading" @click="quickRegister">
        快速注册测试账号
      </button>
    </form>
  </div>
</template>

