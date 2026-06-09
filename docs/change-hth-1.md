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

- `backend/src/main/java/top/ortus/lightmark/backend/service/ItineraryService.java`
  - 新增智能行程模块服务接口。
  - 定义我的行程列表、创建、更新、删除、AI 生成、分享、导出等方法。

- `backend/src/main/java/top/ortus/lightmark/backend/service/ItineraryServiceImpl.java`
  - 实现智能行程模块业务逻辑。
  - 支持按当前登录用户查询行程。
  - 支持行程创建、编辑、删除。
  - 支持调用 AI 服务生成行程。
  - 支持分享行程，将私密行程切换为公开。
  - 支持返回导出地址。
  - 增加用户归属校验，避免操作他人行程。

### 新增社区服务

- `backend/src/main/java/top/ortus/lightmark/backend/service/CommunityService.java`
  - 新增社区模块服务接口。
  - 定义游记列表、详情、发布、编辑、删除、点赞、评论、问答等方法。

- `backend/src/main/java/top/ortus/lightmark/backend/service/CommunityServiceImpl.java`
  - 实现社区模块业务逻辑。
  - 支持游记分页列表、关键词搜索、最新/热门排序。
  - 支持游记详情、发布、编辑、软删除。
  - 支持点赞/取消点赞，并同步 `post.likes`。
  - 支持评论列表和发表评论，并同步 `post.comments_count`。
  - 支持问答列表、发布问题、查看问题、提交回答。
  - 增加作者权限校验，限制游记编辑和删除。

### 扩展 AI 服务

- `backend/src/main/java/top/ortus/lightmark/backend/service/AIService.java`
  - 新增 `generateTravelPlan(Map<String, Object> payload)` 方法。

- `backend/src/main/java/top/ortus/lightmark/backend/service/impl/AIServiceImpl.java`
  - 接入智能行程生成能力。
  - 新增 AI 行程 prompt 资源注入。
  - 支持解析模型返回的结构化行程 JSON。
  - 增加规则降级逻辑：当 AI Key 未配置或模型调用失败时，仍返回可用的每日行程。
  - 增加兼容旧单元测试的重载构造器。

- `backend/src/main/resources/prompts/travel_plan_generate.txt`
  - 新增智能行程生成提示词模板。

### 修改公共 API 控制器

- `backend/src/main/java/top/ortus/lightmark/backend/controller/PublicApiController.java`
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

- `backend/src/test/java/top/ortus/lightmark/backend/M6ItineraryCommunityIntegrationTests.java`
  - 新增 M6 行程与社区接口集成测试。
  - 覆盖社区游记列表、点赞、评论、提问、回答问题。
  - 覆盖 AI 生成行程、保存行程、查询我的行程。

- `backend/src/test/java/top/ortus/lightmark/backend/controller/UserPasswordControllerTest.java`
  - 启用 `test` Profile，使测试使用 H2 测试库。
  - 在测试前显式重置用户 2 的密码哈希，避免依赖不匹配的种子密码。
  - 使用独立 H2 内存库 `lightmark_password`，避免全量测试中 Spring 上下文重建时出现表重复创建问题。

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

- `backend/src/main/java/top/ortus/lightmark/backend/service/CommunityService.java`
  - 调整删帖方法签名，新增管理员权限参数。
  - 新增删除游记评论/回复的服务接口。

- `backend/src/main/java/top/ortus/lightmark/backend/service/CommunityServiceImpl.java`
  - 游记删除改为“发布者或管理员”可操作。
  - 新增评论/回复软删除逻辑，删除后同步扣减游记评论数。
  - 评论/回复删除限制为评论/回复发布者或管理员。

- `backend/src/main/java/top/ortus/lightmark/backend/controller/PublicApiController.java`
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

- `backend/src/main/java/top/ortus/lightmark/backend/dto/module/PostDTO.java`
- `backend/src/main/java/top/ortus/lightmark/backend/dto/module/CommentDTO.java`
- `backend/src/main/java/top/ortus/lightmark/backend/dto/module/QuestionDTO.java`
  - 新增社区展示用昵称字段：`user_nickname`、`answer_user_nickname`。
  - 保留 `user_id`、`answer_user_id` 作为权限判断和数据关联字段。

- `backend/src/test/java/top/ortus/lightmark/backend/M6ItineraryCommunityIntegrationTests.java`
  - 新增游记删除权限测试。
  - 新增评论删除权限测试。

- `docs/API.md`
  - 更新游记删除权限说明。
  - 补充删除游记评论/回复接口说明。

## 2026-06-01 游记点赞状态标识补充

- `backend/src/main/java/top/ortus/lightmark/backend/dto/module/PostDTO.java`
  - 新增 `liked` 字段，用于标识当前登录用户是否已点赞该游记。

