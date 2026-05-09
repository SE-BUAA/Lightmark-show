<template>
  <div class="admin-page">
    <div class="toolbar">
      <el-select
        v-model="statusFilter"
        placeholder="全部状态"
        clearable
        style="width: 200px"
        @change="loadData"
      >
        <el-option label="待支付" :value="0" />
        <el-option label="已支付" :value="1" />
        <el-option label="已出行" :value="2" />
        <el-option label="已取消" :value="3" />
        <el-option label="退款中" :value="4" />
      </el-select>
      <el-button type="primary" @click="loadData">搜索</el-button>
      <el-button
        @click="
          statusFilter = undefined;
          loadData();
        "
        >重置</el-button
      >
    </div>

    <el-table v-loading="loading" :data="rows" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="order_no" label="订单号" width="170" />
      <el-table-column prop="order_type" label="类型" width="90" />
      <el-table-column prop="user_id" label="用户ID" width="80" />
      <el-table-column prop="pay_amount" label="实付金额" width="110">
        <template #default="scope"
          >￥{{ Number(scope.row.pay_amount).toFixed(2) }}</template
        >
      </el-table-column>
      <el-table-column prop="payment_method" label="支付方式" width="110" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag
            :type="statusTagType(scope.row.status)"
            effect="dark"
            size="small"
          >
            {{ statusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="create_time" label="创建时间" min-width="170" />
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="scope">
          <el-button size="small" plain @click="openStatusDialog(scope.row)"
            >改状态</el-button
          >
          <el-button
            size="small"
            type="danger"
            plain
            @click="openRefundDialog(scope.row)"
            :disabled="scope.row.status === 3"
            >退款</el-button
          >
        </template>
      </el-table-column>
    </el-table>

    <div class="table-footer" v-if="total > 0">共 {{ total }} 条</div>

    <!-- 改状态对话框 -->
    <el-dialog
      v-model="statusDialog.visible"
      title="修改订单状态"
      width="420px"
    >
      <el-form label-width="90px">
        <el-form-item label="订单号">{{ statusDialog.orderNo }}</el-form-item>
        <el-form-item label="当前状态">{{
          statusText(statusDialog.currentStatus)
        }}</el-form-item>
        <el-form-item label="新状态">
          <el-select v-model="statusDialog.newStatus" style="width: 100%">
            <el-option label="待支付" :value="0" />
            <el-option label="已支付" :value="1" />
            <el-option label="已出行" :value="2" />
            <el-option label="已取消" :value="3" />
            <el-option label="退款中" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="statusDialog.remark"
            type="textarea"
            :rows="2"
            placeholder="可选填写备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="confirmStatus"
          >确认</el-button
        >
      </template>
    </el-dialog>

    <!-- 退款对话框 -->
    <el-dialog v-model="refundDialog.visible" title="强制退款" width="420px">
      <el-form label-width="90px">
        <el-form-item label="订单号">{{ refundDialog.orderNo }}</el-form-item>
        <el-form-item label="退款原因">
          <el-input
            v-model="refundDialog.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入退款原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="refundDialog.visible = false">取消</el-button>
        <el-button type="danger" :loading="saving" @click="confirmRefund"
          >确认退款</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import {
  AdminOrderDTO,
  getAdminOrders,
  updateOrderStatus,
  refundOrder,
} from "@/api/admin";

const statusFilter = ref<number | undefined>(undefined);
const loading = ref(false);
const saving = ref(false);
const total = ref(0);
const rows = ref<AdminOrderDTO[]>([]);

const statusText = (status: number): string => {
  const map: Record<number, string> = {
    0: "待支付",
    1: "已支付",
    2: "已出行",
    3: "已取消",
    4: "退款中",
  };
  return map[status] ?? "未知";
};

const statusTagType = (status: number): string => {
  const map: Record<number, string> = {
    0: "warning",
    1: "success",
    2: "primary",
    3: "info",
    4: "danger",
  };
  return map[status] ?? "info";
};

const loadData = async () => {
  loading.value = true;
  try {
    const data = await getAdminOrders(statusFilter.value);
    rows.value = data.list ?? [];
    total.value = data.total ?? 0;
  } finally {
    loading.value = false;
  }
};

// ── 改状态 ──
const statusDialog = reactive({
  visible: false,
  orderNo: "",
  currentStatus: 0,
  newStatus: 0,
  remark: "",
});

const openStatusDialog = (row: AdminOrderDTO) => {
  statusDialog.orderNo = row.order_no;
  statusDialog.currentStatus = row.status;
  statusDialog.newStatus = row.status;
  statusDialog.remark = "";
  statusDialog.visible = true;
};

const confirmStatus = async () => {
  saving.value = true;
  try {
    await updateOrderStatus(
      statusDialog.orderNo,
      statusDialog.newStatus,
      statusDialog.remark || undefined
    );
    ElMessage.success("订单状态已更新");
    statusDialog.visible = false;
    await loadData();
  } finally {
    saving.value = false;
  }
};

// ── 退款 ──
const refundDialog = reactive({
  visible: false,
  orderNo: "",
  remark: "",
});

const openRefundDialog = (row: AdminOrderDTO) => {
  refundDialog.orderNo = row.order_no;
  refundDialog.remark = "";
  refundDialog.visible = true;
};

const confirmRefund = async () => {
  saving.value = true;
  try {
    await refundOrder(refundDialog.orderNo, refundDialog.remark || undefined);
    ElMessage.success("已发起退款");
    refundDialog.visible = false;
    await loadData();
  } finally {
    saving.value = false;
  }
};

onMounted(loadData);
</script>
