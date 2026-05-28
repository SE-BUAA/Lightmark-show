# 拾光旅行（Lightmark）API 设计文档 v2.0

## 1. 通用规范

### 1.1 基础路径
所有接口以 `/api` 开头。例如：`/api/auth/login`

### 1.2 统一响应格式

```json
{
  "code": 0,           // 0=成功，其他=失败，具体错误码见附录
  "msg": "success",    // 错误时返回描述信息
  "data": { ... }      // 业务数据，可以是对象、数组、分页结构等
}
```

**分页响应结构**（用于列表接口）：

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "total": 100,
    "page": 1,
    "size": 10,
    "list": [ ... ]
  }
}
```

### 1.3 认证方式
除了登录/注册/健康检查，所有接口需在 `Authorization` 头中携带 JWT：  
`Authorization: Bearer <token>`

管理员接口需额外校验 `ADMIN` 角色。

### 1.4 查询参数标准（用于列表筛选/排序/分页）

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| `page` | int | 页码，从1开始，默认1 | `?page=2` |
| `size` | int | 每页条数，默认10，最大100 | `?size=20` |
| `sort` | string | 排序字段，默认 `create_time`，支持多字段逗号分隔，字段前加`-`表示降序 | `?sort=-price,create_time` |
| `filters` | string | **高级筛选**：JSON格式，支持 `eq`, `ne`, `gt`, `ge`, `lt`, `le`, `like`, `in` 等 | `?filters={"price":{"gte":500},"status":{"eq":1}}` |
| 单字段快速筛选 | 直接传参数 | 简单等值查询，如 `?status=1&productType=FLIGHT` | `?status=1&productType=FLIGHT` |

> 后端实现时，简单参数自动转为等值筛选，复杂需求使用 `filters` 参数。

### 1.5 错误码（部分）

| Code | 含义 |
|------|------|
| 0 | 成功 |
| 400 | 参数错误 |
| 401 | 未登录/token无效 |
| 403 | 无权限（非管理员） |
| 404 | 资源不存在 |
| 409 | 业务冲突（如重复预订） |
| 500 | 服务器内部错误 |

---

## 2. 模块一：认证与用户（M1 + M2 基础）

### 2.1 认证

| 方法 | 路径 | 说明 | 请求体 | 响应（data） |
|------|------|------|--------|--------------|
| POST | `/auth/register` | 注册 | `{account, password, nickname}` | `UserDTO` |
| POST | `/auth/login` | 登录 | `{account, password}` | `{token, userId, nickname, avatar, roles}` |
| POST | `/auth/logout` | 登出 | 无 | `true` |

### 2.2 当前登录用户信息

| 方法 | 路径 | 说明 | 响应（data） |
|------|------|------|--------------|
| GET | `/user/current` | 获取当前用户完整信息（含角色、权限） | `UserCurrentDTO`（含 `roles`, `permissions`, `points`, `level` 等） |
| PUT | `/user/current` | 更新当前用户个人信息 | `UserDTO` |
| POST | `/user/avatar` | 上传头像（multipart） | `{avatarUrl}` |
| PUT | `/user/password` | 修改密码 | `{oldPassword, newPassword}` → `true` |

**UserCurrentDTO** 示例：

```json
{
  "id": 1,
  "phone": "138****0000",
  "email": "admin@example.com",
  "nickname": "张三",
  "avatar": "https://...",
  "points": 1200,
  "level": 2,
  "roles": ["USER"],
  "permissions": ["flight:search", "hotel:book", ...]
}
```

### 2.3 常用出行人管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/user/travelers` | 当前用户的所有出行人（不分页） |
| POST | `/user/travelers` | 新增出行人 |
| PUT | `/user/travelers/{id}` | 修改出行人 |
| DELETE | `/user/travelers/{id}` | 删除出行人 |

> 出行人 `TravelerDTO` 字段：`id, name, idCard, phone, idType`

### 2.4 积分与会员

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/user/points/logs` | 积分明细（分页，可筛选 `type`, `source`） |
| GET | `/user/level/upgrade-info` | 等级升级所需积分/权益展示 |

### 2.5 我的订单（聚合）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/user/orders` | 当前用户所有订单（分页，可按 `status`/`orderType` 筛选） |
| GET | `/user/orders/{orderNo}` | 订单详情（根据订单类型返回对应明细DTO） |

---

## 3. 模块二：产品搜索与展示（机票/酒店/火车/度假）

所有产品列表接口均支持：
- 分页 `page`, `size`
- 排序 `sort`（价格、出发时间、评分等）
- 筛选（出发地、目的地、日期、价格区间等）

### 3.1 公共产品接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/products` | **全局产品搜索**（所有类型混合，支持关键词、类型筛选） |
| GET | `/products/{id}` | 单个产品详情（根据 `product_type` 返回不同 `extra` 结构） |

