# 拾光旅行 Lightmark — 前端

在线旅游平台（机票/酒店/火车票/度假）的前端应用，基于 Vue 3 + TypeScript + Element Plus 构建。

## 技术栈

| 类别 | 选型 |
|---|---|
| 框架 | Vue 3 (Composition API + `<script setup>`) |
| 语言 | TypeScript |
| UI 组件库 | Element Plus |
| 状态管理 | Pinia |
| 路由 | Vue Router 4 |
| HTTP 客户端 | Axios |
| 图表 | ECharts |
| 构建工具 | Vue CLI 5 |

## 项目结构

```
src/
├── api/              # API 接口模块（按业务域拆分）
│   ├── auth.ts       #   认证：登录/注册/登出
│   ├── user.ts       #   用户中心：个人信息/出行人/积分/订单
│   └── admin.ts      #   管理后台：仪表盘/产品/订单/用户/日志
├── assets/
│   └── styles/
│       ├── global.css  # 全局主题变量、重置、按钮、暗色模式
│       ├── admin.css   # 管理后台布局与组件样式
│       └── button.css  # 按钮样式
├── components/       # 公共组件
│   ├── AppHeader.vue   # 顶部导航栏（含登录态/登出）
│   ├── AppFooter.vue   # 页脚
│   ├── ThemeToggle.vue # 明暗主题切换
│   └── HelloWorld.vue  # 默认示例组件
├── layouts/          # 布局组件
│   └── AdminLayout.vue # 管理后台布局（侧边栏 + 顶栏 + 内容区）
├── router/
│   └── index.ts        # 路由配置 + 全局导航守卫（权限控制）
├── stores/
│   └── auth.ts         # 认证状态（Pinia）：token/用户信息/会话持久化
├── utils/
│   ├── auth.ts         # 认证工具：localStorage 读写 / 权限解析
│   └── request.ts      # Axios 封装：统一拦截器 / 错误处理 / Token 注入
├── views/
│   ├── HomeView.vue    # 首页
│   ├── AboutView.vue   # 关于页面
│   ├── auth/
│   │   └── UserLoginView.vue  # 用户登录/注册
│   ├── admin/
│   │   ├── AdminLoginView.vue  # 管理后台登录
│   │   ├── DashboardView.vue   # 仪表盘（指标 + 趋势图 + 热门产品）
│   │   ├── ProductManageView.vue # 产品管理（CRUD + 调价调库存）
│   │   ├── OrderManageView.vue   # 订单管理（改状态 + 退款）
│   │   ├── UserManageView.vue    # 用户管理（封禁 + 改等级）
│   │   └── TableBrowserView.vue  # 操作日志
│   └── module/
│       ├── FlightsView.vue   # 机票搜索
│       ├── HotelsView.vue    # 酒店搜索
│       ├── TrainsView.vue    # 火车票搜索
│       ├── VacationsView.vue # 度假产品搜索
│       ├── ItineraryView.vue # 行程规划
│       ├── CommunityView.vue # 社区
│       └── UserCenterView.vue# 用户中心（个人信息展示/编辑）
├── App.vue           # 根组件（admin 路由下沉 vs 普通布局切换）
└── main.ts           # 应用入口（注册插件、全局样式）
```

## 快速开始

### 前置要求

- Node.js >= 16
- npm >= 7

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run serve
```

应用默认运行在 `http://localhost:8081`，API 请求自动代理到 `http://localhost:8080`。

### 构建生产版本

```bash
npm run build
```

### 代码检查

```bash
npm run lint
```

## 环境配置

### API 代理

开发环境中，`vue.config.js` 配置了 `/api` 前缀的请求代理到后端服务：

```js
proxy: {
  "/api": {
    target: "http://localhost:8080",
    changeOrigin: true,
  },
}
```

如需修改后端地址，编辑 `vue.config.js` 中的 `target`。

### 自定义 API Base URL

通过环境变量 `VUE_APP_API_BASE_URL` 可覆盖默认的 `/api` 前缀（默认即 `/api`，无需额外配置）。

## 后端 API

可参照同目录下的 [`API.md`](./API.md) 文档，后端基于新版 RESTful 路由。

## 功能模块

| 模块 | 路由 | 说明 |
|---|---|---|
| 首页 | `/` | — |
| 用户登录/注册 | `/login` | 支持手机号/邮箱 + 密码 |
| 机票 | `/flights` | 搜索与预订 |
| 酒店 | `/hotels` | 搜索与预订 |
| 火车票 | `/trains` | 搜索与预订 |
| 度假 | `/vacations` | 搜索与预订 |
| 行程规划 | `/itinerary` | — |
| 社区 | `/community` | — |
| 用户中心 | `/user-center` | 个人信息、出行人、积分、订单 |
| 管理后台登录 | `/admin/login` | 与主站账号互通，仅管理员可入 |
| 仪表盘 | `/admin/dashboard` | 核心指标、趋势图、热门产品 |
| 产品管理 | `/admin/products` | 列表/搜索/增删改/调价调库存 |
| 订单总览 | `/admin/orders` | 列表/筛选/改状态/退款 |
| 用户管理 | `/admin/users` | 列表/搜索/封禁/改等级 |
| 操作日志 | `/admin/tables` | 管理员操作记录 |