- `backend/src/main/java/top/ortus/lightmark/backend/service/CommunityServiceImpl.java`
  - 游记列表和详情查询返回当前用户的点赞状态。
  - 保持点赞/取消点赞接口返回 `liked` 和最新点赞数。

- `frontend/src/api/community.ts`
  - `Post` 类型新增 `liked` 字段。

- `frontend/src/views/module/CommunityView.vue`
  - 游记点赞按钮根据 `liked` 状态显示“点赞”或“已赞”。
  - 点赞成功后即时更新按钮状态和点赞数。

## 2026-06-05 行程 AI 接口适配与目的地选择补充

- `backend/src/main/java/top/ortus/lightmark/backend/utils/AIClient.java`
  - 将模型请求体调整为 DeepSeek/OpenAI 兼容的 Chat Completions 格式。
  - 新增 `AI_MODEL` 配置，默认 `deepseek-chat`。
  - 保留 `AI_API_URL`、`AI_API_KEY` 缺失时的规则降级逻辑。

- `frontend/src/data/chinaAdministrativeAreas.ts`
  - 新增全国省/市/区县级联选择数据。
  - 覆盖全国省级行政区和主要城市，部分热门城市补充区县。

- `frontend/src/views/module/ItineraryView.vue`
  - 行程生成目的地由自由输入改为省/市/区县级联选择。
  - 生成前强制校验目的地必须来自选择器，避免任意文本直接生成。

- `docs/API.md`
  - 补充 `/api/itinerary/ai/generate` 的 AI 环境变量配置说明。
  - 标明前端目的地由级联选择器提交。

## 2026-06-05 行程 AI 部署配置补充

- `backend/src/main/resources/application.yaml`
  - 新增 `lightmark.ai.api-url`、`lightmark.ai.api-key`、`lightmark.ai.model` 应用配置。
  - 以上配置从部署环境变量 `AI_API_URL`、`AI_API_KEY`、`AI_MODEL` 读取，避免环境变量与 Spring 属性同名导致占位符循环解析。
  - 默认行程 AI 接口指向 DeepSeek Chat Completions：`https://api.deepseek.com/chat/completions`。
  - 默认模型调整为 `deepseek-v4-flash`。

- `backend/src/main/java/top/ortus/lightmark/backend/utils/AIClient.java`
  - 行程 AI 客户端改为读取 `lightmark.ai.*` 配置。
  - 当 `AI_API_KEY` 为空时，自动复用 `DEEPSEEK_API_KEY`，便于部署时只维护一个 DeepSeek Key。

- `docker-compose.yml`
  - 后端容器新增行程 AI 所需环境变量透传。
  - 支持从部署 `.env` 读取 `AI_API_URL`、`AI_API_KEY`、`AI_MODEL`。

- `.env.example`
  - 新增部署环境变量模板，包含数据库、JWT、DeepSeek 和行程 AI 配置。

- `scripts/test-itinerary-ai.ps1`
  - 新增本地/服务器行程 AI 接口验证脚本。
  - 可通过 `-BaseUrl` 和 `-Token` 参数请求 `/api/itinerary/ai/generate`。

## 2026-06-05 行程内容细化与用户批注补充

- `backend/src/main/resources/prompts/travel_plan_generate.txt`
  - 要求模型生成更具体的每日安排，包括经典地点、博物馆/展馆、特色美食和礼品伴手礼。
  - `classicPlaces`、`museums`、`foods`、`souvenirs` 改为可选字段，仅在当天正文明确提到对应内容时输出。
  - 避免为了凑模块额外生成当天正文未安排的景点、场馆、美食或礼品。

- `backend/src/main/java/top/ortus/lightmark/backend/service/impl/AIServiceImpl.java`
  - 规则降级行程同步扩展为更具体的结构化内容。
  - 未配置 AI Key 或模型调用失败时，也只会返回当天正文实际涉及的经典地点、场馆、美食和伴手礼建议。

- `frontend/src/views/module/ItineraryView.vue`
  - 行程预览新增经典地点、博物馆/展馆、特色美食、礼品伴手礼分区展示。
  - 每日行程新增“批注修改”入口，用户可修改安排、地点、场馆、美食、伴手礼、提醒和个人批注。
  - 批注修改会写回当前 `plan_data`，点击保存/更新行程后同步入库。
  - 清空某个可选模块后，保存的 `plan_data` 不再保留该空字段，预览也不会展示。

## 2026-06-05 社区游记图文编辑与对象存储上传补充

