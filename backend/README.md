# lightmark 后端项目

lightmark 是一个综合性的旅游服务平台后端，提供航班、酒店、火车票、度假产品等多种旅游服务的管理与预订功能。

## 技术栈

- **框架**: Spring Boot 3.5.13
- **语言**: Java 17
- **数据库**: MySQL
- **安全**: JWT Token 认证
- **构建工具**: Maven

## 项目结构

### 核心目录结构

```
src/
├── main/
│   ├── java/top/ortus/lightmark/backend/
│   │   ├── controller/           # 控制器层 - 处理HTTP请求
│   │   ├── service/              # 服务层 - 业务逻辑
│   │   ├── dao/                  # 数据访问层 - 数据库操作
│   │   ├── dto/                  # 数据传输对象 - 数据封装
│   │   ├── config/               # 配置类
│   │   ├── common/               # 通用组件
│   │   ├── exception/            # 异常处理
│   │   ├── security/             # 安全相关
│   │   └── converter/            # 数据转换器
│   └── resources/                # 配置文件和资源
└── test/                         # 测试代码
```

### 源码文件详细说明

#### Controller 层 (控制器)

**[BackendApplication.java](file:///D:/Code/projects/lightmark/backend/src/main/java/top/ortus/lightmark/backend/BackendApplication.java)** - 项目启动类
- Spring Boot 应用程序主类
- 使用 `@SpringBootApplication` 注解启动整个应用

**[HealthController.java](file:///D:/Code/projects/lightmark/backend/src/main/java/top/ortus/lightmark/backend/controller/HealthController.java)** - 健康检查控制器
- 提供 `/api/health` 端点用于健康检查
- 返回应用运行状态

**[AuthController.java](file:///D:/Code/projects/lightmark/backend/src/main/java/top/ortus/lightmark/backend/controller/AuthController.java)** - 认证控制器
- 处理用户注册、登录、登出等认证相关请求
- 提供 `/api/auth/register` 和 `/api/auth/login` 等端点

**[UserController.java](file:///D:/Code/projects/lightmark/backend/src/main/java/top/ortus/lightmark/backend/controller/UserController.java)** - 用户控制器
- 处理用户相关的CRUD操作
- 包括获取用户信息、更新用户资料等功能

**[AdminController.java](file:///D:/Code/projects/lightmark/backend/src/main/java/top/ortus/lightmark/backend/controller/AdminController.java)** - 管理员控制器
- 提供管理员专用的API端点
- 包括仪表板统计、用户管理、订单管理等功能

**[CrudController.java](file:///D:/Code/projects/lightmark/backend/src/main/java/top/ortus/lightmark/backend/controller/CrudController.java)** - 通用CRUD控制器
- 提供对任意数据库表的通用增删改查操作
- 支持动态表名和字段查询

**[ModuleController.java](file:///D:/Code/projects/lightmark/backend/src/main/java/top/ortus/lightmark/backend/controller/ModuleController.java)** - 模块控制器
- 集中管理各种业务模块的API端点
- 包括航班、酒店、火车、度假产品等业务功能

**[ModulePlaceholderController.java](file:///D:/Code/projects/lightmark/backend/src/main/java/top/ortus/lightmark/backend/controller/placeholder/ModulePlaceholderController.java)** - 模块占位符控制器
- 提供各业务模块的占位符接口
- 用于开发阶段的接口占位

#### Service 层 (服务层)

**[AuthService.java](file:///D:/Code/projects/lightmark/backend/src/main/java/top/ortus/lightmark/backend/service/AuthService.java)** - 认证服务
- 处理用户注册、登录等认证相关业务逻辑
- 包含密码加密和验证功能

**[UserService.java](file:///D:/Code/projects/lightmark/backend/src/main/java/top/ortus/lightmark/backend/service/UserService.java)** - 用户服务接口
- 定义用户服务的操作契约

**[UserServiceImpl.java](file:///D:/Code/projects/lightmark/backend/src/main/java/top/ortus/lightmark/backend/service/UserServiceImpl.java)** - 用户服务实现
- 实现用户服务的具体业务逻辑
- 包括用户信息的增删改查操作

**[AdminService.java](file:///D:/Code/projects/lightmark/backend/src/main/java/top/ortus/lightmark/backend/service/AdminService.java)** - 管理员服务
- 处理管理员相关的业务逻辑
- 包括仪表板数据统计、用户管理等功能

**[GenericCrudService.java](file:///D:/Code/projects/lightmark/backend/src/main/java/top/ortus/lightmark/backend/service/GenericCrudService.java)** - 通用CRUD服务
- 提供对任意表的通用数据库操作
- 支持动态SQL构建和执行

#### DAO 层 (数据访问层)

**[User.java](file:///D:/Code/projects/lightmark/backend/src/main/java/top/ortus/lightmark/backend/dao/User.java)** - 用户实体
- 用户数据模型类
- 对应数据库中的 user 表

**[UserRepository.java](file:///D:/Code/projects/lightmark/backend/src/main/java/top/ortus/lightmark/backend/dao/UserRepository.java)** - 用户仓库接口
- 定义用户数据访问操作的契约

**[UserRepositoryImpl.java](file:///D:/Code/projects/lightmark/backend/src/main/java/top/ortus/lightmark/backend/dao/UserRepositoryImpl.java)** - 用户仓库实现
- 实现用户数据访问的具体逻辑
- 包括JDBC操作和SQL语句执行

#### DTO 层 (数据传输对象)

**通用 DTO**
- **[UserDTO.java](file:///D:/Code/projects/lightmark/backend/src/main/java/top/ortus/lightmark/backend/dto/UserDTO.java)** - 用户数据传输对象
- **[HealthDTO.java](file:///D:/Code/projects/lightmark/backend/src/main/java/top/ortus/lightmark/backend/dto/HealthDTO.java)** - 健康检查数据传输对象

**认证 DTO**
- **[AuthTokenDTO.java](file:///D:/Code/projects/lightmark/backend/src/main/java/top/ortus/lightmark/backend/dto/auth/AuthTokenDTO.java)** - 认证令牌数据传输对象
- **[AuthRegisterRequest.java](file:///D:/Code/projects/lightmark/backend/src/main/java/top/ortus/lightmark/backend/dto/auth/AuthRegisterRequest.java)** - 认证注册请求数据传输对象
- **[AuthLoginRequest.java](file:///D:/Code/projects/lightmark/backend/src/main/java/top/ortus/lightmark/backend/dto/auth/AuthLoginRequest.java)** - 认证登录请求数据传输对象

**用户 DTO**
- **[UserCreateRequest.java](file:///D:/Code/projects/lightmark/backend/src/main/java/top/ortus/lightmark/backend/dto/user/UserCreateRequest.java)** - 用户创建请求数据传输对象
- **[UserUpdateRequest.java](file:///D:/Code/projects/lightmark/backend/src/main/java/top/ortus/lightmark/backend/dto/user/UserUpdateRequest.java)** - 用户更新请求数据传输对象

**模块 DTO** - 业务模块数据传输对象
- **[ProductDTO.java](file:///D:/Code/projects/lightmark/backend/src/main/java/top/ortus/lightmark/backend/dto/module/ProductDTO.java)** - 产品数据传输对象
- **[TravelerDTO.java](file:///D:/Code/projects/lightmark/backend/src/main/java/top/ortus/lightmark/backend/dto/module/TravelerDTO.java)** - 旅客数据传输对象
- **[PointsLogDTO.java](file:///D:/Code/projects/lightmark/backend/src/main/java/top/ortus/lightmark/backend/dto/module/PointsLogDTO.java)** - 积分日志数据传输对象
- **[OrderDTO.java](file:///D:/Code/projects/lightmark/backend/src/main/java/top/ortus/lightmark/backend/dto/module/OrderDTO.java)** - 订单数据传输对象
- **[PaymentRecordDTO.java](file:///D:/Code/projects/lightmark/backend/src/main/java/top/ortus/lightmark/backend/dto/module/PaymentRecordDTO.java)** - 支付记录数据传输对象
- **[FlightOrderDetailDTO.java](file:///D:/Code/projects/lightmark/backend/src/main/java/top/ortus/lightmark/backend/dto/module/FlightOrderDetailDTO.java)** - 航班订单详情数据传输对象
- **[ReviewDTO.java](file:///D:/Code/projects/lightmark/backend/src/main/java/top/ortus/lightmark/backend/dto/module/ReviewDTO.java)** - 评价数据传输对象
- **[CommentDTO.java](file:///D:/Code/projects/lightmark/backend/src/main/java/top/ortus/lightmark/backend/dto/module/CommentDTO.java)** - 评论数据传输对象
- **[PostDTO.java](file:///D:/Code/projects/lightmark/backend/src/main/java/top/ortus/lightmark/backend/dto/module/PostDTO.java)** - 帖子数据传输对象
- **[PostLikeDTO.java](file:///D:/Code/projects/lightmark/backend/src/main/java/top/ortus/lightmark/backend/dto/module/PostLikeDTO.java)** - 帖子点赞数据传输对象
- **[QuestionDTO.java](file:///D:/Code/projects/lightmark/backend/src/main/java/top/ortus/lightmark/backend/dto/module/QuestionDTO.java)** - 问答数据传输对象
- **[TravelPlanDTO.java](file:///D:/Code/projects/lightmark/backend/src/main/java/top/ortus/lightmark/backend/dto/module/TravelPlanDTO.java)** - 旅行计划数据传输对象
- **[ProductViewLogDTO.java](file:///D:/Code/projects/lightmark/backend/src/main/java/top/ortus/lightmark/backend/dto/module/ProductViewLogDTO.java)** - 产品浏览日志数据传输对象
- **[RoomTypeDTO.java](file:///D:/Code/projects/lightmark/backend/src/main/java/top/ortus/lightmark/backend/dto/module/RoomTypeDTO.java)** - 房型数据传输对象
- **[AdminLogDTO.java](file:///D:/Code/projects/lightmark/backend/src/main/java/top/ortus/lightmark/backend/dto/module/AdminLogDTO.java)** - 管理员日志数据传输对象
- **[UserLoginLogDTO.java](file:///D:/Code/projects/lightmark/backend/src/main/java/top/ortus/lightmark/backend/dto/module/UserLoginLogDTO.java)** - 用户登录日志数据传输对象
- **[UserRoleDTO.java](file:///D:/Code/projects/lightmark/backend/src/main/java/top/ortus/lightmark/backend/dto/module/UserRoleDTO.java)** - 用户角色数据传输对象
- **[RoleDTO.java](file:///D:/Code/projects/lightmark/backend/src/main/java/top/ortus/lightmark/backend/dto/module/RoleDTO.java)** - 角色数据传输对象

**管理员 DTO**
- **[AdminOrderDTO.java](file:///D:/Code/projects/lightmark/backend/src/main/java/top/ortus/lightmark/backend/dto/admin/AdminOrderDTO.java)** - 管理员订单数据传输对象
- **[AdminProductDTO.java](file:///D:/Code/projects/lightmark/backend/src/main/java/top/ortus/lightmark/backend/dto/admin/AdminProductDTO.java)** - 管理员产品数据传输对象
- **[DashboardSummaryDTO.java](file:///D:/Code/projects/lightmark/backend/src/main/java/top/ortus/lightmark/backend/dto/admin/DashboardSummaryDTO.java)** - 仪表板摘要数据传输对象

**占位符 DTO**
- **[PlaceholderDTO.java](file:///D:/Code/projects/lightmark/backend/src/main/java/top/ortus/lightmark/backend/dto/placeholder/PlaceholderDTO.java)** - 占位符数据传输对象

#### Config 层 (配置)

**[WebConfig.java](file:///D:/Code/projects/lightmark/backend/src/main/java/top/ortus/lightmark/backend/config/WebConfig.java)** - Web配置
- 配置拦截器、跨域设置等
- 注册管理员身份验证拦截器

**[lightmarkDatabaseProperties.java](file:///D:/Code/projects/lightmark/backend/src/main/java/top/ortus/lightmark/backend/config/lightmarkDatabaseProperties.java)** - 数据库配置属性
- 定义支持的数据库表配置
- 从 application.yaml 中读取表定义

#### Common 层 (通用组件)

**[ApiResponse.java](file:///D:/Code/projects/lightmark/backend/src/main/java/top/ortus/lightmark/backend/common/ApiResponse.java)** - 统一响应格式
- 定义API响应的标准格式
- 包含状态码、消息和数据

**[PageResponse.java](file:///D:/Code/projects/lightmark/backend/src/main/java/top/ortus/lightmark/backend/common/PageResponse.java)** - 分页响应格式
- 定义分页查询结果的标准格式
- 包含总记录数、当前页数据等

#### Exception 层 (异常处理)

**[ApiException.java](file:///D:/Code/projects/lightmark/backend/src/main/java/top/ortus/lightmark/backend/exception/ApiException.java)** - API异常类
- 自定义业务异常类
- 包含错误码和错误消息

**[GlobalExceptionHandler.java](file:///D:/Code/projects/lightmark/backend/src/main/java/top/ortus/lightmark/backend/exception/GlobalExceptionHandler.java)** - 全局异常处理器
- 统一处理系统中的异常
- 将异常转换为标准的API响应格式

#### Security 层 (安全)

**[AdminAuthInterceptor.java](file:///D:/Code/projects/lightmark/backend/src/main/java/top/ortus/lightmark/backend/security/AdminAuthInterceptor.java)** - 管理员身份验证拦截器
- 拦截需要管理员权限的请求
- 验证JWT Token的有效性

#### Converter 层 (转换器)

**[UserConverter.java](file:///D:/Code/projects/lightmark/backend/src/main/java/top/ortus/lightmark/backend/converter/UserConverter.java)** - 用户转换器
- 负责 User 实体和 UserDTO 之间的转换
- 简化数据层和业务层之间的数据转换

#### 其他重要组件

**[JwtTokenService.java](file:///D:/Code/projects/lightmark/backend/src/main/java/top/ortus/lightmark/backend/JwtTokenService.java)** - JWT令牌服务
- 负责JWT令牌的创建和验证
- 实现用户身份认证机制

## 配置说明

### application.yaml 配置项

```yaml
spring:
  application:
    name: backend
  datasource:
    # 数据库连接配置
    url: jdbc:mysql://${DB_HOST:24.199.77.196}:${DB_PORT:3306}/${DB_NAME:lightmark}?allowPublicKeyRetrieval=true&useSSL=false
    username: ${DB_USER:se}
    password: ${DB_PASSWORD:sebuaa}
  sql:
    init:
      mode: never
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    baseline-version: 20260611

server:
  # 服务器端口配置
  port: ${SERVER_PORT:8080}

lightmark:
  jwt:
    # JWT令牌配置
    secret: ${JWT_SECRET:lightmark-secret-key-please-change}  # JWT密钥
    issuer: ${JWT_ISSUER:lightmark}                           # JWT发行者
    expire-minutes: ${JWT_EXPIRE_MINUTES:120}               # 令牌过期时间(分钟)
  database:
    # 数据库表定义
    tables:
      - user
      - role
      - user_role
      - traveler
      - points_log
      - user_login_log
      - product
      - room_type
      - product_view_log
      - orders
      - payment_record
      - flight_order_detail
      - travel_plan
      - post
      - post_like
      - comment
      - review
      - question
      - admin_log
```

### 环境变量配置

| 变量名 | 默认值 | 描述 |
|--------|--------|------|
| DB_HOST | 24.199.77.196 | 数据库主机地址 |
| DB_PORT | 3306 | 数据库端口 |
| DB_NAME | lightmark | 数据库名称 |
| DB_USER | se | 数据库用户名 |
| DB_PASSWORD | sebuaa | 数据库密码 |
| SERVER_PORT | 8080 | 服务器端口 |
| JWT_SECRET | lightmark-secret-key-please-change | JWT密钥（生产环境务必更改） |
| JWT_ISSUER | lightmark | JWT发行者 |
| JWT_EXPIRE_MINUTES | 120 | JWT过期时间（分钟） |

## 项目启动

### 开发环境启动

1. **克隆项目**
```bash
git clone <repository-url>
cd backend
```

2. **配置数据库**
   - 确保MySQL数据库服务正在运行
   - 修改 `application.yaml` 中的数据库连接信息

3. **启动项目**
```bash
mvn spring-boot:run
```

4. **使用IDE启动**
   - 直接运行 `BackendApplication.java` 的 main 方法

### 打包部署

```bash
# 打包
mvn clean package

# 运行打包后的jar
java -jar target/backend-0.0.1-SNAPSHOT.jar
```
