# 变更记录

## 本次更新
- 用户模块完成增删改查，所有返回值为 `UserDTO`。
- 新增用户请求 DTO：`UserCreateRequest`、`UserUpdateRequest`。
- 增加数据库表配置 `timemark.database.tables`，覆盖全部业务表。
- 新增配置类 `TimemarkDatabaseProperties` 读取表配置。
- 模块接口升级为强类型 DTO，替换 Map DTO。
- 增加管理员权限拦截器与登录态校验。

## 接口返回规范
- 统一返回 `ApiResponse<T>`：包含 `code`、`errorMsg`、`data`。
- 列表统一返回 `PageResponse<T>`：包含 `total`、`items`。

## 模块一（M1）已补全
- 认证接口：注册/登录/登出。
- 管理员仪表盘：总用户数、总订单数、总成交额统计。
- 后台产品管理：产品列表可按类型筛选。
- 后台订单总览：按状态筛选订单，支持更新订单状态并记录日志。
- 后台用户管理：按关键字筛选，支持更新用户状态并记录日志。
- 健康检查接口。

## 其余模块基础接口
- 基于通用 CRUD 能力提供表级增删改查，路径为 `/api/crud/{table}`。
- 模块接口已提供基本列表/新增/更新/删除，返回对应 DTO。

## 技术实现说明
- 使用 `JdbcTemplate`/`NamedParameterJdbcTemplate` 进行 SQL 访问。
- `GenericCrudService` 通过元数据获取主键/列信息，兼容不同数据库结构。
- `JwtTokenService` 使用 JJWT 生成/解析令牌。
- `GlobalExceptionHandler` 统一捕获异常并返回标准响应。
- `AdminAuthInterceptor` 校验 `/api/admin/**` 的登录态与管理员角色。

## API 规范（新增/更新）
- GET `/api/health`：健康检查。
- POST `/api/auth/register`：注册，Body `AuthRegisterRequest`，返回 `UserDTO`。
- POST `/api/auth/login`：登录，Body `AuthLoginRequest`，返回 `AuthTokenDTO`。
- POST `/api/auth/logout`：登出。
- GET `/api/admin/dashboard/summary`：仪表盘统计。
- GET `/api/admin/products`：产品列表，支持 `productType`。
- GET `/api/admin/users`：用户列表，支持 `keyword`。
- PATCH `/api/admin/users/{id}/status`：更新用户状态，参数 `status`。
- GET `/api/admin/orders`：订单列表，支持 `status`。
- PATCH `/api/admin/orders/{id}/status`：更新订单状态，参数 `status`、`cancelReason`。
- GET `/api/crud/{table}`：通用列表（支持查询参数过滤）。
- GET `/api/crud/{table}/{id}`：通用单条查询（单主键表）。
- POST `/api/crud/{table}`：通用新增。
- PUT `/api/crud/{table}`：通用更新（需带主键字段）。
- DELETE `/api/crud/{table}`：通用删除（需带主键字段）。
- GET `/api/flights|/hotels|/trains|/vacations`：按产品类型列表。
- GET/POST/PUT/DELETE `/api/travelers`、`/api/points-logs`、`/api/user-login-logs`、`/api/room-types`、`/api/product-view-logs`、`/api/orders`、`/api/payment-records`、`/api/flight-order-details`、`/api/travel-plans`、`/api/posts`、`/api/post-likes`、`/api/comments`、`/api/reviews`、`/api/questions`、`/api/roles`、`/api/user-roles`、`/api/admin-logs`（均返回对应 DTO）。