### 3.2 机票（Flight）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/flights/search` | **机票搜索**（出发地、目的地、日期、人数、舱位等） |
| GET | `/flights/price-calendar` | 价格日历（某航线未来30天最低价） |
| GET | `/flights/{productId}` | 航班详情（含舱位、行李额、退改规则） |
| POST | `/flights/order/preview` | 机票下单预览（计算价格、校验库存） |
| POST | `/flights/order` | 创建机票订单（返回 `OrderDTO` 及支付信息） |

**机票搜索参数示例**：
```
GET /flights/search?departureCity=BJS&arrivalCity=SHA&departureDate=2026-05-20&returnDate=2026-05-27&adultCount=2&childCount=1&cabin=ECONOMY
```

### 3.3 酒店（Hotel）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/hotels/search` | 酒店搜索（城市、入住/离店日期、房间数、人数） |
| GET | `/hotels/{hotelId}/rooms` | 酒店房型列表（含房态、价格） |
| GET | `/hotels/{hotelId}/reviews` | 酒店评价（分页） |
| POST | `/hotels/order` | 创建酒店订单 |

### 3.4 火车票（Train）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/trains/search` | 车次搜索（出发站、到达站、日期、坐席类型） |
| GET | `/trains/{productId}` | 车次详情及坐席余票 |
| POST | `/trains/order` | 创建火车票订单（支持学生票） |

### 3.5 度假产品（Vacation）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/vacations/search` | 度假产品搜索（目的地、天数、价格区间、跟团/自由行） |
| GET | `/vacations/{productId}` | 度假产品详情（行程安排、费用说明、酒店参考） |
| POST | `/vacations/order` | 创建度假订单 |

---

## 4. 模块三：订单与支付

### 4.1 订单操作（通用）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/orders/{orderNo}/pay` | **模拟支付**（请求体：`{paymentMethod: "WECHAT"}`） |
| POST | `/orders/{orderNo}/cancel` | 取消订单（未支付或可退改时） |
| POST | `/orders/{orderNo}/refund` | 申请退款（已支付，返回退款金额） |
| GET | `/orders/{orderNo}/status` | 订单状态跟踪 |

> 退改规则根据订单类型和产品政策动态计算，由后端 `OrderService` 处理。

### 4.2 支付回调（模拟）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/payment/callback` | 模拟支付回调（修改订单状态，增加积分，记录支付记录） |

---

## 5. 模块四：智能行程与社区

### 5.1 手动/智能行程规划

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/itinerary/my-plans` | 我的行程列表（分页） |
| POST | `/itinerary/plans` | 手动创建/保存行程（`TravelPlanDTO`） |
| PUT | `/itinerary/plans/{id}` | 修改行程 |
| DELETE | `/itinerary/plans/{id}` | 删除行程 |
| POST | `/itinerary/ai/generate` | **AI 智能生成行程**（请求体：`{destination, days, preferences, budget}`，返回 `TravelPlanDTO` 结构） |
| GET | `/itinerary/plans/{id}/share` | 生成分享链接（返回短链接） |
| GET | `/itinerary/plans/{id}/export` | 导出 PDF/图片 |

### 5.2 游记与互动

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/posts` | 游记列表（支持按时间、热度排序，标签筛选） |
| GET | `/posts/{id}` | 游记详情（含评论列表分页） |
| POST | `/posts` | 发布游记 |
| PUT | `/posts/{id}` | 修改游记 |
| DELETE | `/posts/{id}` | 删除游记 |
| POST | `/posts/{id}/like` | 点赞/取消点赞 |
| GET | `/posts/{id}/comments` | 获取游记评论（分页，筛选父评论） |
| POST | `/posts/{id}/comments` | 发表评论（支持二级回复） |

### 5.3 问答社区

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/questions` | 问题列表（未回答/已回答筛选） |
| POST | `/questions` | 提问 |
| GET | `/questions/{id}` | 问题详情（含官方回答） |
| POST | `/questions/{id}/answer` | 回答（管理员或原提问者？权限自行设计） |

### 5.4 订单评价

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/reviews/orders/{orderNo}` | 查询某订单是否已评价 |
| POST | `/reviews/orders/{orderNo}` | 提交评价（rating, content, images） |
| GET | `/reviews/product/{productId}` | 查看某个产品的所有评价（分页，可按评分排序） |

### 5.5 AI 辅助功能（社区/内容）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/ai/post/generate` | **游记自动生成**：上传图片数组 + 关键词，返回 `{title, content, images}` |
| POST | `/ai/review/sentiment` | **评论情感分析**：输入文字，返回 `{sentiment: "positive/negative/neutral", score}` |
| POST | `/ai/review/reply` | 差评自动生成安抚回复（返回建议文本） |
| POST | `/ai/qa/chat` | **社区智能问答机器人**（基于 RAG） |

