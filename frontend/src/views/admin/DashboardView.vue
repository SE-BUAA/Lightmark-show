<template>
  <div class="admin-page" v-loading="loading">
    <!-- 核心指标 -->
    <div class="metric-grid">
      <article class="metric-card">
        <div class="metric-header">
          <span class="metric-icon">👥</span>
          <span>总用户数</span>
        </div>
        <strong>{{ summary.totalUsers }}</strong>
      </article>
      <article class="metric-card">
        <div class="metric-header">
          <span class="metric-icon">📋</span>
          <span>总订单数</span>
        </div>
        <strong>{{ summary.totalOrders }}</strong>
      </article>
      <article class="metric-card">
        <div class="metric-header">
          <span class="metric-icon">💰</span>
          <span>累计营收</span>
        </div>
        <strong>￥{{ (summary.totalRevenue ?? 0).toFixed(2) }}</strong>
      </article>
    </div>

    <div class="dashboard-grid">
      <!-- 近 7 天趋势 -->
      <section class="chart-card">
        <h3>近 7 天交易趋势</h3>
        <div ref="chartRef" class="chart"></div>
      </section>

      <!-- 热门产品 Top10 -->
      <section class="hot-card">
        <h3>热门产品 Top10</h3>
        <table class="hot-table" v-if="hotProducts.length > 0">
          <thead>
            <tr>
              <th>#</th>
              <th>名称</th>
              <th>类型</th>
              <th>销量</th>
              <th>营收</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(p, i) in hotProducts" :key="p.id">
              <td class="rank">{{ i + 1 }}</td>
              <td>{{ p.name }}</td>
              <td>
                <el-tag size="small" effect="plain">{{ p.productType }}</el-tag>
              </td>
              <td>{{ p.soldCount ?? 0 }}</td>
              <td>￥{{ (p.revenue ?? 0).toFixed(2) }}</td>
            </tr>
          </tbody>
        </table>
        <div v-else class="empty-hint">暂无数据</div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, reactive, ref } from "vue";
import * as echarts from "echarts";
import {
  getDashboardSummary,
  getDashboardTrends,
  getHotProducts,
  DashboardTrendDTO,
  HotProductDTO,
} from "@/api/admin";

const loading = ref(false);
const chartRef = ref<HTMLElement | null>(null);
let chart: echarts.ECharts | null = null;

const summary = reactive({
  totalUsers: 0,
  totalOrders: 0,
  totalRevenue: 0,
});

const hotProducts = ref<HotProductDTO[]>([]);

const renderChart = (trends: DashboardTrendDTO[]) => {
  if (!chartRef.value) return;

  const safeTrends = Array.isArray(trends) ? trends : [];

  chart = echarts.init(chartRef.value);
  chart.setOption({
    tooltip: {
      trigger: "axis",
      valueFormatter: (v: number) =>
        typeof v === "number" ? `￥${v.toFixed(2)}` : String(v),
    },
    legend: { data: ["订单数", "营收"] },
    grid: { left: 50, right: 20, bottom: 20, top: 40 },
    xAxis: {
      type: "category",
      data: safeTrends.map((t) => t.date),
      axisLabel: { fontSize: 12 },
    },
    yAxis: [{ type: "value" }, { type: "value", splitLine: { show: false } }],
    series: [
      {
        name: "订单数",
        type: "bar",
        data: safeTrends.map((t) => t.orderCount),
        itemStyle: { color: "#409eff" },
      },
      {
        name: "营收",
        type: "line",
        yAxisIndex: 1,
        smooth: true,
        data: safeTrends.map((t) => t.revenue),
        lineStyle: { color: "#c9953d", width: 2 },
        itemStyle: { color: "#c9953d" },
        areaStyle: { color: "rgba(201,149,61,0.12)" },
      },
    ],
  });
};

const resizeChart = () => chart?.resize();

onMounted(async () => {
  loading.value = true;
  try {
    const [summaryData, trends, hot] = await Promise.all([
      getDashboardSummary(),
      getDashboardTrends(),
      getHotProducts(),
    ]);
    summary.totalUsers = summaryData.totalUsers ?? 0;
    summary.totalOrders = summaryData.totalOrders ?? 0;
    summary.totalRevenue = summaryData.totalRevenue ?? 0;
    hotProducts.value = hot ?? [];

    await nextTick();
    renderChart(trends ?? []);
    window.addEventListener("resize", resizeChart);
  } finally {
    loading.value = false;
  }
});

onBeforeUnmount(() => {
  window.removeEventListener("resize", resizeChart);
  chart?.dispose();
  chart = null;
});
</script>
