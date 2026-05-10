<template>
  <footer class="site-footer">
    <div class="container footer-grid">
      <div class="footer-brand">
        <div class="footer-logo">
          <span class="logo-icon">✦</span>
          <span class="logo-text">拾光旅行</span>
        </div>
        <p class="footer-desc">
          用心记录每一次出发，让旅行成为生命中最美好的光影。
        </p>
      </div>

      <div class="footer-col">
        <h4>产品服务</h4>
        <router-link to="/flights">机票预订</router-link>
        <router-link to="/hotels">酒店预订</router-link>
        <router-link to="/trains">火车票</router-link>
        <router-link to="/vacations">旅游度假</router-link>
      </div>

      <div class="footer-col">
        <h4>帮助中心</h4>
        <router-link to="/about">关于我们</router-link>
        <a href="#">退改签政策</a>
        <a href="#">常见问题</a>
        <a href="#">联系客服</a>
      </div>

      <div class="footer-col">
        <h4>社区</h4>
        <router-link to="/community">游记攻略</router-link>
        <router-link to="/itinerary">行程规划</router-link>
        <a href="#">问答社区</a>
      </div>
    </div>

    <div class="footer-bottom">
      <div class="container">
        <p>
          &copy; {{ new Date().getFullYear() }} 拾光旅行 Lightmark. All rights
          reserved.
        </p>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.site-footer {
  background: var(--navy-900);
  color: rgba(255, 255, 255, 0.7);
  margin-top: auto;
}

.footer-grid {
  display: grid;
  grid-template-columns: 1.8fr 1fr 1fr 1fr;
  gap: 40px;
  padding: 56px 24px 40px;
  max-width: 1200px;
  margin: 0 auto;
}

.footer-logo {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 22px;
  font-weight: 700;
  margin-bottom: 14px;
}

.footer-logo .logo-icon {
  color: var(--accent);
  font-size: 24px;
}

.footer-logo .logo-text {
  font-family: var(--font-heading);
  color: var(--white);
  letter-spacing: 1px;
}

.footer-desc {
  font-size: 14px;
  line-height: 1.7;
  max-width: 300px;
  color: rgba(255, 255, 255, 0.5);
}

.footer-col h4 {
  color: var(--white);
  font-size: 14px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-bottom: 16px;
}

.footer-col a {
  display: block;
  font-size: 14px;
  padding: 5px 0;
  color: rgba(255, 255, 255, 0.55);
  transition: color var(--duration-fast) var(--ease-out);
}

.footer-col a:hover {
  color: var(--gold-400);
}

.footer-bottom {
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  padding: 18px 0;
}

.footer-bottom p {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.35);
  text-align: center;
}

@media (max-width: 768px) {
  .footer-grid {
    grid-template-columns: 1fr 1fr;
    gap: 30px;
    padding: 40px 16px 30px;
  }

  .footer-brand {
    grid-column: 1 / -1;
  }
}

@media (max-width: 480px) {
  .footer-grid {
    grid-template-columns: 1fr;
  }
}
</style>
