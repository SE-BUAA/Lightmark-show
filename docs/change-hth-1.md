# change-hth-1

本文档记录本次围绕“模块六：智能行程与社区模块（M6）”完成的变更内容。

## 后端变更

### 新增数据库迁移

- `backend/src/main/resources/db/migration/V20260608__m6_itinerary_community.sql`
  - 新增 `travel_plan` 行程计划表。
  - 新增 `post` 游记表。
  - 新增 `post_like` 游记点赞表。
  - 新增 `comment` 评论表。
  - 新增 `question` 问答表。
  - 写入少量 M6 演示数据，包括公开行程、游记、评论和问答。

### 更新测试数据库资源

- `backend/src/test/resources/schema-h2.sql`
  - 补充 H2 测试环境中的 M6 表结构：`travel_plan`、`post`、`post_like`、`comment`、`question`。

- `backend/src/test/resources/data-h2.sql`
  - 补充 H2 测试环境中的 M6 示例数据。

### 新增智能行程服务

- `backend/src/main/java/top/ortus/timemark/backend/service/ItineraryService.java`
  - 新增智能行程模块服务接口。
  - 定义我的行程列表、创建、更新、删除、AI 生成、分享、导出等方法。

- `backend/src/main/java/top/ortus/timemark/backend/service/ItineraryServiceImpl.java`
  - 实现智能行程模块业务逻辑。
  - 支持按当前登录用户查询行程。
  - 支持行程创建、编辑、删除。
  - 支持调用 AI 服务生成行程。
  - 支持分享行程，将私密行程切换为公开。
  - 支持返回导出地址。
  - 增加用户归属校验，避免操作他人行程。

### 新增社区服务

- `backend/src/main/java/top/ortus/timemark/backend/service/CommunityService.java`
  - 新增社区模块服务接口。
  - 定义游记列表、详情、发布、编辑、删除、点赞、评论、问答等方法。

- `backend/src/main/java/top/ortus/timemark/backend/service/CommunityServiceImpl.java`
  - 实现社区模块业务逻辑。
  - 支持游记分页列表、关键词搜索、最新/热门排序。
  - 支持游记详情、发布、编辑、软删除。
  - 支持点赞/取消点赞，并同步 `post.likes`。
  - 支持评论列表和发表评论，并同步 `post.comments_count`。
  - 支持问答列表、发布问题、查看问题、提交回答。
  - 增加作者权限校验，限制游记编辑和删除。

### 扩展 AI 服务

- `backend/src/main/java/top/ortus/timemark/backend/service/AIService.java`
  - 新增 `generateTravelPlan(Map<String, Object> payload)` 方法。

- `backend/src/main/java/top/ortus/timemark/backend/service/impl/AIServiceImpl.java`
  - 接入智能行程生成能力。
  - 新增 AI 行程 prompt 资源注入。
  - 支持解析模型返回的结构化行程 JSON。
  - 增加规则降级逻辑：当 AI Key 未配置或模型调用失败时，仍返回可用的每日行程。
  - 增加兼容旧单元测试的重载构造器。

- `backend/src/main/resources/prompts/travel_plan_generate.txt`
  - 新增智能行程生成提示词模板。

### 修改公共 API 控制器

- `backend/src/main/java/top/ortus/timemark/backend/controller/PublicApiController.java`
  - 将原本 M6 的占位接口改为真实业务调用。
  - 注入 `ItineraryService` 和 `CommunityService`。
  - 实现以下行程接口的真实转发：
    - `GET /api/itinerary/my-plans`
    - `POST /api/itinerary/plans`
    - `PUT /api/itinerary/plans/{id}`
    - `DELETE /api/itinerary/plans/{id}`
    - `POST /api/itinerary/ai/generate`
    - `GET /api/itinerary/plans/{id}/share`
    - `GET /api/itinerary/plans/{id}/export`
  - 实现以下社区接口的真实转发：
    - `GET /api/posts`
    - `GET /api/posts/{id}`
    - `POST /api/posts`
    - `PUT /api/posts/{id}`
    - `DELETE /api/posts/{id}`
    - `POST /api/posts/{id}/like`
    - `GET /api/posts/{id}/comments`
    - `POST /api/posts/{id}/comments`
    - `GET /api/questions`
    - `GET /api/questions/{id}`
    - `POST /api/questions`
    - `POST /api/questions/{id}/answer`

### 补充 M6 集成测试和既有测试修复

- `backend/src/test/java/top/ortus/timemark/backend/M6ItineraryCommunityIntegrationTests.java`
  - 新增 M6 行程与社区接口集成测试。
  - 覆盖社区游记列表、点赞、评论、提问、回答问题。
  - 覆盖 AI 生成行程、保存行程、查询我的行程。

- `backend/src/test/java/top/ortus/timemark/backend/controller/UserPasswordControllerTest.java`
  - 启用 `test` Profile，使测试使用 H2 测试库。
  - 在测试前显式重置用户 2 的密码哈希，避免依赖不匹配的种子密码。
  - 使用独立 H2 内存库 `timemark_password`，避免全量测试中 Spring 上下文重建时出现表重复创建问题。

## 前端变更

### 新增前端 API 封装

- `frontend/src/api/itinerary.ts`
  - 新增行程模块请求封装。
  - 包含我的行程、创建、更新、删除、AI 生成、分享、导出接口。
  - 定义 `TravelPlan`、`GeneratePlanPayload`、`PageResponse` 类型。

- `frontend/src/api/community.ts`
  - 新增社区模块请求封装。
  - 包含游记列表、详情、发布、编辑、删除、点赞、评论、问答接口。
  - 定义 `Post`、`Comment`、`Question` 类型。

### 改造智能行程页面

