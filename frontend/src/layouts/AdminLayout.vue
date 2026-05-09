<template>
  <div class="admin-shell">
    <aside class="admin-sidebar">
      <div class="sidebar-brand">
        <span class="brand-icon">✦</span>
        <span class="brand-text">Lightmark</span>
        <span class="brand-badge">Admin</span>
      </div>
      <nav class="admin-nav">
        <router-link to="/admin/dashboard">
          <span class="nav-icon">📊</span>
          仪表盘
        </router-link>
        <router-link to="/admin/products">
          <span class="nav-icon">📦</span>
          产品管理
        </router-link>
        <router-link to="/admin/orders">
          <span class="nav-icon">📋</span>
          订单总览
        </router-link>
        <router-link to="/admin/users">
          <span class="nav-icon">👥</span>
          用户管理
        </router-link>
        <router-link to="/admin/tables">
          <span class="nav-icon">�</span>
          操作日志
        </router-link>
      </nav>
      <div class="sidebar-footer">
        <router-link to="/">← 主站首页</router-link>
      </div>
    </aside>

    <div class="admin-main">
      <header class="admin-header">
        <div class="admin-header-left">
          <h2>{{ pageTitle }}</h2>
          <p>欢迎，{{ authStore.nickname || "管理员" }}</p>
        </div>
        <div class="header-actions">
          <ThemeToggle />
          <el-button size="small" @click="logout" round>退出登录</el-button>
        </div>
      </header>

      <main class="admin-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { logoutApi } from "@/api/auth";
import { useAuthStore } from "@/stores/auth";
import ThemeToggle from "@/components/ThemeToggle.vue";

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const titleMap: Record<string, string> = {
  "admin-dashboard": "后台仪表盘",
  "admin-products": "产品管理",
  "admin-orders": "订单总览",
  "admin-users": "用户管理",
  "admin-tables": "操作日志",
};

const pageTitle = computed(() => {
  const routeName = String(route.name || "");
  return titleMap[routeName] || "后台管理";
});

const logout = async () => {
  try {
    await logoutApi();
  } catch {
    // 后端登出失败不阻塞本地退出。
  }

  authStore.clearSession();
  ElMessage.success("已退出登录");
  await router.replace("/admin/login");
};
</script>
