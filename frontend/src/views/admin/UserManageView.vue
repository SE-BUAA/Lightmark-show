<template>
  <div class="admin-page">
    <div class="toolbar">
      <el-input
        v-model="keyword"
        placeholder="手机号/邮箱/昵称"
        style="width: 200px"
        clearable
        @keyup.enter="loadData"
      />
      <el-select
        v-model="statusFilter"
        placeholder="状态"
        clearable
        style="width: 110px"
        @change="loadData"
      >
        <el-option label="正常" :value="0" />
        <el-option label="封禁" :value="1" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <el-table v-loading="loading" :data="rows" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="nickname" label="昵称" width="130" />
      <el-table-column prop="phone" label="手机号" width="140" />
      <el-table-column prop="email" label="邮箱" min-width="180" />
      <el-table-column prop="level" label="等级" width="80">
        <template #default="scope">
          <el-tag size="small" effect="plain">Lv.{{ scope.row.level }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="register_source" label="来源" width="110" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="scope">
          <el-tag
            :type="scope.row.status === 0 ? 'success' : 'danger'"
            effect="dark"
            size="small"
          >
            {{ scope.row.status === 0 ? "正常" : "封禁" }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="create_time" label="注册时间" min-width="170" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="scope">
          <el-button size="small" plain @click="openLevelDialog(scope.row)"
            >改等级</el-button
          >
          <el-button
            v-if="scope.row.status === 0"
            size="small"
            type="danger"
            plain
            @click="toggleStatus(scope.row.id, 1)"
            >封禁</el-button
          >
          <el-button
            v-else
            size="small"
            type="success"
            plain
            @click="toggleStatus(scope.row.id, 0)"
            >解封</el-button
          >
        </template>
      </el-table-column>
    </el-table>

    <div class="table-footer" v-if="total > 0">
      <span>共 {{ total }} 条</span>
      <el-pagination
        v-if="total > pageSize"
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        background
        layout="prev, pager, next, jumper, total"
        @current-change="loadData"
      />
    </div>

    <!-- 改等级对话框 -->
    <el-dialog v-model="levelDialog.visible" title="调整会员等级" width="400px">
      <el-form label-width="80px">
        <el-form-item label="用户昵称">{{ levelDialog.nickname }}</el-form-item>
        <el-form-item label="当前等级"
          >Lv.{{ levelDialog.oldLevel }}</el-form-item
        >
        <el-form-item label="新等级">
          <el-select v-model="levelDialog.newLevel" style="width: 100%">
            <el-option label="普通会员 (Lv.0)" :value="0" />
            <el-option label="银卡会员 (Lv.1)" :value="1" />
            <el-option label="金卡会员 (Lv.2)" :value="2" />
            <el-option label="钻石会员 (Lv.3)" :value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="levelDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="confirmLevel"
          >确认</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import {
  UserDTO,
  getAdminUsers,
  updateUserStatus,
  updateUserLevel,
} from "@/api/admin";

const keyword = ref("");
const statusFilter = ref<number | undefined>(undefined);
const currentPage = ref(1);
const pageSize = 100;
const loading = ref(false);
const saving = ref(false);
const total = ref(0);
const rows = ref<UserDTO[]>([]);

const loadData = async () => {
  loading.value = true;
  try {
    const data = await getAdminUsers({
      keyword: keyword.value || undefined,
      status: statusFilter.value,
      page: currentPage.value,
      size: pageSize,
    });
    rows.value = data.list ?? [];
    total.value = data.total ?? 0;
  } finally {
    loading.value = false;
  }
};

const resetSearch = async () => {
  keyword.value = "";
  statusFilter.value = undefined;
  currentPage.value = 1;
  await loadData();
};

const handleSearch = async () => {
  currentPage.value = 1;
  await loadData();
};

// ── 封禁/解封 ──
const toggleStatus = async (id: number, status: number) => {
  await updateUserStatus(id, status);
  ElMessage.success(status === 0 ? "已解封" : "已封禁");
  await loadData();
};

// ── 改等级 ──
const levelDialog = reactive({
  visible: false,
  userId: 0,
  nickname: "",
  oldLevel: 0,
  newLevel: 0,
});

const openLevelDialog = (row: UserDTO) => {
  levelDialog.userId = row.id;
  levelDialog.nickname = row.nickname;
  levelDialog.oldLevel = row.level;
  levelDialog.newLevel = row.level;
  levelDialog.visible = true;
};

const confirmLevel = async () => {
  saving.value = true;
  try {
    await updateUserLevel(levelDialog.userId, levelDialog.newLevel);
    ElMessage.success("会员等级已更新");
    levelDialog.visible = false;
    await loadData();
  } finally {
    saving.value = false;
  }
};

onMounted(loadData);
</script>