- `frontend/src/views/module/ItineraryView.vue`
  - 原占位页改造为可操作页面。
  - 新增 AI 行程生成表单：目的地、天数、出发日期、预算、偏好。
  - 新增行程预览区域，展示每日主题、安排和提醒。
  - 新增我的行程列表。
  - 新增保存行程、分享行程、导出行程、删除行程功能。
  - 补充已保存行程的选中态、编辑弹窗和更新保存能力。
  - 增加每日安排 JSON 校验，避免编辑时写入格式错误的 `plan_data`。
  - 使用 Element Plus 表单、按钮、开关和反馈组件。
  - 增加响应式布局，适配移动端。

### 改造社区页面

- `frontend/src/views/module/CommunityView.vue`
  - 原占位页改造为可操作社区页。
  - 新增游记和问答两个 Tab。
  - 游记区支持搜索、最新/热门排序、发布游记、点赞、查看评论、发表评论。
  - 问答区支持搜索问题、发布问题、回答问题。
  - 新增发布游记、评论、提问、回答弹窗。
  - 补充游记详情弹窗、游记编辑、软删除和右下角快速发布入口。
  - 补充发布、评论、提问、回答过程中的加载状态与图片 JSON 输入校验。
  - 使用 Element Plus Tabs、Dialog、Form、Tag 等组件。
  - 增加响应式布局。

## 文档变更

- `docs/API.md`
  - 将行程、社区与评价模块中的 M6 接口状态由“待实现”更新为“已实现”。
  - 补充智能行程、游记社区、问答社区的请求参数、请求体和响应示例。
  - 标明需要 `Authorization: Bearer <token>` 的接口调用约定。

## 验证情况

- 前端执行 `npm run build` 成功。
  - 仅存在构建产物体积较大的警告，未阻断构建。
  - 前端完成项补充后再次执行 `npm run build` 成功。

- 后端执行 `mvn test` 成功。
  - 测试结果：`Tests run: 47, Failures: 0, Errors: 0, Skipped: 0`。
  - 已单独验证 `UserPasswordControllerTest`、`M6ItineraryCommunityIntegrationTests`、`AIServiceImplTest`、`FlightSearchApiIntegrationTests` 等关键用例。
  - 前端补充完成后再次执行后端全量测试，结果仍为 `Tests run: 47, Failures: 0, Errors: 0, Skipped: 0`。

## 注意事项

- 工作区中已有 `frontend/package-lock.json` 修改，该文件不是本次 M6 代码逻辑主动变更，未进行回退。
- M6 目前已具备前后端主流程，前端构建和后端全量测试均已通过。
- 本地前后端联调时不要直接使用后端默认配置启动；默认 `application.yaml` 会读取外部 MySQL 配置。建议先准备本地数据库或显式覆盖 `spring.datasource.*` 后再启动，避免把联调数据写入公共数据库。

## 2026-06-01 游记与问答权限补充

- `backend/src/main/java/top/ortus/timemark/backend/service/CommunityService.java`
  - 调整删帖方法签名，新增管理员权限参数。
  - 新增删除游记评论/回复的服务接口。

- `backend/src/main/java/top/ortus/timemark/backend/service/CommunityServiceImpl.java`
  - 游记删除改为“发布者或管理员”可操作。
  - 新增评论/回复软删除逻辑，删除后同步扣减游记评论数。
  - 评论/回复删除限制为评论/回复发布者或管理员。

- `backend/src/main/java/top/ortus/timemark/backend/controller/PublicApiController.java`
  - 新增 `DELETE /api/posts/{postId}/comments/{commentId}`。
  - 从 JWT 中解析管理员身份，用于游记和评论删除权限判断。

- `frontend/src/api/community.ts`
  - 新增 `deletePostComment` 请求封装。

- `frontend/src/views/module/CommunityView.vue`
  - 游记卡片和游记详情展示发布者昵称。
  - 问答列表展示提问者昵称，已回答问题展示回答者昵称。
  - 评论列表展示评论者昵称。
  - 游记编辑仅发布者可见，游记删除仅发布者或管理员可见。
  - 评论/回复删除仅评论/回复发布者或管理员可见。

- `backend/src/main/java/top/ortus/timemark/backend/dto/module/PostDTO.java`
- `backend/src/main/java/top/ortus/timemark/backend/dto/module/CommentDTO.java`
- `backend/src/main/java/top/ortus/timemark/backend/dto/module/QuestionDTO.java`
  - 新增社区展示用昵称字段：`user_nickname`、`answer_user_nickname`。
  - 保留 `user_id`、`answer_user_id` 作为权限判断和数据关联字段。

- `backend/src/test/java/top/ortus/timemark/backend/M6ItineraryCommunityIntegrationTests.java`
  - 新增游记删除权限测试。
  - 新增评论删除权限测试。

- `docs/API.md`
  - 更新游记删除权限说明。
  - 补充删除游记评论/回复接口说明。

## 2026-06-01 游记点赞状态标识补充

- `backend/src/main/java/top/ortus/timemark/backend/dto/module/PostDTO.java`
  - 新增 `liked` 字段，用于标识当前登录用户是否已点赞该游记。

- `backend/src/main/java/top/ortus/timemark/backend/service/CommunityServiceImpl.java`
  - 游记列表和详情查询返回当前用户的点赞状态。
  - 保持点赞/取消点赞接口返回 `liked` 和最新点赞数。

- `frontend/src/api/community.ts`
  - `Post` 类型新增 `liked` 字段。

- `frontend/src/views/module/CommunityView.vue`
  - 游记点赞按钮根据 `liked` 状态显示“点赞”或“已赞”。
  - 点赞成功后即时更新按钮状态和点赞数。
