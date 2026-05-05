# timemark backend

## 文档结构
- 使用方法
- API 规范
- 目录与文件说明

## 使用方法

- 运行测试：

```powershell
.\mvnw test
```

- 运行应用：

```powershell
.\mvnw spring-boot:run
```

## API 规范

- 统一响应结构：
  - `code`: 状态码（0 表示成功）
  - `errorMsg`: 错误信息（成功时为空字符串）
  - `data`: 业务数据（DTO 或分页对象）
- 分页结构：
  - `total`: 总数
  - `items`: 数据列表

## 目录与文件说明

- `src/main/java/top/ortus/timemark/backend/controller`：控制器，提供 HTTP API。
- `src/main/java/top/ortus/timemark/backend/service`：业务逻辑与通用 CRUD 服务。
- `src/main/java/top/ortus/timemark/backend/dao`：数据访问层，使用 `JdbcTemplate`。
- `src/main/java/top/ortus/timemark/backend/dto`：数据传输对象（响应 DTO）。
- `src/main/java/top/ortus/timemark/backend/dto/module`：各业务表对应的模块 DTO。
- `src/main/java/top/ortus/timemark/backend/common`：通用响应与分页模型。
- `src/main/java/top/ortus/timemark/backend/exception`：全局异常与统一错误返回。
- `src/main/java/top/ortus/timemark/backend/config`：配置与拦截器注册。
- `src/main/java/top/ortus/timemark/backend/security`：权限拦截与鉴权辅助。
- `src/main/resources/application.yaml`：基础配置与数据库表清单。
- `CHANGELOG.md`：变更记录与 API 说明。