- `backend/src/main/java/top/ortus/lightmark/backend/service/ObjectStorageService.java`
  - 新增游记图片上传方法，复用对象存储 PAR URL PUT 上传方式。
  - 游记图片统一转存为 jpg，文件名格式为 `post-{user_id}-{yyyyMMddHHmmss}-{random}.jpg`。
  - 保留头像上传命名 `avatar-{user_id}.jpg`。

- `backend/src/main/java/top/ortus/lightmark/backend/controller/PublicApiController.java`
  - `/api/upload/image` 由占位接口改为真实 `multipart/form-data` 图片上传接口。
  - 上传成功后返回对象存储 URL。

- `frontend/src/api/community.ts`
  - 新增 `uploadCommunityImage`，用于游记编辑器上传图片。

- `frontend/src/views/module/CommunityView.vue`
  - 游记详情改为类似帖子阅读流：帖子正文在上，向下滚动查看评论和发布评论。
  - 游记发布/编辑增加 Markdown 编辑与预览。
  - 支持多图上传、对象存储 URL 回填、图集预览、移除图片和将图片插入正文。
  - 游记内容展示支持常用 Markdown：标题、加粗、列表、引用、行内代码和图片。

- `docs/API.md`
  - 将 `/api/upload/image` 标记为已实现。
  - 补充 multipart 上传方式、对象存储返回 URL 和文件命名规则。

## 2026-06-05 社区点赞/发布异常处理补充

- `backend/src/main/java/top/ortus/lightmark/backend/service/CommunityServiceImpl.java`
  - 在发帖、点赞、评论、提问和回答等会写入 `user_id` 的接口前增加用户存在性校验。
  - 当前登录 token 对应用户不存在时，提前返回 `401 登录状态已失效，请重新登录`，避免 MySQL 外键错误直接暴露到前端。
  - 删除游记和删除评论仍保持原有权限判断语义，非作者普通用户返回 `403`。

- `frontend/src/views/module/CommunityView.vue`
  - 为发布游记、点赞、评论和图片上传增加局部错误处理。
  - 请求失败时由统一请求拦截器展示错误提示，页面不再被开发环境 runtime error 红屏覆盖。

## 2026-06-05 社区与问答删除权限补充

- `backend/src/main/java/top/ortus/lightmark/backend/service/CommunityService.java`
  - 新增 `deleteQuestion`、`deleteAnswer` 服务方法。
  - `answerQuestion` 增加管理员身份参数，用于校验已有回答的修改权限。

- `backend/src/main/java/top/ortus/lightmark/backend/service/CommunityServiceImpl.java`
  - 游记评论删除权限调整为：评论发布者、对应游记发布者、管理员可删除；其他用户返回 `403`。
  - 问题删除权限调整为：问题发布者或管理员可删除。
  - 回答删除权限调整为：回答发布者或管理员可删除，删除后问题恢复为待回答状态。
  - 已有回答仅回答发布者或管理员可修改，避免其他用户覆盖他人回答。

- `backend/src/main/java/top/ortus/lightmark/backend/controller/PublicApiController.java`
  - 新增 `DELETE /api/questions/{id}`。
  - 新增 `DELETE /api/questions/{id}/answer`。
  - `POST /api/questions/{id}/answer` 接入管理员权限判断。

- `frontend/src/api/community.ts`
  - 新增 `deleteQuestion`、`deleteQuestionAnswer` 请求封装。

- `frontend/src/views/module/CommunityView.vue`
  - 问答列表按权限显示“回答/编辑回答”“删除回答”“删除问题”按钮。
  - 评论删除按钮新增游记发布者权限判断。
  - 发布问题、回答问题、删除问题、删除回答增加局部异常兜底。

- `backend/src/test/java/top/ortus/lightmark/backend/M6ItineraryCommunityIntegrationTests.java`
  - 新增游记发布者删除自己游记下他人评论的权限测试。
  - 新增问题删除、回答删除、已有回答防覆盖的权限测试。

- `docs/API.md`
  - 补充问答删除接口和已有回答修改权限说明。

## 2026-06-05 社区作者权限识别与问答单回答修复

- `frontend/src/views/module/CommunityView.vue`
  - 修复前端作者权限识别：登录态 `userId` 为 16 位格式化 ID，而社区数据 `user_id` 为数据库数字 ID，统一归一化后再比较。
  - 修复游记、评论、问题、回答发布者本人看不到删除/编辑按钮的问题。
  - 问答回答入口增加 `status`、`answer_user_id`、`answer` 综合判断，已回答问题不再向其他普通用户展示回答按钮。

- `backend/src/main/java/top/ortus/lightmark/backend/service/CommunityServiceImpl.java`
  - 后端回答权限加严：只要问题 `status = 1`、已有回答内容或已有回答者，即视为已回答。
  - 已回答问题仅回答发布者或管理员可修改，防止多人重复回答覆盖。

## 2026-06-05 评论删除超时红屏修复

