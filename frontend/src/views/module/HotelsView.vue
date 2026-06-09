<template>
  <div class="module-page">
    <section class="module-hero module-hero--warm">
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
          <span class="step-num">{{ item.step }}</span>
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
.feature-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
}

@media (max-width: 900px) {
  .feature-grid {
    grid-template-columns: 1fr;
  }
}
</style>
