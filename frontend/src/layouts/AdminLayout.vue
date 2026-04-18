<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { logout } from '@/api/auth'
import { clearSession, getUser } from '@/utils/auth'
import type { SessionUser } from '@/types/app'

const route = useRoute()
const router = useRouter()

const currentUser = computed<SessionUser | null>(() => getUser())

const menus = [
  { to: '/admin/dashboard', label: '运营仪表盘' },
  { to: '/admin/products', label: '产品管理' },
  { to: '/admin/orders', label: '订单总览' },
  { to: '/admin/users', label: '用户管理' },
  { to: '/admin/m2', label: 'M2 占位接口' },
  { to: '/admin/m3', label: 'M3 占位接口' },
  { to: '/admin/m4', label: 'M4 占位接口' },
  { to: '/admin/m5', label: 'M5 占位接口' },
  { to: '/admin/m6', label: 'M6 占位接口' },
]

async function onLogout() {
  try {
    await logout()
  } catch {
    // Keep local logout behavior even when backend token is already invalid.
  }
  clearSession()
  await router.push('/login')
}
</script>

<template>
  <div class="layout-root">
    <aside class="sidebar">
      <RouterLink to="/" class="brand brand-link-inline">
        <span class="brand-mark">T</span>
        <div>
          <strong>Timemark</strong>
          <small>返回首页</small>
        </div>
      </RouterLink>
      <p class="subtitle">模块一后台骨架 · 预留 M2~M6</p>
      <nav class="menu">
        <RouterLink
          v-for="item in menus"
          :key="item.to"
          :to="item.to"
          class="menu-item"
          :class="{ active: route.path === item.to }"
        >
          {{ item.label }}
        </RouterLink>
      </nav>
    </aside>

    <div class="content-area">
      <header class="topbar">
        <span>欢迎，{{ currentUser?.nickname || '游客' }}</span>
        <div class="topbar-actions">
          <RouterLink class="btn ghost" to="/">看前台首页</RouterLink>
          <button class="btn" type="button" @click="onLogout">退出登录</button>
        </div>
      </header>

      <main class="page-content">
        <RouterView />
      </main>
    </div>
  </div>
</template>

