# 拾光旅行 Lightmark

> 一站式在线旅游平台，涵盖机票、酒店、火车票、度假产品预订，以及智能行程规划、社区互动、AI 助手等功能。前后端分离，响应式 Web 设计，一套代码适配 PC / 平板 / 手机。

[![Vue 3](https://img.shields.io/badge/Vue-3.4-42b883?logo=vue.js)](https://vuejs.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.13-6db33f?logo=springboot)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479a1?logo=mysql)](https://mysql.com)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.0-3178c6?logo=typescript)](https://www.typescriptlang.org/)
[![License](https://img.shields.io/badge/license-MIT-green)](LICENSE)

## 📖 目录

- [项目简介](#项目简介)
- [技术栈](#技术栈)
- [核心功能模块](#核心功能模块)
- [工程结构](#工程结构)
- [快速开始](#快速开始)
- [数据库设计](#数据库设计)
- [API 文档](#api-文档)
- [AI 集成](#ai-集成)
- [Skill 提示词](#skill-提示词)
- [部署架构](#部署架构)
- [贡献与许可](#贡献与许可)

## 项目简介

**拾光旅行 (Lightmark)** 是一个功能完整的旅游服务平台，包含用户端和管理后台。项目采用前后端分离架构，前端使用 Vue 3 + TypeScript + Element Plus，后端使用 Spring Boot 3 + MyBatis-Plus，数据库为 MySQL 8.0，并集成了大模型 API 实现智能客服、自然语言搜索、行程规划等 AI 能力。

主要特点：
- ✅ 响应式布局，移动端友好
- ✅ JWT 身份认证 + 路由权限控制
- ✅ 订单聚合管理（机票/酒店/火车票/度假）
- ✅ 后台运营仪表盘（ECharts 可视化）
- ✅ 常用出行人、积分会员体系
- ✅ 社区功能（游记/评价/问答）
- ✅ AI 智能助手（客服、自然语言修改信息、行程规划）

## 技术栈

| 层次            | 技术选型                                                     |
| --------------- | ------------------------------------------------------------ |
| **前端**        | Vue 3 (Composition API), TypeScript, Vite, Pinia, Vue Router 4 |
| **UI 库**       | Element Plus (PC/后台), 响应式 CSS                           |
| **后端**        | Spring Boot 3.5.13, MyBatis-Plus, JWT, BCrypt                |
| **数据库**      | MySQL 8.0, Redis (会话/缓存)                                 |
| **HTTP 客户端** | Axios (前端), RestTemplate (后端)                            |
| **图表**        | ECharts                                                      |
| **构建与部署**  | Maven, Nginx, Docker (可选)                                  |
| **AI 集成**     | 大模型 API (讯飞/百度) + RAG 知识库                          |

## 核心功能模块

| 模块         | 主要功能                                                     |
| ------------ | ------------------------------------------------------------ |
| 🔐 身份认证   | 手机号/邮箱注册登录、JWT token、路由守卫、管理员权限拦截     |
| 📊 管理后台   | 仪表盘（交易额/订单量/用户增长）、产品上下架/调价、订单干预、用户封禁/等级调整、操作日志 |
| 👤 用户中心   | 个人资料修改、头像上传、出行人管理、积分明细、会员等级、安全设置、我的订单聚合 |
| ✈️ 机票预订   | 多条件搜索、航班列表排序筛选、价格日历、舱位选择、乘客信息填写、模拟支付、退改签计算 |
| 🏨 酒店预订   | 目的地/日期搜索、地图模式、房型详情、积分抵扣、取消政策、发票申请 |
| 🚆 火车票预订 | 车次查询、坐席选铺、儿童/学生票、改签退票                    |
| 🏖️ 度假产品   | 跟团/自由行搜索、产品详情、模拟合同生成                      |
| 🧠 智能行程   | 手动拖拽规划、行程共享导出、出行前提醒、AI 生成个性化行程、游记自动生成、评论情感分析 |
| 💬 社区       | 游记图文发布、点赞评论、问答社区、当地玩乐推荐               |
| 🤖 AI 助手    | 智能客服（退改签政策）、语音输入搜索、自然语言修改个人信息、机票/酒店的智能推荐与解释 |

详细功能点参见 [需求划分文档](./docs/requirements.md)。

## 工程结构

### 前端 (Vue 3)

frontend/            
├── src/  
│   ├── api/              # API 接口（按业务拆分）  
│   ├── assets/styles/    # 全局样式（暗色主题、按钮、admin）  
│   ├── components/       # 公共组件（Header/Footer/ThemeToggle）  
│   ├── layouts/          # 布局（AdminLayout）  
│   ├── router/           # 路由 + 导航守卫  
│   ├── stores/           # Pinia 状态（auth）  
│   ├── utils/            # request 拦截器、auth 工具   
│   └── views/            # 页面视图（auth/admin/module）  
└── ...  

### 后端 (Spring Boot)  

backend/  
├── src/main/java/top/ortus/timemark/backend/  
│   ├── controller/       # REST 控制器（Auth/User/Admin/Crud/Module）  
│   ├── service/          # 业务逻辑  
│   ├── dao/              # MyBatis-Plus Mapper + 实体  
│   ├── dto/              # 请求/响应 DTO  
│   ├── config/           # WebConfig、数据库属性配置  
│   ├── security/         # JWT、AdminAuthInterceptor  
│   ├── exception/        # 全局异常处理  
│   └── converter/        # 实体-DTO 转换  
└── resources/            # application.yaml, SQL 脚本  

## 快速开始

### 前置要求
- Node.js ≥ 16, npm ≥ 7
- JDK 17, Maven ≥ 3.6
- MySQL 8.0

### 1. 克隆仓库

```bash
git clone https://github.com/your-org/lightmark.git
cd lightmark
```

### 2. 配置数据库

- 创建数据库 `lightmark` (utf8mb4)
- 执行 [`docs/database/schema.sql`](./docs/database/schema.sql) 建表
- 执行 [`docs/database/data.sql`](./docs/database/data.sql) 插入演示数据
- 修改后端 `application.yaml` 中的数据库连接（或使用环境变量）

### 3. 启动后端

```bash
cd backend
mvn clean package
java -jar target/backend-0.0.1-SNAPSHOT.jar
# 默认运行于 http://localhost:8080
```

### 4. 启动前端

```bash
cd frontend
npm install
npm run serve
# 默认运行于 http://localhost:8081
```

前端开发服务器已配置代理，`/api` 请求转发至 `http://localhost:8080`，可直接调用后端接口。

### 5. 访问应用

- 用户端：`http://localhost:8081`
- 管理后台：`http://localhost:8081/admin/login`（默认管理员账号 `admin@lightmark.com` / `123456`）

## AI 集成

## Skill 提示词

本项目使用了两个辅助 Skill 来优化设计和开发：

- 🎨 **前端设计 Skill**：[frontend-design](https://www.skill-cn.com/skill/10)  
  用于生成高质量、美观的前端组件和页面布局，特别适合快速构建响应式 UI 和后台管理界面。

- 🔧 **后端 API 设计 Skill**：[API design skill](https://skillsmp.com/zh/skills/affaan-m-everything-claude-code-agents-skills-api-design-skill-md)  
  帮助设计 RESTful API 结构、错误码、版本管理等，确保后端接口规范、易维护。

---

⭐ 如果这个项目对你有帮助，请给一个 Star 支持一下！
