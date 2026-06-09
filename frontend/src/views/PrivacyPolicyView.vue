<template>
  <div class="module-page">
    <section class="module-hero">
      <div class="container">
        <h1 class="section-title">隐私政策</h1>
        <p class="section-subtitle">
          拾光旅行重视你的个人信息安全。以下内容用于说明我们如何收集、使用、存储与保护你的信息。
        </p>
      </div>
    </section>

    <section class="policy-content">
      <div class="container policy-card">
        <div class="policy-section">
          <h2>1. 我们收集的信息</h2>
          <p>
            当你注册、登录、预订机票/酒店/火车票/度假产品、使用 AI 行程助手或联系客服时，我们可能收集账号信息、联系方式、订单信息、出行偏好及必要的设备日志信息。
          </p>
        </div>

        <div class="policy-section">
          <h2>2. 信息的使用目的</h2>
          <p>
            我们使用这些信息为你提供账号认证、订单处理、退款售后、行程推荐、客服支持、风控审计与服务优化能力，不会超出与你当前业务直接相关的范围使用。
          </p>
        </div>

        <div class="policy-section">
          <h2>3. 信息存储与保护</h2>
          <p>
            我们会采取合理的技术和管理措施保护你的数据安全，包括访问控制、日志记录与必要的传输保护。仅在完成服务所需的期限内保存相关数据。
          </p>
        </div>

        <div class="policy-section">
          <h2>4. 第三方服务</h2>
          <p>
            部分能力可能依赖第三方服务，例如邮件验证码、对象存储、地图或 AI 能力提供方。我们会尽量选择合规服务，并仅在实现功能所必需时共享最少信息。
          </p>
        </div>

        <div class="policy-section">
          <h2>5. 你的权利</h2>
          <p>
            你可以通过用户中心维护个人资料；如需进一步咨询、反馈问题或申请处理与账户相关的事项，可通过客服邮箱联系我们。
          </p>
        </div>

        <div class="policy-section">
          <h2>6. 联系我们</h2>
          <p>
            客服邮箱：
            <a href="mailto:feedback@lightmark.com">feedback@lightmark.com</a>
          </p>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.policy-card {
  max-width: 920px;
  margin: 0 auto;
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-light);
  box-shadow: var(--shadow-md);
  padding: 36px 32px;
}

.policy-section + .policy-section {
  margin-top: 28px;
}

.policy-section h2 {
  margin: 0 0 10px;
  font-size: 20px;
  color: var(--navy-900);
}

.policy-section p {
  margin: 0;
  line-height: 1.8;
  color: var(--text-secondary);
}

.policy-section a {
  color: var(--accent);
}

@media (max-width: 768px) {
  .policy-card {
    padding: 28px 20px;
  }
}</style>
