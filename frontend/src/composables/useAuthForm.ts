import { computed, reactive, ref } from "vue";
import type {
  LoginRequest,
  RegisterRequest,
  SendEmailVerificationCodeRequest,
} from "@/api/auth";

const EMAIL_PATTERN = /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i;
const PASSWORD_PATTERN = /^(?=.*[a-z])(?=.*[A-Z])(?=.*[!#@$%^&*()_])[A-Za-z\d!#@$%^&*()_]{8,20}$/;
const COUNTRY_CODE_PATTERN = /^\+?[1-9]\d{0,3}$/;
const PHONE_PATTERN = /^\d{6,15}$/;
const ALLOWED_EMAIL_DOMAINS = new Set([
  "gmail.com",
  "outlook.com",
  "hotmail.com",
  "icloud.com",
  "qq.com",
  "163.com",
  "126.com",
  "sina.com",
  "foxmail.com",
]);

export interface AuthFormState {
  account: string;
  email: string;
  password: string;
  nickname: string;
  countryCode: string;
  phone: string;
  verificationCode: string;
  captchaCode: string;
}

export function useAuthForm() {
  const isRegisterMode = ref(false);
  const submitting = ref(false);
  const sendingCode = ref(false);
  const cooldownSeconds = ref(0);
  const captchaNonce = ref(Date.now());

  const form = reactive<AuthFormState>({
    account: "",
    email: "",
    password: "",
    nickname: "",
    countryCode: "+86",
    phone: "",
    verificationCode: "",
    captchaCode: "",
  });

  const captchaUrl = computed(() => `/api/auth/captcha?t=${captchaNonce.value}`);
  const sendButtonText = computed(() => {
    if (cooldownSeconds.value > 0) {
      return `重新发送 ${cooldownSeconds.value}s`;
    }
    return "发送验证码";
  });

  const normalizedAccount = computed(() => form.account.trim());
  const normalizedNickname = computed(() => form.nickname.trim());
  const normalizedEmail = computed(() => form.email.trim().toLowerCase());
  const normalizedCountryCode = computed(() => form.countryCode.trim());
  const normalizedPhone = computed(() => form.phone.trim().replace(/[\s-]/g, ""));
  const normalizedCaptchaCode = computed(() => form.captchaCode.trim());
  const normalizedVerificationCode = computed(() => form.verificationCode.trim());

  const emailError = computed(() => {
    if (!normalizedEmail.value) {
      return "";
    }
    if (!EMAIL_PATTERN.test(normalizedEmail.value)) {
      return "请输入正确的邮箱格式";
    }
    const domain = normalizedEmail.value.split("@")[1] || "";
    if (ALLOWED_EMAIL_DOMAINS.has(domain) || domain.endsWith(".edu.cn")) {
      return "";
    }
    return "仅支持常用邮箱及 .edu.cn 邮箱";
  });

  const phoneError = computed(() => {
    if (!normalizedPhone.value) {
      return "";
    }
    if (!COUNTRY_CODE_PATTERN.test(normalizedCountryCode.value)) {
      return "区号格式不正确";
    }
    if (!PHONE_PATTERN.test(normalizedPhone.value)) {
      return "手机号格式不正确";
    }
    return "";
  });

  const passwordError = computed(() => {
    if (!form.password) {
      return "";
    }
    return PASSWORD_PATTERN.test(form.password)
      ? ""
      : "密码需为 8-20 位，且包含大小写字母和特殊字符 !#@$%^&*()_";
  });

  const nicknameError = computed(() => {
    if (!isRegisterMode.value) {
      return "";
    }
    if (!normalizedNickname.value) {
      return "";
    }
    return normalizedNickname.value.length <= 30
      ? ""
      : "昵称最长 30 个字符";
  });

  const canSendCode = computed(() => {
    if (!isRegisterMode.value) {
      return false;
    }
    if (sendingCode.value || cooldownSeconds.value > 0 || !normalizedCaptchaCode.value) {
      return false;
    }
    return Boolean(normalizedEmail.value) && !emailError.value;
  });

  const toggleMode = () => {
    isRegisterMode.value = !isRegisterMode.value;
    form.captchaCode = "";
    form.verificationCode = "";
    refreshCaptcha();
  };

  const refreshCaptcha = () => {
    captchaNonce.value = Date.now();
  };

  const startCooldown = (seconds: number) => {
    cooldownSeconds.value = seconds;
    const timer = window.setInterval(() => {
      cooldownSeconds.value -= 1;
      if (cooldownSeconds.value <= 0) {
        window.clearInterval(timer);
      }
    }, 1000);
  };

  const validateBeforeSubmit = () => {
    if (!normalizedCaptchaCode.value) {
      return "请填写图形验证码";
    }
    if (!normalizedAccount.value && !isRegisterMode.value) {
      return "请填写邮箱或昵称";
    }
    if (!form.password) {
      return isRegisterMode.value ? "请填写密码" : "请填写登录密码";
    }
    if (passwordError.value) {
      return passwordError.value;
    }
    if (!isRegisterMode.value) {
      return "";
    }
    if (!normalizedEmail.value) {
      return "请填写邮箱";
    }
    if (emailError.value) {
      return emailError.value;
    }
    if (!normalizedNickname.value) {
      return "请填写昵称";
    }
    if (nicknameError.value) {
      return nicknameError.value;
    }
    if (!normalizedVerificationCode.value) {
      return "请填写注册邮箱验证码";
    }
    if (normalizedPhone.value) {
      return phoneError.value;
    }
    return "";
  };

  const buildLoginPayload = (privacyAccepted: boolean): LoginRequest => ({
    account: normalizedAccount.value,
    password: form.password,
    captchaCode: normalizedCaptchaCode.value,
    privacyAccepted,
  });

  const buildRegisterPayload = (privacyAccepted: boolean): RegisterRequest => ({
    email: normalizedEmail.value,
    nickname: normalizedNickname.value,
    countryCode: normalizedPhone.value ? normalizedCountryCode.value : "",
    phone: normalizedPhone.value,
    password: form.password,
    verificationCode: normalizedVerificationCode.value,
    captchaCode: normalizedCaptchaCode.value,
    privacyAccepted,
  });

  const buildSendRegisterEmailPayload = (): SendEmailVerificationCodeRequest => ({
    email: normalizedEmail.value,
    captchaCode: normalizedCaptchaCode.value,
  });

  return {
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
  };
}
