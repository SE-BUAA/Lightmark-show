<template>
  <div class="profile-page">
    <!-- ── 顶部横幅 ── -->
    <section class="profile-hero">
      <div class="container">
        <div class="profile-header">
          <div class="profile-avatar-wrap">
            <span class="profile-avatar">{{
              displayName.charAt(0) || "?"
            }}</span>
          </div>
          <div class="profile-meta">
            <h1>{{ displayName || "未设置昵称" }}</h1>
            <p class="profile-account">{{ accountInfo }}</p>
            <div class="profile-badges">
              <span class="badge badge-level" v-if="userLevel > 0"
                >Lv.{{ userLevel }}</span
              >
              <span class="badge badge-points">{{ userPoints }} 积分</span>
              <span class="badge badge-admin" v-if="authStore.isAdmin"
                >管理员</span
              >
            </div>
          </div>
        </div>
      </div>
    </section>

    <div class="container profile-body">
      <div class="profile-grid">
        <!-- ── 个人信息卡片 ── -->
        <div class="profile-card">
          <div class="card-header">
            <h3>个人信息</h3>
            <button class="btn-text" @click="editing = !editing">
              {{ editing ? "取消" : "编辑" }}
            </button>
          </div>
          <div class="card-body">
            <template v-if="editing">
              <div class="form-field">
                <label>昵称</label>
                <input
                  v-model="editForm.nickname"
                  type="text"
                  class="form-input"
                  placeholder="请输入昵称"
                />
              </div>
              <div class="form-field">
                <label>手机号</label>
                <input
                  v-model="editForm.phone"
                  type="text"
                  class="form-input"
                  placeholder="请输入手机号"
                />
              </div>
              <div class="form-field">
                <label>邮箱</label>
                <input
                  v-model="editForm.email"
                  type="email"
                  class="form-input"
                  placeholder="请输入邮箱"
                />
              </div>
              <button
                class="btn btn-primary btn-save"
                @click="saveProfile"
                :disabled="saving"
              >
                {{ saving ? "保存中..." : "保存修改" }}
              </button>
            </template>
            <template v-else>
              <div class="info-row">
                <span class="info-label">昵称</span>
                <span class="info-value">{{ displayName || "未设置" }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">手机号</span>
                <span class="info-value">{{ userPhone || "未绑定" }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">邮箱</span>
                <span class="info-value">{{ userEmail || "未绑定" }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">会员等级</span>
                <span class="info-value">{{ levelText }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">积分余额</span>
                <span class="info-value highlight">{{ userPoints }}</span>
              </div>
            </template>
          </div>
        </div>

        <!-- ── 快捷操作 ── -->
        <div class="profile-card">
          <div class="card-header">
            <h3>快捷操作</h3>
          </div>
          <div class="card-body quick-links">
            <router-link
              to="/admin/dashboard"
              class="quick-item"
              v-if="authStore.isAdmin"
            >
              <span class="quick-icon">⚙</span>
              <span>后台管理</span>
            </router-link>
            <router-link to="/flights" class="quick-item">
              <span class="quick-icon">✈</span>
              <span>预订机票</span>
            </router-link>
            <router-link to="/hotels" class="quick-item">
              <span class="quick-icon">🏨</span>
              <span>预订酒店</span>
            </router-link>
            <router-link to="/itinerary" class="quick-item">
              <span class="quick-icon">🗺</span>
              <span>规划行程</span>
            </router-link>
            <button class="quick-item quick-logout" @click="handleLogout">
              <span class="quick-icon">🚪</span>
              <span>退出登录</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { useAuthStore } from "@/stores/auth";
import { logoutApi } from "@/api/auth";
import { getCurrentUser, updateCurrentUser } from "@/api/user";

const router = useRouter();
const authStore = useAuthStore();

const editing = ref(false);
const saving = ref(false);

// 用户资料（后续可从后端拉取完整 UserDTO）
const userPhone = ref("");
const userEmail = ref("");
const userPoints = ref(0);
const userLevel = ref(0);

const displayName = computed(() => authStore.nickname || "");
const accountInfo = computed(() => {
  if (userPhone.value) return userPhone.value;
  if (userEmail.value) return userEmail.value;
  return "用户ID: " + authStore.userId;
});

const levelText = computed(() => {
  const levels = ["普通会员", "银卡会员", "金卡会员", "钻石会员"];
  return levels[userLevel.value] || "普通会员";
});

const editForm = ref({
  nickname: "",
  phone: "",
  email: "",
});

// 获取用户详情 — GET /api/user/current
const fetchUserProfile = async () => {
  if (!authStore.userId) return;
  try {
    const res = await getCurrentUser();
    userPhone.value = res.phone || "";
    userEmail.value = res.email || "";
    userPoints.value = res.points ?? 0;
    userLevel.value = res.level ?? 0;
    editForm.value.nickname = res.nickname || authStore.nickname || "";
    editForm.value.phone = res.phone || "";
    editForm.value.email = res.email || "";
  } catch {
    // 获取失败时使用 auth store 的数据
    editForm.value.nickname = authStore.nickname || "";
  }
};

const saveProfile = async () => {
  if (!editForm.value.nickname.trim()) {
    ElMessage.warning("昵称不能为空");
    return;
  }
  saving.value = true;
  try {
    const res = await updateCurrentUser({
      nickname: editForm.value.nickname,
      phone: editForm.value.phone,
      email: editForm.value.email,
    });
    // 更新本地 store 与 localStorage
    authStore.updateLocalProfile({
      nickname: res.nickname || editForm.value.nickname,
    });
    userPhone.value = res.phone || "";
    userEmail.value = res.email || "";
    ElMessage.success("保存成功");
    editing.value = false;
  } catch {
    ElMessage.error("保存失败，请稍后重试");
  } finally {
    saving.value = false;
  }
};

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm("确定要退出登录吗？", "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "info",
    });
    await logoutApi();
  } catch {
    // 忽略
  }
  authStore.clearSession();
  ElMessage.success("已退出登录");
  router.push("/");
};

onMounted(() => {
  fetchUserProfile();
});
</script>

<style scoped>
.profile-page {
  padding-top: 64px;
}

/* ── Hero ── */
.profile-hero {
  padding: 48px 0;
  background: linear-gradient(135deg, var(--navy-800), var(--navy-600));
  color: var(--white);
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 24px;
}

.profile-avatar-wrap {
  flex-shrink: 0;
}

.profile-avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--gold-500), var(--accent-hover));
  color: var(--white);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
  font-weight: 700;
  box-shadow: 0 0 0 3px rgba(255, 255, 255, 0.15);
}

