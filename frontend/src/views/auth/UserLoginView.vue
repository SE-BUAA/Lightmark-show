<template>
  <div class="auth-page">
    <div class="auth-bg"></div>
    <div class="auth-card">
      <div class="auth-header">
        <div class="auth-logo">
          <span class="logo-icon">✦</span>
          <span class="logo-text">拾光旅行</span>
        </div>
        <h1>{{ isRegisterMode ? "用户注册" : "用户登录" }}</h1>
        <p>登录后进入主站；管理员也可通过独立后台入口登录。</p>
      </div>

      <el-form :model="form" label-position="top" @submit.prevent>
        <el-form-item label="账号（手机号或邮箱）">
          <el-input v-model="form.account" placeholder="请输入手机号或邮箱" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>
        <el-form-item v-if="isRegisterMode" label="昵称">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>

        <el-button
          type="primary"
          class="auth-submit"
          :loading="submitting"
          @click="submit"
        >
          {{ isRegisterMode ? "注册并登录" : "登录" }}
        </el-button>
      </el-form>

      <div class="auth-footer">
        <button class="link-button" type="button" @click="toggleMode">
          {{ isRegisterMode ? "已有账号？去登录" : "没有账号？去注册" }}
        </button>
        <router-link to="/admin/login">后台登录入口</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { loginApi, registerApi } from "@/api/auth";
import { useAuthStore } from "@/stores/auth";

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const isRegisterMode = ref(false);
const submitting = ref(false);

const form = reactive({
  account: "",
  password: "",
  nickname: "",
});

const toggleMode = () => {
  isRegisterMode.value = !isRegisterMode.value;
};

const submit = async () => {
  if (!form.account || !form.password) {
    ElMessage.warning("请填写账号和密码");
    return;
  }
  if (isRegisterMode.value && !form.nickname) {
    ElMessage.warning("请填写昵称");
    return;
  }

  submitting.value = true;

  try {
    if (isRegisterMode.value) {
      // === 注册流程 ===
      await registerApi({
        account: form.account,
        password: form.password,
        nickname: form.nickname,
      });
      ElMessage.success("注册成功，请登录");
      isRegisterMode.value = false;
      return;
    }

    // === 登录流程 ===
    // 新 API 直接返回 { token, userId, nickname, avatar, roles }
    const loginRes = await loginApi({
      account: form.account,
      password: form.password,
    });

    // 保存登录会话（roles 已由后端返回，自动判断 isAdmin）
    authStore.setSession(loginRes as unknown as Record<string, unknown>);

    ElMessage.success("登录成功");

    // 规范化 redirect 参数（可能为数组）
    let redirect = "/";
    if (route.query.redirect) {
      if (Array.isArray(route.query.redirect)) {
        redirect = route.query.redirect[0] || "/";
      } else {
        redirect = String(route.query.redirect) || "/";
      }
    }

    await router.replace(redirect);
  } catch {
    // 错误已由请求拦截器统一处理
  } finally {
    submitting.value = false;
  }
};
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  position: relative;
  background: linear-gradient(135deg, var(--navy-900), var(--navy-700));
  overflow: hidden;
}

.auth-bg {
  position: absolute;
  inset: 0;
  background-image: radial-gradient(
      circle at 20% 30%,
      rgba(201, 149, 61, 0.06) 0%,
      transparent 40%
    ),
    radial-gradient(
      circle at 80% 70%,
      rgba(212, 122, 98, 0.04) 0%,
      transparent 40%
    );
  pointer-events: none;
}

.auth-card {
  width: min(420px, 100%);
  background: rgba(255, 255, 255, 0.06);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: var(--radius-lg);
  padding: 36px 32px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  position: relative;
  z-index: 1;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.auth-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 8px;
}

.auth-logo .logo-icon {
  font-size: 24px;
  color: var(--gold-500);
}

.auth-logo .logo-text {
  font-family: var(--font-heading);
  font-size: 22px;
  color: var(--white);
  letter-spacing: 1px;
}

.auth-header h1 {
  margin: 0;
  font-size: 26px;
  text-align: center;
  color: var(--white);
}

.auth-header p {
  margin: 8px 0 24px;
  color: rgba(255, 255, 255, 0.45);
  line-height: 1.6;
  text-align: center;
  font-size: 14px;
}

.auth-submit {
  width: 100%;
  background: var(--accent);
  border-color: var(--accent);
}

.auth-submit:hover {
  background: var(--accent-hover);
  border-color: var(--accent-hover);
}

.auth-footer {
  margin-top: 18px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
}

.auth-footer :deep(a),
.link-button {
  background: transparent;
  border: 0;
  color: var(--gold-400);
  cursor: pointer;
  padding: 0;
  font-size: 13px;
  transition: color var(--duration-fast) var(--ease-out);
}

.link-button:hover,
.auth-footer :deep(a:hover) {
  color: var(--gold-300);
}

:deep(.el-form-item__label) {
  color: rgba(255, 255, 255, 0.6) !important;
}

:deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.06) !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
  box-shadow: none !important;
}

:deep(.el-input__inner) {
  color: var(--white) !important;
}

:deep(.el-input__placeholder) {
  color: rgba(255, 255, 255, 0.3) !important;
}

@media (max-width: 720px) {
  .auth-card {
    padding: 28px 20px;
  }
}
</style>
