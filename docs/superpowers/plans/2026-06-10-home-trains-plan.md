# 首页登录按钮与火车票卡顿修复 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 隐藏首页内容区的登录/注册按钮，并降低从其他页面切入火车票页面时的首屏冻结感。

**Architecture:** 保持现有业务逻辑不变，只在前端做定点优化。首页通过现有 Pinia 登录态控制内容区按钮显隐；火车票页通过减少阻塞式路由切换、延后初始化、并行请求及预处理表格派生字段来降低主线程压力。

**Tech Stack:** Vue 3, TypeScript, Vue Router, Pinia, Element Plus

---

### Task 1: 首页内容区按钮按登录态隐藏

**Files:**
- Modify: `frontend/src/views/HomeView.vue`

- [ ] **Step 1: 接入认证 store 并暴露登录态**

```ts
import { computed, ref } from "vue";
import { storeToRefs } from "pinia";
import { useAuthStore } from "@/stores/auth";

const authStore = useAuthStore();
const { isLoggedIn } = storeToRefs(authStore);
const showGuestActions = computed(() => !isLoggedIn.value);
```

- [ ] **Step 2: 只在未登录时渲染 Hero 区登录按钮**

```vue
<router-link
  v-if="showGuestActions"
  to="/login"
  class="btn btn-secondary btn-hero"
>
  登录 / 注册
</router-link>
```

- [ ] **Step 3: 只在未登录时渲染 CTA 注册按钮**

```vue
<router-link
  v-if="showGuestActions"
  to="/login"
  class="btn btn-primary btn-lg"
>
  立即注册
  <span class="btn-arrow">→</span>
</router-link>
```

### Task 2: 降低普通页面切换阻塞

**Files:**
- Modify: `frontend/src/App.vue`

- [ ] **Step 1: 移除阻塞式 `out-in` 过渡**

```vue
<router-view v-slot="{ Component }">
  <component :is="Component" />
</router-view>
```

- [ ] **Step 2: 保持后台路由与公共壳层逻辑不变**

```ts
const route = useRoute();
const isAdminRoute = computed(() => route.path.startsWith("/admin"));
```

### Task 3: 火车票页初始化改为首帧后并行执行

**Files:**
- Modify: `frontend/src/views/module/TrainsView.vue`

- [ ] **Step 1: 将挂载初始化放到首帧之后**

```ts
import { nextTick, onMounted } from "vue";

onMounted(async () => {
  await nextTick();
  void initializePage();
});
```

- [ ] **Step 2: 抽出并行初始化函数**

```ts
const initializePage = async () => {
  await Promise.allSettled([getTravelers(), loadOptions()]);
  await refreshTrainData();
};
```

- [ ] **Step 3: 保留现有错误处理与回退行为**

```ts
const loadOptions = async () => {
  try {
    // 保留现有接口逻辑
  } catch (error) {
    // 保留现有提示逻辑
  }
};
```

### Task 4: 预处理火车票表格派生字段

**Files:**
- Modify: `frontend/src/views/module/TrainsView.vue`

- [ ] **Step 1: 定义展示层派生字段结构**

```ts
type TrainRowView = TrainItem & {
  __seatNames: string[];
  __availableTickets: number;
  __priceText: string;
  __segments: TrainItem[];
};
```

- [ ] **Step 2: 增加行级预处理函数**

```ts
const enrichTrainRow = (row: TrainItem): TrainRowView => ({
  ...row,
  __seatNames: availableSeatNames(row),
  __availableTickets: availableTickets(row),
  __priceText: formatTrainPrices(row),
  __segments: transferSegments(row),
});
```

- [ ] **Step 3: 让表格使用预处理后的分页数据**

```ts
const pagedTrains = computed(() =>
  trainStore.trainsList
    .slice((currentPage.value - 1) * pageSize, currentPage.value * pageSize)
    .map(enrichTrainRow)
);
```

- [ ] **Step 4: 模板直接渲染派生字段**

```vue
<el-tag v-for="seat in row.__seatNames" :key="seat" class="tag" size="small">{{ seat }}</el-tag>
<template #default="{ row }">{{ row.__priceText }}</template>
<template #default="{ row }">{{ row.__availableTickets }}</template>
<div v-for="(segment, index) in row.__segments" :key="`${segmentTrainNo(segment)}-${index}`">
```

### Task 5: 验证与回归

**Files:**
- Verify: `frontend/src/views/HomeView.vue`
- Verify: `frontend/src/App.vue`
- Verify: `frontend/src/views/module/TrainsView.vue`

- [ ] **Step 1: 运行前端构建**

Run: `npm run build`

Expected: Vite 构建成功，无新增 TypeScript 或模板错误

- [ ] **Step 2: 人工验证首页登录态**

Run:

```text
未登录访问首页 -> 内容区显示“登录 / 注册”“立即注册”
登录后返回首页 -> 内容区两个按钮隐藏，顶部导航保持原样
```

Expected: 内容区按钮仅在未登录时显示

- [ ] **Step 3: 人工验证火车票切页体感**

Run:

```text
从首页/机票页/酒店页点击进入火车票页
观察首屏是否立即可滚动、可点击
确认直达/中转列表内容与原来一致
```

Expected: 不再出现明显全页冻结，功能行为保持不变