## 变更文件
- src/main/java/top/ortus/timemark/backend/controller/UserController.java
- src/main/java/top/ortus/timemark/backend/service/UserService.java
- src/main/java/top/ortus/timemark/backend/service/UserServiceImpl.java
- src/main/java/top/ortus/timemark/backend/dao/UserRepository.java
- src/main/java/top/ortus/timemark/backend/dao/UserRepositoryImpl.java
- src/main/java/top/ortus/timemark/backend/dto/user/UserCreateRequest.java
- src/main/java/top/ortus/timemark/backend/dto/user/UserUpdateRequest.java
- src/main/java/top/ortus/timemark/backend/config/TimemarkDatabaseProperties.java
- src/main/resources/application.yaml
- src/main/java/top/ortus/timemark/backend/common/ApiResponse.java
- src/main/java/top/ortus/timemark/backend/common/PageResponse.java
- src/main/java/top/ortus/timemark/backend/exception/ApiException.java
- src/main/java/top/ortus/timemark/backend/exception/GlobalExceptionHandler.java
- src/main/java/top/ortus/timemark/backend/JwtTokenService.java
- src/main/java/top/ortus/timemark/backend/controller/AuthController.java
- src/main/java/top/ortus/timemark/backend/controller/AdminController.java
- src/main/java/top/ortus/timemark/backend/controller/HealthController.java
- src/main/java/top/ortus/timemark/backend/controller/CrudController.java
- src/main/java/top/ortus/timemark/backend/controller/ModuleController.java
- src/main/java/top/ortus/timemark/backend/service/AuthService.java
- src/main/java/top/ortus/timemark/backend/service/AdminService.java
- src/main/java/top/ortus/timemark/backend/service/GenericCrudService.java
- src/main/java/top/ortus/timemark/backend/dto/HealthDTO.java
- src/main/java/top/ortus/timemark/backend/dto/auth/AuthLoginRequest.java
- src/main/java/top/ortus/timemark/backend/dto/auth/AuthRegisterRequest.java
- src/main/java/top/ortus/timemark/backend/dto/auth/AuthTokenDTO.java
- src/main/java/top/ortus/timemark/backend/dto/admin/DashboardSummaryDTO.java
- src/main/java/top/ortus/timemark/backend/dto/admin/AdminProductDTO.java
- src/main/java/top/ortus/timemark/backend/dto/admin/AdminOrderDTO.java
- src/main/java/top/ortus/timemark/backend/controller/placeholder/ModulePlaceholderController.java
- src/main/java/top/ortus/timemark/backend/config/WebConfig.java
- src/main/java/top/ortus/timemark/backend/security/AdminAuthInterceptor.java
- src/main/java/top/ortus/timemark/backend/dto/module/RoleDTO.java
- src/main/java/top/ortus/timemark/backend/dto/module/UserRoleDTO.java
- src/main/java/top/ortus/timemark/backend/dto/module/TravelerDTO.java
- src/main/java/top/ortus/timemark/backend/dto/module/PointsLogDTO.java
- src/main/java/top/ortus/timemark/backend/dto/module/UserLoginLogDTO.java
- src/main/java/top/ortus/timemark/backend/dto/module/ProductDTO.java
- src/main/java/top/ortus/timemark/backend/dto/module/RoomTypeDTO.java
- src/main/java/top/ortus/timemark/backend/dto/module/ProductViewLogDTO.java
- src/main/java/top/ortus/timemark/backend/dto/module/OrderDTO.java
- src/main/java/top/ortus/timemark/backend/dto/module/PaymentRecordDTO.java
- src/main/java/top/ortus/timemark/backend/dto/module/FlightOrderDetailDTO.java
- src/main/java/top/ortus/timemark/backend/dto/module/TravelPlanDTO.java
- src/main/java/top/ortus/timemark/backend/dto/module/PostDTO.java
- src/main/java/top/ortus/timemark/backend/dto/module/PostLikeDTO.java
- src/main/java/top/ortus/timemark/backend/dto/module/CommentDTO.java
- src/main/java/top/ortus/timemark/backend/dto/module/ReviewDTO.java
- src/main/java/top/ortus/timemark/backend/dto/module/QuestionDTO.java
- src/main/java/top/ortus/timemark/backend/dto/module/AdminLogDTO.java
