<template>
  <div class="admin-page">
    <!-- 工具栏 -->
    <div class="toolbar">
      <el-input
        v-model="searchName"
        placeholder="搜索产品名称"
        clearable
        style="width: 200px"
        @keyup.enter="loadData"
      />
      <el-select
        v-model="searchType"
        placeholder="产品类型"
        clearable
        style="width: 130px"
        @change="loadData"
      >
        <el-option label="机票" value="FLIGHT" />
        <el-option label="酒店" value="HOTEL" />
        <el-option label="火车" value="TRAIN" />
        <el-option label="度假" value="VACATION" />
      </el-select>
      <el-select
        v-model="searchStatus"
        placeholder="状态"
        clearable
        style="width: 110px"
        @change="loadData"
      >
        <el-option label="上架" :value="1" />
        <el-option label="下架" :value="0" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
      <div style="flex: 1" />
      <el-button type="primary" @click="openCreate">+ 新增产品</el-button>
    </div>

    <!-- 产品列表 -->
    <el-table v-loading="loading" :data="rows" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="product_type" label="类型" width="100" />
      <el-table-column prop="name" label="名称" min-width="180" />
      <el-table-column prop="price" label="价格" width="110">
        <template #default="scope"
          >￥{{ Number(scope.row.price).toFixed(2) }}</template
        >
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="80" />
      <el-table-column prop="sold_count" label="销量" width="80" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="scope">
          <el-tag
            :type="scope.row.status === 1 ? 'success' : 'info'"
            effect="dark"
            size="small"
          >
            {{ scope.row.status === 1 ? "上架" : "下架" }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="scope">
          <el-button
            size="small"
            :type="scope.row.status === 1 ? 'warning' : 'success'"
            plain
            @click="toggleStatus(scope.row)"
          >
            {{ scope.row.status === 1 ? "下架" : "上架" }}
          </el-button>
          <el-button size="small" plain @click="openEditPrice(scope.row)"
            >调价</el-button
          >
          <el-button size="small" plain @click="openEditStock(scope.row)"
            >库存</el-button
          >
          <el-popconfirm
            title="确定删除此产品？"
            @confirm="handleDelete(scope.row.id)"
          >
            <template #reference>
              <el-button size="small" type="danger" plain>删除</el-button>
            </template>
          </el-popconfirm>
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

    <!-- 调价对话框 -->
    <el-dialog v-model="priceDialog.visible" title="调整价格" width="400px">
      <el-form label-width="80px">
        <el-form-item label="产品名称">{{
          priceDialog.productName
        }}</el-form-item>
        <el-form-item label="当前价格"
          >￥{{ priceDialog.oldValue.toFixed(2) }}</el-form-item
        >
        <el-form-item label="新价格">
          <el-input-number
            v-model="priceDialog.newValue"
            :min="0"
            :precision="2"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="priceDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="confirmPrice"
          >确认</el-button
        >
      </template>
    </el-dialog>

    <!-- 调库存对话框 -->
    <el-dialog v-model="stockDialog.visible" title="调整库存" width="400px">
      <el-form label-width="80px">
        <el-form-item label="产品名称">{{
          stockDialog.productName
        }}</el-form-item>
        <el-form-item label="当前库存">{{ stockDialog.oldValue }}</el-form-item>
        <el-form-item label="新库存">
          <el-input-number
            v-model="stockDialog.newValue"
            :min="0"
            :step="1"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stockDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="confirmStock"
          >确认</el-button
        >
      </template>
    </el-dialog>

    <!-- 新增产品对话框 -->
    <el-dialog v-model="createDialog.visible" title="新增产品" width="520px">
      <el-form :model="createForm" label-width="100px">
        <el-form-item label="产品类型">
          <el-select v-model="createForm.productType" style="width: 100%">
            <el-option label="机票" value="FLIGHT" />
            <el-option label="酒店" value="HOTEL" />
            <el-option label="火车" value="TRAIN" />
            <el-option label="度假" value="VACATION" />
          </el-select>
        </el-form-item>
        <el-form-item label="产品名称">
          <el-input v-model="createForm.name" placeholder="请输入名称" />
        </el-form-item>
        <el-form-item label="价格">
          <el-input-number
            v-model="createForm.price"
            :min="0"
            :precision="2"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="库存">
          <el-input-number
            v-model="createForm.stock"
            :min="0"
            :step="1"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="createForm.active"
            active-text="上架"
            inactive-text="下架"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="confirmCreate"
          >创建</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import {
  AdminProductDTO,
  getAdminProducts,
  updateProductStatus,
  updateProductPrice,
  updateProductStock,
  createProduct,
  deleteProduct,
  CreateProductRequest,
} from "@/api/admin";

// ── 搜索 ──
const searchName = ref("");
const searchType = ref("");
const searchStatus = ref<number | undefined>(undefined);
const currentPage = ref(1);
const pageSize = 100;
const loading = ref(false);
const saving = ref(false);
const total = ref(0);
const rows = ref<AdminProductDTO[]>([]);

const loadData = async () => {
  loading.value = true;
  try {
    const data = await getAdminProducts({
      productType: searchType.value || undefined,
      name: searchName.value || undefined,
      status: searchStatus.value,
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
  searchName.value = "";
  searchType.value = "";
  searchStatus.value = undefined;
  currentPage.value = 1;
  await loadData();
};

const handleSearch = async () => {
  currentPage.value = 1;
  await loadData();
};

// ── 上架/下架 ──
const toggleStatus = async (row: AdminProductDTO) => {
  const newStatus = row.status === 1 ? 0 : 1;
  await updateProductStatus(row.id, newStatus);
  ElMessage.success(newStatus === 1 ? "已上架" : "已下架");
  await loadData();
};

// ── 调价 ──
const priceDialog = reactive({
  visible: false,
  productId: 0,
  productName: "",
  oldValue: 0,
  newValue: 0,
});

const openEditPrice = (row: AdminProductDTO) => {
  priceDialog.productId = row.id;
  priceDialog.productName = row.name;
  priceDialog.oldValue = row.price;
  priceDialog.newValue = row.price;
  priceDialog.visible = true;
};

const confirmPrice = async () => {
  saving.value = true;
  try {
    await updateProductPrice(priceDialog.productId, priceDialog.newValue);
    ElMessage.success("价格已更新");
    priceDialog.visible = false;
    await loadData();
  } finally {
    saving.value = false;
  }
};

// ── 调库存 ──
const stockDialog = reactive({
  visible: false,
  productId: 0,
  productName: "",
  oldValue: 0,
  newValue: 0,
});

const openEditStock = (row: AdminProductDTO) => {
  stockDialog.productId = row.id;
  stockDialog.productName = row.name;
  stockDialog.oldValue = row.stock;
  stockDialog.newValue = row.stock;
  stockDialog.visible = true;
};

const confirmStock = async () => {
  saving.value = true;
  try {
    await updateProductStock(stockDialog.productId, stockDialog.newValue);
    ElMessage.success("库存已更新");
    stockDialog.visible = false;
    await loadData();
  } finally {
    saving.value = false;
  }
};

// ── 新增产品 ──
const createDialog = reactive({ visible: false });
const createForm = reactive({
  productType: "HOTEL",
  name: "",
  price: 0,
  stock: 0,
  active: true,
});

const openCreate = () => {
  createForm.productType = "HOTEL";
  createForm.name = "";
  createForm.price = 0;
  createForm.stock = 0;
  createForm.active = true;
  createDialog.visible = true;
};

const confirmCreate = async () => {
  if (!createForm.name.trim()) {
    ElMessage.warning("请填写产品名称");
    return;
  }
  saving.value = true;
  try {
    const payload: CreateProductRequest = {
      productType: createForm.productType,
      name: createForm.name,
      price: createForm.price,
      stock: createForm.stock,
      soldCount: 0,
      status: createForm.active ? 1 : 0,
      categoryTags: "",
      extra: "",
    };
    await createProduct(payload);
    ElMessage.success("产品创建成功");
    createDialog.visible = false;
    await loadData();
  } finally {
    saving.value = false;
  }
};

// ── 删除产品 ──
const handleDelete = async (id: number) => {
  await deleteProduct(id);
  ElMessage.success("产品已删除");
  await loadData();
};

onMounted(loadData);
</script>
