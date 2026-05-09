<template>
  <div class="module-page">
    <section class="module-hero">
      <div class="container">
        <span class="module-icon">🗺</span>
        <h1 class="section-title">智能行程</h1>
        <p class="section-subtitle">AI 规划行程，省心省力</p>
      </div>
    </section>
    <section class="module-placeholder">
      <div class="container">
        <div class="placeholder-card">
          <span class="placeholder-icon">🚧</span>
          <h3>模块开发中</h3>
          <p>手动/智能行程规划、行中提醒等功能即将上线</p>
        </div>
      </div>
    </section>
  </div>
</template>
<style scoped>
.module-page {
  padding-top: 64px;
}
.module-hero {
  padding: 60px 0 40px;
  background: linear-gradient(135deg, var(--navy-900), var(--gold-500));
  text-align: center;
  color: var(--white);
}
.module-hero .section-title {
  color: var(--white);
}
.module-hero .section-subtitle {
  color: rgba(255, 255, 255, 0.5);
  margin: 10px auto 0;
}
.module-icon {
  font-size: 48px;
  display: block;
  margin-bottom: 16px;
}
.module-placeholder {
  padding: 60px 0;
}
.placeholder-card {
  text-align: center;
  padding: 60px 24px;
  background: var(--white);
  border-radius: var(--radius-lg);
  border: 2px dashed var(--slate-200);
}
.placeholder-icon {
  font-size: 48px;
  display: block;
  margin-bottom: 16px;
}
.placeholder-card h3 {
  font-size: 20px;
  color: var(--navy-900);
  margin-bottom: 8px;
}
.placeholder-card p {
  color: var(--text-secondary);
}
</style>
