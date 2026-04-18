<script setup lang="ts">
import { computed, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { getToken, getUser } from '@/utils/auth'
import type { DestinationCard, ModuleCard, StoryCard } from '@/types/app'

const router = useRouter()
const keyword = ref('上海')
const travelDate = ref('2026-05-01')
const travelers = ref('2 人')

const isLoggedIn = computed(() => Boolean(getToken()))
const currentUser = computed(() => getUser())

const quickStats = [
  { value: '120+', label: '精选目的地' },
  { value: '24h', label: '灵活退改支持' },
  { value: '98%', label: '用户满意度' },
  { value: '1M+', label: '累计灵感行程' },
]

const destinations: DestinationCard[] = [
  { city: '上海', route: '机票 + 外滩酒店 + 城市漫游', price: '￥899 起', highlight: '夜景 / 艺术 / 购物', tone: 'rose' },
  { city: '三亚', route: '海岛跟团 / 自由行 / 亲子度假', price: '￥1,299 起', highlight: '海风 / 日落 / 泳池', tone: 'azure' },
  { city: '成都', route: '美食小住 / 熊猫基地 / 轻松漫游', price: '￥799 起', highlight: '火锅 / 慢节奏 / 巷子', tone: 'amber' },
  { city: '东京', route: '精致酒店 / 赏樱 / 城市购物', price: '￥2,499 起', highlight: '街区 / 霓虹 / 美学', tone: 'violet' },
]

const modules: ModuleCard[] = [
  { id: 'm2', title: '用户中心 & AI 助手', description: '出行人、积分、会员、AI 客服', status: '预留中' },
  { id: 'm3', title: '机票预订', description: '搜索、下单、支付、出票', status: '预留中' },
  { id: 'm4', title: '酒店预订', description: '酒店、房型、退订规则', status: '预留中' },
  { id: 'm5', title: '火车票 & 度假', description: '高铁、跟团、自由行', status: '预留中' },
  { id: 'm6', title: '行程 & 社区', description: '行程规划、游记、问答', status: '预留中' },
]

const stories: StoryCard[] = [
  { title: '一张票，一间房，一次刚刚好的出发。', content: '我们把搜索、预订、行程和分享收拢成一条清晰的旅行体验线，让复杂的出行变轻。', author: 'Timemark Studio', accent: 'from-rose' },
  { title: '不是堆功能，而是让每一步都顺手。', content: '把航班、酒店、度假与社区内容放进同一个视觉系统，用户会更愿意停留。', author: '产品设计', accent: 'from-azure' },
]

function enterAdmin() {
  router.push('/admin/dashboard')
}
</script>

<template>
  <div class="home-shell">
    <header class="site-topbar card glass">
      <RouterLink to="/" class="brand-link">
        <span class="brand-mark">T</span>
        <div>
          <strong>Timemark</strong>
          <small>拾光旅行</small>
        </div>
      </RouterLink>

      <nav class="site-nav">
        <a href="#destinations">目的地</a>
        <a href="#modules">模块预览</a>
        <a href="#stories">旅行灵感</a>
      </nav>

      <div class="site-actions">
        <RouterLink class="btn ghost" :to="isLoggedIn ? '/admin/dashboard' : '/login'">{{ isLoggedIn ? '进入后台' : '登录' }}</RouterLink>
        <button v-if="isLoggedIn" class="btn" type="button" @click="enterAdmin">管理台</button>
      </div>
    </header>

    <section class="hero card glass">
      <div class="hero-copy">
        <span class="eyebrow">旅行产品级前台 · 旅游网站首页</span>
        <h1>把出发这件事，做得像在翻一本高级旅行杂志。</h1>
        <p v-if="currentUser" class="welcome-line">欢迎回来，{{ currentUser.nickname }}。今天想去哪里？</p>
        <p>
          机票、酒店、火车票、度假与行程规划，统一在一个更轻盈、更有质感的界面里完成。
          先把体验做美，再把功能做全。
        </p>

        <div class="hero-stats">
          <div v-for="stat in quickStats" :key="stat.label" class="stat-pill">
            <strong>{{ stat.value }}</strong>
            <span>{{ stat.label }}</span>
          </div>
        </div>
      </div>

      <div class="booking-panel card">
        <div class="panel-head">
          <span>快速预订</span>
          <strong>灵感搜索</strong>
        </div>

        <label class="search-field">
          <span>目的地</span>
          <input v-model="keyword" type="text" placeholder="输入城市 / 海岛 / 景区" />
        </label>

        <div class="field-grid">
          <label class="search-field">
            <span>出发日期</span>
            <input v-model="travelDate" type="date" />
          </label>

          <label class="search-field">
            <span>同行人数</span>
            <input v-model="travelers" type="text" />
          </label>
        </div>

        <div class="booking-tags">
          <span>周末短逃离</span>
          <span>海岛松弛感</span>
          <span>亲子友好</span>
          <span>City Walk</span>
        </div>

        <button class="btn booking-btn" type="button">搜索旅行灵感</button>
        <p class="panel-foot">当前默认展示精选内容，真正的搜索与预订在后续模块中接入。</p>
      </div>
    </section>

    <section class="section-head">
      <div>
        <span class="eyebrow">精选目的地</span>
        <h2>先把最有画面感的地方摆在前面。</h2>
      </div>
      <p>高频出行场景已预留好入口，后续再扩成完整搜索 / 列表 / 详情 / 下单链路。</p>
    </section>

    <section id="destinations" class="destination-grid">
      <article v-for="item in destinations" :key="item.city" class="destination-card card" :class="item.tone">
        <div class="destination-top">
          <span>{{ item.city }}</span>
          <strong>{{ item.price }}</strong>
        </div>
        <h3>{{ item.route }}</h3>
        <p>{{ item.highlight }}</p>
        <RouterLink class="mini-link" to="/login">查看灵感</RouterLink>
      </article>
    </section>

    <section class="section-head">
      <div>
        <span class="eyebrow">模块预告</span>
        <h2>其余模块先留好口子，后面按节奏接上。</h2>
      </div>
      <p>点击预览会走现有路由守卫，未登录自动跳转登录页，登录后可继续分模块开发。</p>
    </section>

    <section id="modules" class="module-grid">
      <RouterLink v-for="item in modules" :key="item.id" :to="`/admin/${item.id}`" class="module-card card">
        <span>{{ item.status }}</span>
        <h3>{{ item.title }}</h3>
        <p>{{ item.description }}</p>
      </RouterLink>
    </section>

    <section id="stories" class="story-grid">
      <article v-for="story in stories" :key="story.title" class="story-card card" :class="story.accent">
        <span>Travel Narrative</span>
        <h3>{{ story.title }}</h3>
        <p>{{ story.content }}</p>
        <strong>{{ story.author }}</strong>
      </article>
    </section>

    <footer class="site-footer card">
      <div>
        <strong>Timemark · 拾光旅行</strong>
        <p>模块一优先完成，其他模块预留接口和入口。</p>
      </div>
      <div class="footer-actions">
        <RouterLink class="btn ghost" to="/login">后台登录</RouterLink>
        <RouterLink class="btn" to="/admin/dashboard">运营后台</RouterLink>
      </div>
    </footer>
  </div>
</template>

