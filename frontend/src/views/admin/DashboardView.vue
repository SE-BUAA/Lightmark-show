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

const chartTooltipValueFormatter = (seriesName: string, value: number | string) => {
  if (typeof value !== "number") {
    return String(value);
  }
  return seriesName === "订单数" ? `${Math.round(value)} 单` : `￥${value.toFixed(2)}`;
};

const renderChart = (trends: DashboardTrendDTO[]) => {
  if (!chartRef.value) return;

  const safeTrends = Array.isArray(trends) ? trends : [];

  chart = echarts.init(chartRef.value);
  chart.setOption({
    tooltip: {
      trigger: "axis",
      valueFormatter: (value: number | string, _dataIndex: number, params: { seriesName?: string }) =>
        chartTooltipValueFormatter(params?.seriesName || "", value),
    },
    legend: { data: ["订单数", "营收"] },
    grid: { left: 50, right: 20, bottom: 20, top: 40 },
    xAxis: {
      type: "category",
      data: safeTrends.map((t) => t.date),
      axisLabel: { fontSize: 12 },
    },
    yAxis: [
      {
        type: "value",
        name: "订单数",
        minInterval: 1,
        axisLabel: {
          formatter: (value: number) => `${Math.round(value)}`,
        },
      },
      {
        type: "value",
        name: "营收",
        splitLine: { show: false },
        axisLabel: {
          formatter: (value: number) => `￥${value.toFixed(0)}`,
        },
      },
    ],
    series: [
      {
        name: "订单数",
        type: "bar",
        data: safeTrends.map((t) => Math.round(t.orderCount ?? 0)),
        itemStyle: { color: "#409eff" },
        tooltip: {
          valueFormatter: (value: number | string) => chartTooltipValueFormatter("订单数", value),
        },
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
        tooltip: {
          valueFormatter: (value: number | string) => chartTooltipValueFormatter("营收", value),
        },
      },
    ],
  });
};

const resizeChart = () => chart?.resize();

onMounted(async () => {
  loading.value = true;

  // 逐个请求，互不阻塞；某个接口失败不影响其他数据展示
  try {
    const summaryData = await getDashboardSummary();
    summary.totalUsers = summaryData.totalUsers ?? 0;
    summary.totalOrders = summaryData.totalOrders ?? 0;
    summary.totalRevenue = summaryData.totalRevenue ?? 0;
  } catch {
    // 概要接口失败，保持默认值 0
  }

  try {
    const trends = await getDashboardTrends();
    await nextTick();
    renderChart(trends ?? []);
  } catch {
    // 趋势接口失败，不渲染图表
  }

  window.addEventListener("resize", resizeChart);

  try {
    const hot = await getHotProducts();
    hotProducts.value = hot ?? [];
  } catch {
    // 热门产品接口失败，展示「暂无数据」
  }

  loading.value = false;
});

onBeforeUnmount(() => {
  window.removeEventListener("resize", resizeChart);
  chart?.dispose();
  chart = null;
});
</script>
