<template>
  <div class="module-page">
    <section class="module-hero">
      <div class="container">
        <h1 class="section-title">关于拾光旅行</h1>
        <p class="section-subtitle">
          用心记录每一次出发，让旅行成为生命中最美好的光影
        </p>
      </div>
    </section>
    <section class="about-content">
      <div class="container about-grid">
        <div class="about-card">
          <span class="about-icon">🎯</span>
          <h3>我们的使命</h3>
          <p>
            打造一站式智能旅行规划平台，让每个人都能轻松规划完美的出行体验。
          </p>
        </div>
        <div class="about-card">
          <span class="about-icon">💡</span>
          <h3>核心优势</h3>
          <p>
            机票、酒店、火车票、度假产品全覆盖，AI 智能助手让行程规划更智慧。
          </p>
        </div>
        <div class="about-card">
          <span class="about-icon">🤝</span>
          <h3>联系我们</h3>
          <p>
            如有问题或建议，欢迎通过客服邮箱 feedback@lightmark.com 与我们联系。
          </p>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.about-content {
  padding: 60px 0;
}

.about-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.about-card {
  background: var(--white);
  border-radius: var(--radius-md);
  padding: 36px 28px;
  border: 1px solid var(--slate-100);
  transition: all var(--duration-normal) var(--ease-out);
}

.about-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
}

.about-icon {
  font-size: 36px;
  display: block;
  margin-bottom: 16px;
}
.about-card h3 {
  font-size: 18px;
  font-weight: 700;
  color: var(--navy-900);
  margin-bottom: 10px;
}
.about-card p {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.7;
}

@media (max-width: 768px) {
  .about-grid {
    grid-template-columns: 1fr;
  }
}
</style>
