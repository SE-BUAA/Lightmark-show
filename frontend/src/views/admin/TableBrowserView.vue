<template>
  <div class="admin-page">
    <div class="page-header">
      <h3>操作日志</h3>
    </div>

    <div class="toolbar">
      <el-input
        v-model="filter.operation"
        placeholder="操作类型"
        clearable
        style="width: 160px"
      />
      <el-select
        v-model="filter.result"
        placeholder="结果"
        clearable
        style="width: 110px"
      >
        <el-option label="成功" value="SUCCESS" />
        <el-option label="失败" value="FAIL" />
      </el-select>
      <el-button type="primary" @click="loadData">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <el-table v-loading="loading" :data="rows" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="adminId" label="管理员ID" width="110" />
      <el-table-column prop="operation" label="操作" width="140" />
      <el-table-column
        prop="target"
        label="操作对象"
        min-width="180"
        show-overflow-tooltip
      />
      <el-table-column prop="result" label="结果" width="100">
        <template #default="scope">
          <el-tag
            :type="scope.row.result === 'SUCCESS' ? 'success' : 'danger'"
            effect="dark"
            size="small"
          >
            {{ scope.row.result === "SUCCESS" ? "成功" : "失败" }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="操作时间" width="180" />
    </el-table>

    <div class="table-footer" v-if="total > 0">共 {{ total }} 条</div>
    <div v-else-if="!loading" class="empty-hint">暂无操作日志</div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { getAdminLogs, AdminLogDTO } from "@/api/admin";

const loading = ref(false);
const total = ref(0);
const rows = ref<AdminLogDTO[]>([]);

const filter = reactive({
  operation: "",
  result: "",
});

const loadData = async () => {
  loading.value = true;
  try {
    const data = await getAdminLogs({
      operation: filter.operation || undefined,
      result: filter.result || undefined,
    });
    rows.value = data.list ?? [];
    total.value = data.total ?? 0;
  } finally {
    loading.value = false;
  }
};

const resetSearch = () => {
  filter.operation = "";
  filter.result = "";
  loadData();
};

onMounted(loadData);
</script>
