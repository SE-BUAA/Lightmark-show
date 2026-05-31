<template>
  <div class="module-page">
    <section class="module-hero">
      <div class="container hero-inner">
        <span class="module-icon">酒店住宿</span>
        <h1 class="section-title">酒店住宿</h1>
        <p class="section-subtitle">按目的地、入住日期、人数和偏好筛选酒店，完成房型选择与预订。</p>
        <div class="hero-actions">
          <el-button type="primary" size="large" @click="goSearch">进入酒店搜索</el-button>
          <el-button size="large" @click="openAiRecommend">AI 推荐酒店</el-button>
        </div>
      </div>
    </section>

    <section class="module-content container">
      <div class="feature-grid">
        <article v-for="item in features" :key="item.title" class="feature-card">
          <span>{{ item.step }}</span>
          <h3>{{ item.title }}</h3>
          <p>{{ item.desc }}</p>
        </article>
      </div>
    </section>

    <AIChatFloat ref="aiChatRef" :hide-fab="true" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import AIChatFloat from '@/views/hotel/components/AIChatFloat.vue'

const router = useRouter()
const aiChatRef = ref<InstanceType<typeof AIChatFloat>>()

const features = [
  {
    step: '01',
    title: '多城市酒店',
    desc: '覆盖北京、上海、成都、杭州、三亚、西安等热门目的地。'
  },
  {
    step: '02',
    title: '房型预订',
    desc: '查看大床房、双床房和套房，选择入住人后创建订单。'
  },
  {
    step: '03',
    title: '取消退款',
    desc: '根据入住前天数和取消政策计算手续费与退款金额。'
  }
]

const goSearch = () => {
  router.push('/hotels/search')
}

const openAiRecommend = () => {
  aiChatRef.value?.open()
}
</script>

<style scoped>
.module-page {
  padding-top: 64px;
}

.module-hero {
  padding: 56px 0 36px;
  background: linear-gradient(135deg, #23415f, #4f7a93);
  color: var(--white);
  text-align: center;
}

.hero-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14px;
}

.module-hero .section-title {
  color: var(--white);
  margin: 0;
}

.module-hero .section-subtitle {
  color: rgba(255, 255, 255, 0.82);
  margin: 0;
  max-width: 680px;
}

.module-icon {
  font-size: 18px;
  font-weight: 600;
}

.hero-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  flex-wrap: wrap;
}

.module-content {
  padding: 32px 0 60px;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
}

.feature-card {
  padding: 24px;
  background: var(--white);
  border: 1px solid var(--slate-200);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.feature-card span {
  color: var(--gold-500);
  font-size: 13px;
  font-weight: 700;
}

.feature-card h3 {
  font-size: 20px;
  color: var(--navy-900);
  margin: 12px 0 8px;
}

.feature-card p {
  color: var(--text-secondary);
  line-height: 1.7;
  margin: 0;
}

@media (max-width: 900px) {
  .feature-grid {
    grid-template-columns: 1fr;
  }
}
</style>