- `frontend/src/views/module/CommunityView.vue`
  - 为打开游记详情、打开评论、删除游记、删除评论增加局部异常兜底，避免请求超时触发 Vue 开发环境红屏。
  - 评论删除成功后改为本地移除评论并同步评论数，不再强制重新请求游记详情和评论列表。
  - 删除后的帖子列表刷新改为后台补偿刷新，刷新超时不会影响当前已完成的删除操作。

## 2026-06-05 游记图片上传部署适配

- `backend/src/main/java/top/ortus/lightmark/backend/service/ObjectStorageService.java`
  - 对对象存储上传增加连接超时和 PUT 请求超时，避免上传请求长时间挂起。
  - 对象存储上传失败时返回更明确的错误信息，便于定位配置或网络问题。

- `backend/src/main/resources/application.yaml`
  - 增加 Spring multipart 上传大小限制：单文件 5MB，请求 6MB。
  - `lightmark.object-storage.base-url` 默认指向 `docs/对象存储.md` 中的 Oracle Object Storage PAR baseurl，也可通过 `OBJECT_STORAGE_BASE_URL` 覆盖。

- `.env.example`
  - 补充 `OBJECT_STORAGE_BASE_URL` 示例值，部署时可直接使用或替换为新的 PAR 地址。

- `frontend/src/api/community.ts`
  - 游记图片上传接口单独设置 60 秒超时，避免沿用普通请求 15 秒超时。

- `frontend/src/views/module/CommunityView.vue`
  - 上传前校验文件类型和大小，仅允许图片且单张不超过 5MB。
  - 单篇游记最多上传 9 张图片，超过部分自动忽略并提示。

## 2026-06-05 社区列表加载超时红屏修复

- `frontend/src/views/module/CommunityView.vue`
  - `loadPosts`、`loadQuestions` 增加局部异常兜底，请求超时或失败时由统一拦截器提示，不再触发 Vue 开发环境红屏。
  - 页面初始化不再使用 `Promise.all` 等待游记和问答两个列表全部成功，避免单个列表超时导致整个社区页异常。

- `frontend/src/utils/request.ts`
  - 普通接口请求超时时间由 15 秒调整为 30 秒；图片上传接口仍保留单独的 60 秒超时。

## 2026-06-05 PNG 图片转存修复

- `backend/src/main/java/top/ortus/lightmark/backend/service/ObjectStorageService.java`
  - 修复 PNG 等带透明通道图片转存为 jpg 时返回 `invalid image` 的问题。
  - 非 jpg 图片写入对象存储前先合成白底 `TYPE_INT_RGB` 图片，再输出 jpg。

## 2026-06-05 游记图片上传响应优化

- `frontend/src/views/module/CommunityView.vue`
  - 上传前在浏览器端压缩图片：最长边限制为 1600px，jpg 质量为 0.82。
  - 小于 900KB 的 jpg 原样上传，避免不必要的二次压缩。
  - PNG、透明图和大图优先在前端转为白底 jpg，减少后端图片解码转换和对象存储上传流量。

## 2026-06-05 游记详情与删除响应优化

- `frontend/src/views/module/CommunityView.vue`
  - 打开游记详情时，帖子详情和评论列表改为并行加载，减少等待时间。
  - 删除游记成功后立即本地移除卡片并提示成功，不再等待列表重新加载完成。
  - 删除后的列表刷新改为后台补偿刷新，避免网络慢时造成“删除很慢”的体感。

## 2026-06-05 行程生成内容具体化

- `backend/src/main/resources/prompts/travel_plan_generate.txt`
  - 强化 AI 提示词，要求输出真实具体名称。
  - 禁止输出“核心景区”“经典地标”“当地招牌菜”“特色小吃拼盘”等泛化占位词。
  - 增加杭州示例，要求景区、美食、场馆使用专名。

- `backend/src/main/java/top/ortus/lightmark/backend/service/impl/AIServiceImpl.java`
  - 为规则兜底行程增加城市旅行知识库，覆盖杭州、成都、北京、上海、西安、三亚。
  - 兜底行程改为输出具体景区、博物馆/展馆、街区、美食和伴手礼。
  - 杭州可输出西湖、断桥残雪、苏堤、灵隐寺、法喜寺、浙江省博物馆、中国茶叶博物馆、片儿川、葱包桧、定胜糕、龙井虾仁等具体内容。
## 2026-06-06 �μ����������ť����
- `frontend/src/views/module/CommunityView.vue`
  - ȥ������ײ��������еġ��ղء���ť��
  - ����������ť��Ϊ���Ƶ�ǰ�μ����ӵ������塣
  - Clipboard API ������ʱʹ����ʱ textarea ���׸��ơ�
