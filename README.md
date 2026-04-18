# Timemark

模块一（M1）已落地基础骨架：
- 后端：Spring Boot + JWT + 管理后台基础接口
- 前端：Vue3 + TypeScript + Router + Axios + 高质感旅游官网首页 + 响应式后台页面
- 预留：M2~M6 占位接口与占位页面

## 快速启动

### 1) 启动后端
```powershell
Set-Location D:\Code\projects\timemark\backend
.\mvnw.cmd spring-boot:run
```

### 2) 启动前端
```powershell
Set-Location D:\Code\projects\timemark\frontend
npm install
npm run dev
```

### 3) 代码检查 / 构建
```powershell
Set-Location D:\Code\projects\timemark\frontend
npm run typecheck
npm run build
```

### 4) 访问
- 前端：`http://localhost:5173`
- 后端健康检查：`http://localhost:8080/api/health`

## 关键接口
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/logout`
- `GET /api/auth/me`
- `GET /api/admin/dashboard/summary`
- `GET /api/admin/products`
- `GET /api/admin/orders`
- `GET /api/admin/users`
- `GET /api/m2/ping` 到 `GET /api/m6/ping`（预留）

## 默认说明
- 数据库连接参数默认已写入 `backend/src/main/resources/application.yaml`，可通过环境变量覆盖。
- 若数据库初始化数据密码不可用，可先通过前端“快速注册测试账号”创建新账号。

