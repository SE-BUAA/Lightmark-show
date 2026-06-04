<template>
  <div class="profile-page">
    <!-- ── 顶部横幅 ── -->
    <section class="profile-hero">
      <div class="container">
        <div class="profile-header">
          <div class="profile-avatar-wrap">
            <button class="avatar-button" @click="triggerAvatarUpload">
              <img v-if="avatarUrl" :src="avatarUrl" class="profile-avatar-img" />
              <span v-else class="profile-avatar">{{
                displayName.charAt(0) || "?"
              }}</span>
            </button>
            <input
              ref="avatarInput"
              class="avatar-input"
              type="file"
              accept="image/*"
              @change="handleAvatarSelected"
            />
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
                <label>性别</label>
                <select v-model="editForm.gender" class="form-input">
                  <option :value="0">未设置</option>
                  <option :value="1">男</option>
                  <option :value="2">女</option>
                </select>
              </div>
              <div class="form-field">
                <label>出生日期</label>
                <input v-model="editForm.birth_date" type="date" class="form-input" />
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
                <span class="info-label">性别</span>
                <span class="info-value">{{ genderText }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">出生日期</span>
                <span class="info-value">{{ userBirthDate || "未设置" }}</span>
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
            <router-link to="/hotels/orders" class="quick-item">
              <span class="quick-icon">单</span>
              <span>酒店订单</span>
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

      <div class="profile-grid profile-grid-2">
        <div class="profile-card">
          <div class="card-header">
            <h3>自然语言修改个人信息</h3>
          </div>
          <div class="card-body">
            <div class="form-field">
              <label>输入指令</label>
              <input
                v-model="nlText"
                type="text"
                class="form-input"
                placeholder="例如：把昵称改成小李"
              />
            </div>
            <button class="btn btn-secondary btn-save" @click="parseNlUpdate">
              解析
            </button>
            <div v-if="nlPreview" class="nl-preview">{{ nlPreview }}</div>
            <button
              v-if="nlParsed"
              class="btn btn-primary btn-save"
              @click="confirmNlUpdate"
              :disabled="nlSaving"
            >
              {{ nlSaving ? "提交中..." : "确认修改" }}
            </button>
          </div>
        </div>
      </div>

      <div class="profile-grid profile-grid-2">
        <div class="profile-card">
          <div class="card-header">
            <h3>常用出行人</h3>
            <button class="btn-text" @click="openCreateTraveler">新增</button>
          </div>
          <div class="card-body">
            <div v-if="!travelers.length" class="empty">暂无出行人</div>
            <div v-else class="list">
              <div class="list-item" v-for="t in travelers" :key="t.id">
                <div class="list-main">
                  <div class="list-title">{{ t.name }}</div>
                  <div class="list-sub">
                    {{ t.id_card }} {{ t.phone || "" }}
                  </div>
                </div>
                <div class="list-actions">
                  <button class="btn-text" @click="openEditTraveler(t)">
                    编辑
                  </button>
                  <button class="btn-text danger" @click="removeTraveler(t)">
                    删除
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="profile-card">
          <div class="card-header">
            <h3>积分与会员</h3>
          </div>
          <div class="card-body">
            <div class="info-row" v-if="upgradeInfo">
              <span class="info-label">当前等级</span>
              <span class="info-value">Lv.{{ upgradeInfo.level }}</span>
            </div>
            <div class="info-row" v-if="upgradeInfo">
              <span class="info-label">距离升级所需积分</span>
              <span class="info-value">{{ upgradeInfo.pointsNeeded }}</span>
            </div>
            <div class="divider"></div>
            <div v-if="!pointsLogs.length" class="empty">暂无积分明细</div>
            <div v-else class="list">
              <div class="list-item" v-for="p in pointsLogs" :key="p.id">
                <div class="list-main">
                  <div class="list-title">
                    类型: {{ p.type }} 变动: {{ p.amount }}
                  </div>
                  <div class="list-sub">
                    {{ p.source || "" }} {{ p.create_time || "" }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="profile-card">
          <div class="card-header">
            <h3>我的订单</h3>
          </div>
          <div class="card-body">
            <div v-if="!orders.length" class="empty">暂无订单</div>
            <div v-else class="list">
              <div class="list-item" v-for="o in orders" :key="o.order_no">
                <div class="list-main">
                  <div class="list-title">{{ o.order_no }}</div>
                  <div class="list-sub">
                    {{ o.order_type }} ¥{{ o.pay_amount }}
                    {{ o.create_time || "" }}
                  </div>
                </div>
                <div class="list-actions">
                  <span class="status">状态: {{ o.status }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="profile-card">
          <div class="card-header">
            <h3>安全设置</h3>
          </div>
          <div class="card-body">
            <div class="form-field">
              <label>旧密码</label>
              <input
                v-model="passwordForm.oldPassword"
                type="password"
                class="form-input"
                placeholder="请输入旧密码"
              />
            </div>
            <div class="form-field">
              <label>新密码</label>
              <input
                v-model="passwordForm.newPassword"
                type="password"
                class="form-input"
                placeholder="请输入新密码"
              />
            </div>
            <div class="form-field">
              <label>确认新密码</label>
              <input
                v-model="passwordForm.confirmPassword"
                type="password"
                class="form-input"
                placeholder="请再次输入新密码"
              />
            </div>
            <button
              class="btn btn-primary btn-save"
              @click="handleChangePassword"
              :disabled="passwordSaving"
            >
              {{ passwordSaving ? "提交中..." : "修改密码" }}
            </button>
          </div>
        </div>
      </div>

      <el-dialog v-model="travelerDialogOpen" title="出行人" width="420px">
        <div class="form-field">
          <label>姓名</label>
          <input
            v-model="travelerForm.name"
            type="text"
            class="form-input"
            placeholder="请输入姓名"
          />
        </div>
        <div class="form-field">
          <label>证件号</label>
          <input
            v-model="travelerForm.id_card"
            type="text"
            class="form-input"
            placeholder="请输入证件号"
          />
        </div>
        <div class="form-field">
          <label>手机号</label>
          <input
            v-model="travelerForm.phone"
            type="text"
            class="form-input"
            placeholder="可选"
          />
        </div>
        <template #footer>
          <button class="btn btn-secondary" @click="travelerDialogOpen = false">
            取消
          </button>
          <button
            class="btn btn-primary"
            @click="saveTraveler"
            :disabled="travelerSaving"
          >
            {{ travelerSaving ? "保存中..." : "保存" }}
          </button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { useAuthStore } from "@/stores/auth";
import { logoutApi } from "@/api/auth";
import {
  addTraveler,
  changePassword,
  deleteTraveler,
  getCurrentUser,
  getLevelUpgradeInfo,
  getPointsLogs,
  getTravelers,
  getUserOrders,
  uploadAvatarFile,
  updateCurrentUser,
  updateTraveler,
} from "@/api/user";
import type {
  OrderDTO,
  PointsLogDTO,
  TravelerDTO,
  UserLevelUpgradeInfoDTO,
} from "@/api/user";

const router = useRouter();
const authStore = useAuthStore();

const editing = ref(false);
const saving = ref(false);

// 用户资料（后续可从后端拉取完整 UserDTO）
const userPhone = ref("");
const userEmail = ref("");
const userPoints = ref(0);
const userLevel = ref(0);
const userGender = ref(0);
const userBirthDate = ref("");
const avatarUrl = ref("");

const displayName = computed(() => authStore.nickname || "");
const accountInfo = computed(() => {
  if (userPhone.value) return userPhone.value;
  if (userEmail.value) return userEmail.value;
  const raw = authStore.userId || "";
  const formatted =
    raw && /^\d+$/.test(raw) ? raw.padStart(16, "0").slice(-16) : raw;
  return "用户ID: " + formatted;
});

const levelText = computed(() => {
  const levels = ["普通会员", "银卡会员", "金卡会员", "钻石会员"];
  return levels[userLevel.value] || "普通会员";
});

const genderText = computed(() => {
  if (userGender.value === 1) return "男";
  if (userGender.value === 2) return "女";
  return "未设置";
});

const editForm = ref({
  nickname: "",
  gender: 0,
  birth_date: "",
  phone: "",
  email: "",
});

const travelers = ref<TravelerDTO[]>([]);
const orders = ref<OrderDTO[]>([]);
const pointsLogs = ref<PointsLogDTO[]>([]);
const upgradeInfo = ref<UserLevelUpgradeInfoDTO | null>(null);

const travelerDialogOpen = ref(false);
const travelerSaving = ref(false);
const travelerForm = ref({
  id: "",
  name: "",
  id_card: "",
  phone: "",
});

const passwordForm = ref({
  oldPassword: "",
  newPassword: "",
  confirmPassword: "",
});
const passwordSaving = ref(false);

const avatarInput = ref<HTMLInputElement | null>(null);
const avatarUploading = ref(false);

const triggerAvatarUpload = () => {
  avatarInput.value?.click();
};

const handleAvatarSelected = async (e: Event) => {
  const input = e.target as HTMLInputElement;
  const file = input.files?.[0];
  input.value = "";
  if (!file) return;
  avatarUploading.value = true;
  try {
    const res = await uploadAvatarFile(file);
    avatarUrl.value = res.avatarUrl || "";
    authStore.updateLocalProfile({ avatar: avatarUrl.value });
    ElMessage.success("头像已更新");
  } catch {
    ElMessage.error("头像上传失败");
  } finally {
    avatarUploading.value = false;
  }
};

type NlParsed = { field: "nickname" | "email" | "phone"; value: string };
const nlText = ref("");
const nlParsed = ref<NlParsed | null>(null);
const nlPreview = ref("");
const nlSaving = ref(false);

const formatUserId16 = () => {
  const raw = authStore.userId || "";
  return raw && /^\d+$/.test(raw) ? raw.padStart(16, "0").slice(-16) : raw;
};

const parseNlUpdate = () => {
  const text = nlText.value.trim();
  nlParsed.value = null;
  nlPreview.value = "";
  if (!text) return;
  const nicknameMatch = text.match(/昵称.*?(改成|改为|修改为)\s*([^\s，。]+)$/);
  const emailMatch = text.match(/邮箱.*?(改成|改为|修改为)\s*([^\s，。]+)$/);
  const phoneMatch = text.match(/(手机号|手机).*?(改成|改为|修改为)\s*(\d{6,})$/);

  if (nicknameMatch) {
    nlParsed.value = { field: "nickname", value: nicknameMatch[2] };
    nlPreview.value = `将用户ID ${formatUserId16()} 的昵称修改为：${nicknameMatch[2]}`;
    return;
  }
  if (emailMatch) {
    nlParsed.value = { field: "email", value: emailMatch[2] };
    nlPreview.value = `将用户ID ${formatUserId16()} 的邮箱修改为：${emailMatch[2]}`;
    return;
  }
  if (phoneMatch) {
    nlParsed.value = { field: "phone", value: phoneMatch[3] };
    nlPreview.value = `将用户ID ${formatUserId16()} 的手机号修改为：${phoneMatch[3]}`;
    return;
  }
  nlPreview.value = "未识别到可修改字段，目前支持昵称/邮箱/手机号";
};

const confirmNlUpdate = async () => {
  if (!nlParsed.value) return;
  nlSaving.value = true;
  try {
    const payload: Record<string, unknown> = {};
    payload[nlParsed.value.field] = nlParsed.value.value;
    const res = await updateCurrentUser(payload);
    authStore.updateLocalProfile({
      nickname: res.nickname,
      avatar: res.avatar,
    });
    await fetchUserProfile();
    nlText.value = "";
    nlParsed.value = null;
    nlPreview.value = "";
    ElMessage.success("修改成功");
  } catch {
    ElMessage.error("修改失败");
  } finally {
    nlSaving.value = false;
  }
};

// 获取用户详情 — GET /api/user/current
const fetchUserProfile = async () => {
  if (!authStore.userId) return;
  try {
    const res = await getCurrentUser();
    userPhone.value = res.phone || "";
    userEmail.value = res.email || "";
    userPoints.value = res.points ?? 0;
    userLevel.value = res.level ?? 0;
    userGender.value = res.gender ?? 0;
    userBirthDate.value = res.birth_date || "";
    avatarUrl.value = res.avatar || "";
    editForm.value.nickname = res.nickname || authStore.nickname || "";
    editForm.value.gender = res.gender ?? 0;
    editForm.value.birth_date = res.birth_date || "";
    editForm.value.phone = res.phone || "";
    editForm.value.email = res.email || "";
    authStore.updateLocalProfile({ avatar: res.avatar || "" });
  } catch {
    // 获取失败时使用 auth store 的数据
    editForm.value.nickname = authStore.nickname || "";
  }
};

const fetchTravelers = async () => {
  try {
    travelers.value = await getTravelers();
  } catch {
    travelers.value = [];
  }
};

const fetchPointsLogs = async () => {
  try {
    const res = await getPointsLogs();
    pointsLogs.value = res.list || [];
  } catch {
    pointsLogs.value = [];
  }
};

const fetchUpgradeInfo = async () => {
  try {
    upgradeInfo.value = await getLevelUpgradeInfo();
  } catch {
    upgradeInfo.value = null;
  }
};

const fetchOrders = async () => {
  try {
    const res = await getUserOrders();
    orders.value = res.list || [];
  } catch {
    orders.value = [];
  }
};

const openCreateTraveler = () => {
  travelerForm.value = { id: "", name: "", id_card: "", phone: "" };
  travelerDialogOpen.value = true;
};

const openEditTraveler = (t: TravelerDTO) => {
  travelerForm.value = {
    id: t.id || "",
    name: t.name || "",
    id_card: t.id_card || "",
    phone: t.phone || "",
  };
  travelerDialogOpen.value = true;
};

const saveTraveler = async () => {
  if (!travelerForm.value.name.trim() || !travelerForm.value.id_card.trim()) {
    ElMessage.warning("请填写姓名与证件号");
    return;
  }
  travelerSaving.value = true;
  try {
    if (travelerForm.value.id) {
      await updateTraveler(travelerForm.value.id, {
        id: travelerForm.value.id,
        name: travelerForm.value.name,
        id_card: travelerForm.value.id_card,
        phone: travelerForm.value.phone,
      });
    } else {
      await addTraveler({
        name: travelerForm.value.name,
        id_card: travelerForm.value.id_card,
        phone: travelerForm.value.phone,
      });
    }
    travelerDialogOpen.value = false;
    await fetchTravelers();
    ElMessage.success("保存成功");
  } catch {
    ElMessage.error("保存失败");
  } finally {
    travelerSaving.value = false;
  }
};

const removeTraveler = async (t: TravelerDTO) => {
  try {
    await ElMessageBox.confirm("确定删除该出行人吗？", "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    });
    await deleteTraveler(Number(t.id));
    await fetchTravelers();
    ElMessage.success("已删除");
  } catch {
    // TODO
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
      gender: editForm.value.gender,
      birth_date: editForm.value.birth_date || undefined,
    });
    // 更新本地 store 与 localStorage
    authStore.updateLocalProfile({
      nickname: res.nickname || editForm.value.nickname,
      avatar: res.avatar,
    });
    userPhone.value = res.phone || "";
    userEmail.value = res.email || "";
    userGender.value = res.gender ?? 0;
    userBirthDate.value = res.birth_date || "";
    avatarUrl.value = res.avatar || "";
    ElMessage.success("保存成功");
    editing.value = false;
  } catch {
    ElMessage.error("保存失败，请稍后重试");
  } finally {
    saving.value = false;
  }
};

const handleChangePassword = async () => {
  if (!passwordForm.value.oldPassword || !passwordForm.value.newPassword) {
    ElMessage.warning("请填写旧密码与新密码");
    return;
  }
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    ElMessage.warning("两次新密码不一致");
    return;
  }
  passwordSaving.value = true;
  try {
    await changePassword({
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword,
    });
    passwordForm.value = {
      oldPassword: "",
      newPassword: "",
      confirmPassword: "",
    };
    ElMessage.success("密码修改成功");
  } catch {
    ElMessage.error("密码修改失败");
  } finally {
    passwordSaving.value = false;
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
  fetchTravelers();
  fetchPointsLogs();
  fetchUpgradeInfo();
  fetchOrders();
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

.avatar-button {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  overflow: hidden;
  padding: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.avatar-input {
  display: none;
}

.profile-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
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

.profile-grid-2 {
  margin-top: 24px;
  grid-template-columns: 1fr 1fr;
}

.nl-preview {
  margin-top: 12px;
  font-size: 13px;
  color: var(--slate-700);
}

.empty {
  color: var(--text-secondary);
  font-size: 13px;
  padding: 10px 0;
}

.list {
  display: grid;
  gap: 10px;
}

.list-item {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 12px;
  border-radius: 12px;
  border: 1px solid var(--slate-200);
  background: var(--white);
}

.list-title {
  font-weight: 700;
  color: var(--navy-900);
  font-size: 14px;
}

.list-sub {
  margin-top: 4px;
  font-size: 12px;
  color: var(--text-secondary);
}

.list-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.btn-text.danger {
  color: var(--cta);
}

.divider {
  height: 1px;
  background: var(--slate-100);
  margin: 14px 0;
}

.status {
  font-size: 12px;
  color: var(--text-secondary);
}

/* ── Responsive ── */
@media (max-width: 768px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }

  .profile-grid-2 {
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
