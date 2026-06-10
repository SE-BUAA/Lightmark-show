<script setup lang="ts">
import { computed, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import {
  loginApi,
  registerApi,
  sendEmailVerificationCodeApi,
} from "@/api/auth";
import CaptchaField from "@/components/auth/CaptchaField.vue";
import VerificationCodeField from "@/components/auth/VerificationCodeField.vue";
import { useAuthForm } from "@/composables/useAuthForm";
import { useAuthStore } from "@/stores/auth";

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const privacyAccepted = ref(false);

const {
  form,
  isRegisterMode,
  submitting,
  sendingCode,
  captchaUrl,
  sendButtonText,
  passwordError,
  nicknameError,
  emailError,
  phoneError,
  canSendCode,
  toggleMode,
  refreshCaptcha,
  startCooldown,
  validateBeforeSubmit,
  buildLoginPayload,
  buildRegisterPayload,
  buildSendRegisterEmailPayload,
} = useAuthForm();

const title = computed(() => (isRegisterMode.value ? "用户注册" : "用户登录"));
const submitLabel = computed(() => (isRegisterMode.value ? "注册并登录" : "登录"));

const openPrivacyPolicy = () => {
  const href = router.resolve({ path: "/privacy-policy" }).href;
  window.open(href, "_blank", "noopener");
};

const switchMode = () => {
  toggleMode();
  privacyAccepted.value = false;
};

const submit = async () => {
  const validationMessage = validateBeforeSubmit();
  if (validationMessage) {
    ElMessage.warning(validationMessage);
    return;
  }
  if (isRegisterMode.value && !privacyAccepted.value) {
    ElMessage.warning("请先阅读并同意隐私政策");
    return;
  }
  if (!isRegisterMode.value && !privacyAccepted.value) {
    ElMessage.warning("登录前请先阅读并同意隐私政策");
    return;
  }

  submitting.value = true;
  try {
    if (isRegisterMode.value) {
      await registerApi(buildRegisterPayload(privacyAccepted.value));
      ElMessage.success("注册成功，请登录");
      isRegisterMode.value = false;
      privacyAccepted.value = false;
      form.verificationCode = "";
      form.captchaCode = "";
      refreshCaptcha();
      return;
    }

    const loginRes = await loginApi(buildLoginPayload(privacyAccepted.value));
    authStore.setSession(loginRes as unknown as Record<string, unknown>);
    ElMessage.success("登录成功");

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
    refreshCaptcha();
  } finally {
    submitting.value = false;
  }
};

const sendVerificationCode = async () => {
  if (!canSendCode.value) {
    ElMessage.warning("请先填写邮箱和图形验证码");
    return;
  }

  sendingCode.value = true;
  try {
    await sendEmailVerificationCodeApi(buildSendRegisterEmailPayload());
    ElMessage.success("验证码已发送到邮箱");
    startCooldown(60);
  } catch {
    refreshCaptcha();
  } finally {
    sendingCode.value = false;
  }
};
</script>

<template>
  <div class="auth-page">
    <div class="auth-bg"></div>
    <div class="auth-card">
      <div class="auth-header">
        <div class="auth-logo">
          <span class="logo-icon">✦</span>
          <span class="logo-text">拾光旅行</span>
        </div>
        <h1>{{ title }}</h1>
        <p>登录支持邮箱或昵称 + 密码。注册时必须完成邮箱验证，手机号仅注册时选填。</p>
      </div>

      <el-form label-position="top" @submit.prevent>
        <el-form-item v-if="!isRegisterMode" label="登录账号（邮箱或昵称）">
          <el-input v-model="form.account" placeholder="请输入邮箱或昵称" />
        </el-form-item>

        <el-form-item v-if="isRegisterMode" label="邮箱">
          <el-input v-model="form.email" placeholder="请输入常用邮箱或 .edu.cn 邮箱" />
          <div v-if="emailError" class="field-error">{{ emailError }}</div>
        </el-form-item>

        <el-form-item v-if="isRegisterMode" label="昵称">
          <el-input v-model="form.nickname" maxlength="30" show-word-limit placeholder="请输入昵称" />
          <div v-if="nicknameError" class="field-error">{{ nicknameError }}</div>
        </el-form-item>

        <el-form-item label="密码">
          <el-input
            v-model="form.password"
            type="password"
            :placeholder="isRegisterMode ? '请输入密码' : '请输入登录密码'"
            show-password
          />
          <div v-if="passwordError" class="field-error">{{ passwordError }}</div>
          <div class="field-hint">密码需为 8-20 位，且包含大小写字母和特殊字符 !#@$%^&amp;*()_</div>
        </el-form-item>

        <el-form-item v-if="isRegisterMode" label="手机号（选填）">
          <div class="phone-row">
            <el-input v-model="form.countryCode" class="country-code" placeholder="+86" />
            <el-input v-model="form.phone" placeholder="请输入手机号（可不填）" />
          </div>
          <div v-if="phoneError" class="field-error">{{ phoneError }}</div>
        </el-form-item>

        <el-form-item label="图形验证码">
          <CaptchaField v-model="form.captchaCode" :image-url="captchaUrl" @refresh="refreshCaptcha" />
        </el-form-item>

        <el-form-item v-if="isRegisterMode" label="注册邮箱验证码">
          <VerificationCodeField
            v-model="form.verificationCode"
            :button-text="sendButtonText"
            :disabled="!canSendCode"
            :loading="sendingCode"
            @send="sendVerificationCode"
          />
        </el-form-item>

        <el-form-item class="consent-item">
          <el-checkbox v-model="privacyAccepted">
            我已阅读并同意
            <button type="button" class="policy-link-button" @click.stop="openPrivacyPolicy">
              《隐私政策》
            </button>
          </el-checkbox>
        </el-form-item>

        <el-button
          type="primary"
          class="auth-submit"
          :loading="submitting"
          @click="submit"
        >
          {{ submitLabel }}
        </el-button>
      </el-form>

      <div class="auth-footer">
        <button class="link-button" type="button" @click="switchMode">
          {{ isRegisterMode ? "已有账号？去登录" : "没有账号？去注册" }}
        </button>
        <router-link to="/admin/login">后台登录入口</router-link>
      </div>
    </div>
  </div>
</template>

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
  width: min(480px, 100%);
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
  gap: 12px;
  flex-wrap: wrap;
}

.link-button {
  background: transparent;
  border: none;
  color: var(--white);
  cursor: pointer;
  padding: 0;
}

.consent-item {
  margin-top: 4px;
}

.consent-item :deep(.el-checkbox) {
  align-items: flex-start;
  line-height: 1.6;
}

.consent-item :deep(.el-checkbox__label) {
  color: rgba(255, 255, 255, 0.82);
  white-space: normal;
}

.policy-link-button {
  display: inline;
  background: transparent;
  border: none;
  color: var(--gold-400);
  cursor: pointer;
  padding: 0;
  font: inherit;
}

.policy-link-button:hover {
  color: var(--gold-300);
}

.field-error {
  margin-top: 6px;
  color: #ffb4b4;
  font-size: 12px;
}

.field-hint {
  margin-top: 6px;
  color: rgba(255, 255, 255, 0.52);
  font-size: 12px;
}

.phone-row {
  display: grid;
  grid-template-columns: 100px minmax(0, 1fr);
  gap: 12px;
}

.country-code {
  width: 100%;
}
</style>