.profile-meta h1 {
  font-size: 24px;
  font-weight: 700;
  margin: 0;
}

.profile-account {
  color: rgba(255, 255, 255, 0.5);
  font-size: 14px;
  margin-top: 4px;
}

.profile-badges {
  display: flex;
  gap: 8px;
  margin-top: 10px;
  flex-wrap: wrap;
}

.badge {
  padding: 3px 12px;
  border-radius: 100px;
  font-size: 12px;
  font-weight: 600;
}

.badge-level {
  background: rgba(201, 149, 61, 0.2);
  color: var(--gold-400);
}

.badge-points {
  background: rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.7);
}

.badge-admin {
  background: rgba(196, 93, 66, 0.2);
  color: var(--terracotta-400);
}

/* ── Body ── */
.profile-body {
  padding: 32px 24px 60px;
}

.profile-grid {
  display: grid;
  grid-template-columns: 1.5fr 1fr;
  gap: 24px;
  align-items: start;
}

.profile-card {
  background: var(--white);
  border-radius: var(--radius-md);
  border: 1px solid var(--slate-100);
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 24px;
  border-bottom: 1px solid var(--slate-100);
}

.card-header h3 {
  font-size: 16px;
  font-weight: 700;
  color: var(--navy-900);
  margin: 0;
}

.btn-text {
  font-size: 13px;
  color: var(--accent);
  font-weight: 600;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background var(--duration-fast) var(--ease-out);
}

.btn-text:hover {
  background: rgba(201, 149, 61, 0.06);
}

.card-body {
  padding: 20px 24px;
}

/* ── Info rows ── */
.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid var(--slate-100);
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  font-size: 14px;
  color: var(--text-secondary);
}

.info-value {
  font-size: 14px;
  font-weight: 500;
  color: var(--navy-900);
}

.info-value.highlight {
  color: var(--accent);
  font-weight: 700;
}

/* ── Form ── */
.form-field {
  margin-bottom: 16px;
}

.form-field label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 6px;
}

.form-input {
  width: 100%;
  padding: 10px 14px;
  border: 1.5px solid var(--slate-200);
  border-radius: var(--radius-sm);
  font-size: 14px;
  font-family: var(--font-body);
  transition: border-color var(--duration-fast) var(--ease-out);
  outline: none;
  background: var(--cream-50);
}

.form-input:focus {
  border-color: var(--accent);
  box-shadow: 0 0 0 3px rgba(201, 149, 61, 0.1);
}

.btn-save {
  width: 100%;
  margin-top: 8px;
  justify-content: center;
}

/* ── Quick links ── */
.quick-links {
  display: grid;
  gap: 4px;
}

.quick-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  font-size: 14px;
  color: var(--slate-700);
  border-radius: var(--radius-sm);
  transition: all var(--duration-fast) var(--ease-out);
  cursor: pointer;
  width: 100%;
  text-align: left;
  background: transparent;
}

.quick-item:hover {
  color: var(--accent);
  padding-left: 4px;
}

.quick-icon {
  font-size: 18px;
  width: 24px;
  text-align: center;
  flex-shrink: 0;
}

.quick-logout:hover {
  color: var(--cta);
}

/* ── Responsive ── */
@media (max-width: 768px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }

  .profile-header {
    flex-direction: column;
    text-align: center;
  }

  .profile-badges {
    justify-content: center;
  }

  .card-header,
  .card-body {
    padding-left: 16px;
    padding-right: 16px;
  }
}
</style>
