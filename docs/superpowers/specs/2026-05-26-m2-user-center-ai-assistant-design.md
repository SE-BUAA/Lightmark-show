## M2｜用户中心与 AI 智能助手（悬浮 AI 客服）设计说明

### 1. 背景与目标

本设计覆盖模块二（M2）交付范围：用户中心与 AI 智能助手（以“悬浮 AI 客服组件”为主要入口），并以 `docs/API.md` 为接口口径，实现“可用、可演示、功能完整”的前后端闭环。

约束：

- AI 走 DeepSeek，密钥仅在后端保存，前端不直连大模型。
- 现阶段优先保证功能可正常使用；持久化与复杂权限策略可后续增强。

### 2. 交付范围

#### 2.1 用户中心（前后端联动）

- 个人资料：展示与编辑（昵称/手机号/邮箱/头像占位）
- 常用出行人：CRUD
- 积分/等级：积分明细、等级升级信息
- 我的订单：列表与详情
- 安全设置：修改密码、退出登录（前端已具备基础登出）

#### 2.2 AI 智能助手（悬浮 AI 客服）

- 悬浮入口：全站可唤起（不影响路由结构）
- 基础对话：非流式 `POST /api/chat`
- 可选流式：SSE `POST /api/chat/stream`
- 会话管理：获取上下文、重置会话

### 3. 采用的接口口径

以 [API.md](file:///c:/Users/tangs/OneDrive/Desktop/Lightmark-main/docs/API.md#L700-L818) 为准：

- `POST /api/chat`（已存在，但需要完善 systemPrompt 生效）
- `POST /api/chat/region/complete`（已存在）
- `POST /api/chat/stream`（已存在）
- `GET /api/chat/context/{sessionId}`（补齐）
- `POST /api/chat/context/{sessionId}/reset`（补齐）

用户中心接口以 `/api/user/*` 为准，代码现状与文档基本对齐：

- `GET /api/user/current`
- `PUT /api/user/current`
- `POST /api/user/avatar`（前端先占位 URL 方式）
- `PUT /api/user/password`（需从占位改为真实校验/更新）
- `GET/POST/PUT/DELETE /api/user/travelers`
- `GET /api/user/points/logs`
- `GET /api/user/level/upgrade-info`
- `GET /api/user/orders`
- `GET /api/user/orders/{orderNo}`

### 4. 后端设计

#### 4.1 会话上下文与 systemPrompt

现状：`ChatRequest` 包含 `systemPrompt`，但 `ConversationController` 调用 `conversationService.chat(sessionId, message)` 丢失该字段。

设计：

- 支持“会话级 systemPrompt”：同一 `sessionId` 首次设置后持久化在内存结构中（Map），后续可被覆盖。
- 对话历史继续使用 `ConcurrentHashMap<String, List<Message>> conversations`（内存存储）。
- 增加历史裁剪（避免无限增长）：例如保留最近 N 条消息（N 可配置，默认 30）。

#### 4.2 新增接口：context 与 reset

- `GET /api/chat/context/{sessionId}`
  - 返回：当前会话历史（包含 role + content）
  - 说明：先提供简单返回结构，后续再分页/裁剪参数化
- `POST /api/chat/context/{sessionId}/reset`
  - 行为：清空该 session 的历史与 systemPrompt
  - 返回：`ApiResponse<Boolean>` 或 `ApiResponse<Object>`（以现有 ApiResponse 约定为准）

#### 4.3 修改密码（安全设置）

现状：`UserController.updatePassword` 返回占位 `true`。

设计：

- 通过 `JwtTokenService` 解析当前用户 ID
- 读取数据库中该用户的 `password`（BCrypt hash）
- 校验 `oldPassword`：`BCryptPasswordEncoder.matches`
- 写入 `newPassword`：`BCryptPasswordEncoder.encode` 后更新 user 表
- 返回 `ApiResponse<Boolean>`

### 5. 前端设计

#### 5.1 悬浮 AI 客服组件（全站入口）

组件位置：

- 新增 `frontend/src/components/ai/FloatingAssistant.vue`
- 在 `App.vue` 全局挂载，确保所有页面都能唤起

交互：

- 右下角悬浮按钮（展开/收起）
- 展开后为聊天面板：消息列表、输入框、发送按钮、清空会话按钮
- sessionId 规则：优先使用登录用户 `authStore.userId`；未登录则使用本地生成的随机 ID（localStorage 持久化）

功能：

- 非流式发送：`POST /api/chat`
- 可选切换流式：`POST /api/chat/stream`（SSE）
- 面板首次打开时拉取历史：`GET /api/chat/context/{sessionId}`
- 清空会话：`POST /api/chat/context/{sessionId}/reset`

#### 5.2 用户中心页面增强

现状页面：`frontend/src/views/module/UserCenterView.vue` 仅覆盖“个人资料展示/编辑 + 退出登录”。

设计增强：

- 在同一页面增加 Tab/分区：
  - 出行人管理：列表 + 新增/编辑弹窗 + 删除
  - 积分明细：列表展示（分页后续增强）
  - 等级升级信息：展示当前等级、距下一等级积分差、权益文案
  - 我的订单：列表 + 点击查看详情
  - 安全设置：修改密码表单（旧密码/新密码/确认新密码）

#### 5.3 前端 API 模块

- 新增 `frontend/src/api/chat.ts`
  - `chat(sessionId, message, systemPrompt?)`
  - `streamChat(sessionId, message)`（返回 EventSource / fetch SSE 方案二选一）
  - `getContext(sessionId)`
  - `resetContext(sessionId)`

### 6. 错误处理与安全

- 前端不记录/打印任何 API Key、token
- 后端异常统一走 `ApiResponse` 错误封装（沿用现有全局异常处理）
- SSE 断开时前端允许重试；后端发送异常时发 `error` 事件
- 密码修改失败返回明确错误码与 msg（例如旧密码不正确）

### 7. 验收清单（人工测试）

- 登录后打开任意页面，右下角出现悬浮 AI 客服入口；可正常对话并保持上下文
- “清空会话”后再次打开历史为空
- 个人中心可读写昵称/手机号/邮箱
- 出行人可新增/编辑/删除
- 积分明细、等级信息、订单列表可正常展示（无数据时也能正常显示空态）
- 修改密码：旧密码正确可修改；旧密码错误提示失败