---

## 6. 模块五：后台管理（Admin）

*所有接口需 `ADMIN` 角色。*

### 6.1 仪表盘

| 方法 | 路径 | 说明 | 响应 data |
|------|------|------|-----------|
| GET | `/admin/dashboard/summary` | 核心指标 | `{totalUsers, totalOrders, totalRevenue, todayOrders, todayRevenue}` |
| GET | `/admin/dashboard/trends` | 趋势图（近7天交易额、订单量） | `{dates, orderCounts, revenues}` |
| GET | `/admin/dashboard/hot-products` | 热门产品 Top10（按销量） | `[{productId, name, soldCount}]` |

### 6.2 用户管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/admin/users` | 用户列表（支持按手机号/昵称/状态筛选） |
| PUT | `/admin/users/{id}/status` | 封禁/解封（`status=0/1`） |
| PUT | `/admin/users/{id}/level` | 调整会员等级 |

### 6.3 产品管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/admin/products` | 产品列表（支持按类型、状态、名称模糊查询） |
| PUT | `/admin/products/{id}/status` | 上架/下架 |
| PUT | `/admin/products/{id}/price` | 调价 |
| PUT | `/admin/products/{id}/stock` | 调库存（增量） |
| POST | `/admin/products` | 新增产品（复杂 JSON 按类型分别提交） |
| DELETE | `/admin/products/{id}` | 逻辑删除产品（下架 + 不可见） |

### 6.4 订单干预

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/admin/orders` | 订单列表（可按用户、状态、订单类型筛选） |
| PUT | `/admin/orders/{orderNo}/status` | 强制修改订单状态（需填写备注） |
| POST | `/admin/orders/{orderNo}/refund` | 强制退款（管理员发起） |

### 6.5 运营工具

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/admin/logs` | 管理员操作日志（分页） |
| POST | `/admin/questions/{id}/answer` | 回答用户问题（官方回答） |
| PUT | `/admin/comments/{id}/approve` | 审核评论（通过/驳回） |

---

## 7. 模块六：AI 通用能力（独立接口）

所有 AI 功能均通过后端代理调用大模型 API，避免前端暴露密钥。

### 7.1 智能客服

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/ai/customer-service/chat` | 对话式客服（消息历史可选，支持上下文） |
| POST | `/ai/customer-service/faq` | 常见问题直接查询（返回标准答案） |

**请求体示例**：
```json
{
  "message": "我的订单怎么退款？",
  "sessionId": "uuid"   // 可选，用于维护上下文
}
```

### 7.2 自然语言搜索

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/ai/search/flight` | 自然语言机票搜索（如“下周五去上海，周日回，不要红眼航班”） |
| POST | `/ai/search/hotel` | 自然语言酒店搜索（“近迪士尼，预算500以内，有早餐”） |
| POST | `/ai/search/travel` | 智能行程助手（根据多轮对话推荐行程，返回结构化产品列表） |

> 这些接口返回的 `data` 结构与对应的搜索接口（如 `/flights/search`）一致，方便前端复用。

### 7.3 智能解释

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/ai/explain/refund` | 解释退改签规则（输入订单号或产品ID，返回通俗解释及建议） |

### 7.4 语音输入转文字（辅助接口）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/ai/speech-to-text` | 接收音频文件（FormData），返回识别文本 |

---

## 8. 辅助接口

### 8.1 全局搜索

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/search/global` | 同时搜索产品、游记、目的地（关键词高亮） |

### 8.2 目的地相关

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/destinations/hot` | 热门目的地推荐（用于首页） |
| GET | `/destinations/{city}/weather` | 天气信息（调用第三方或模拟） |

### 8.3 文件上传

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/upload/image` | 通用图片上传（返回 URL） |

---

## 9. 附录：DTO 关键字段约定

| DTO | 说明 | 关键字段 |
|-----|------|----------|
| `UserDTO` | 用户公开信息 | id, nickname, avatar, points, level, status |
| `UserCurrentDTO` | 当前用户完整信息 | 继承 `UserDTO`，增加 `roles`, `permissions`, `email`, `phone` |
| `ProductDTO` | 产品基础信息 | id, productType, name, price, stock, soldCount, tags, status, extra (JSON) |
| `FlightDetailDTO` | 航班扩展信息 | airline, flightNo, departure, arrival, departureTime, arrivalTime, cabin, baggage |
| `OrderDTO` | 订单信息 | orderNo, userId, orderType, totalAmount, payAmount, status, createTime |
| `TravelPlanDTO` | 行程计划 | id, userId, title, destination, startDate, endDate, planData (JSON), isPublic |
| `PostDTO` | 游记 | id, userId, title, content, images, likes, commentsCount, createTime |
| `ReviewDTO` | 评价 | id, orderId, userId, rating, content, images |