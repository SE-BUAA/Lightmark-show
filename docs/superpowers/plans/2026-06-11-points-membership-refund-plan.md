# 积分与会员退款联动修复 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 让酒店、机票、火车、度假订单在支付成功后统一赠送积分，退款成功后统一扣回积分，并同步刷新用户会员等级与积分流水。

**Architecture:** 保持现有订单支付/退款入口不变，在后端新增一个统一的积分会员服务，封装“计算积分、更新 `user.points`、刷新 `user.level`、写 `points_log`”四件事。各业务服务只在支付成功和退款成功的关键状态切换后调用该服务，避免不同模块再次各写一套积分规则。

**Tech Stack:** Spring Boot 3, JdbcTemplate, H2/MySQL, JUnit 5

---

### Task 1: 抽出统一积分会员服务

**Files:**
- Modify: `backend/src/main/java/top/ortus/lightmark/backend/service/MembershipService.java`
- Modify: `backend/src/main/java/top/ortus/lightmark/backend/dao/UserRepositoryImpl.java`
- Create: `backend/src/main/java/top/ortus/lightmark/backend/service/PointsMembershipService.java`

- [ ] **Step 1: 在 `MembershipService` 中补充按积分反算会员等级的方法**

```java
public short resolveLevelByPoints(int points) {
    List<lightmarkMembershipProperties.LevelRule> rules = effectiveRules();
    short resolved = 0;
    for (lightmarkMembershipProperties.LevelRule rule : rules) {
        if (points >= rule.getPointsThreshold()) {
            resolved = rule.getLevel();
        }
    }
    return resolved;
}
```

- [ ] **Step 2: 在 `UserRepositoryImpl` 中补充积分流水写入方法**

```java
public int insertPointsLog(String userId, int type, int amount, String source, String orderId) {
    String sql = """
            insert into points_log (user_id, type, amount, source, order_id, create_time)
            values (?, ?, ?, ?, ?, ?)
            """;
    return jdbcTemplate.update(sql, userId, type, amount, source, orderId, Timestamp.valueOf(LocalDateTime.now()));
}
```

- [ ] **Step 3: 新建统一积分会员服务，封装赠送与回收入口**

```java
public interface PointsMembershipService {
    int calculateRewardPoints(BigDecimal paidAmount);
    void awardPoints(String userId, String orderId, String source, BigDecimal paidAmount);
    void revokePoints(String userId, String orderId, String source, BigDecimal paidAmount);
}
```

- [ ] **Step 4: 在实现类中统一处理余额、等级、流水**

```java
int delta = calculateRewardPoints(paidAmount);
User user = userRepository.findById(userId);
user.setPoints(Math.max(0, user.getPoints() + signedDelta));
user.setLevel(membershipService.resolveLevelByPoints(user.getPoints()));
user.setUpdate_time(LocalDateTime.now());
userRepository.update(user);
userRepository.insertPointsLog(userId, type, signedDelta, source, orderId);
```

### Task 2: 接入机票支付与退款

**Files:**
- Modify: `backend/src/main/java/top/ortus/lightmark/backend/service/FlightSearchService.java`
- Test: `backend/src/test/java/top/ortus/lightmark/backend/FlightSearchApiIntegrationTests.java`

- [ ] **Step 1: 在机票支付成功逻辑中改为调用统一赠分服务**

```java
pointsMembershipService.awardPoints(
        String.valueOf(order.getUserId()),
        String.valueOf(order.getId()),
        "FLIGHT_PAY",
        order.getPayAmount()
);
```

- [ ] **Step 2: 在机票退款成功逻辑中补充统一扣分服务**

```java
pointsMembershipService.revokePoints(
        String.valueOf(order.getUserId()),
        String.valueOf(order.getId()),
        "FLIGHT_REFUND",
        order.getPayAmount()
);
```

- [ ] **Step 3: 增加机票支付/退款积分余额与流水断言**

```java
assertThat(userRepository.findById("2").getPoints()).isEqualTo(expectedPoints);
assertThat(pointsLogsFor("2"))
        .extracting("source", "amount")
        .contains(tuple("FLIGHT_PAY", 13), tuple("FLIGHT_REFUND", -13));
```

### Task 3: 接入酒店支付与退款

**Files:**
- Modify: `backend/src/main/java/top/ortus/lightmark/backend/service/impl/HotelServiceImpl.java`
- Test: `backend/src/test/java/top/ortus/lightmark/backend/controller/HotelControllerPointsIntegrationTest.java`

- [ ] **Step 1: 在酒店支付成功后调用统一赠分服务**

```java
pointsMembershipService.awardPoints(
        String.valueOf(order.getUserId()),
        String.valueOf(order.getId()),
        "HOTEL_PAY",
        order.getPayAmount()
);
```

- [ ] **Step 2: 在酒店退款/取消成功后调用统一扣分服务**

```java
pointsMembershipService.revokePoints(
        String.valueOf(order.getUserId()),
        String.valueOf(order.getId()),
        "HOTEL_REFUND",
        order.getPayAmount()
);
```

- [ ] **Step 3: 新增酒店支付退款积分回归测试**

```java
assertEquals(expectedAfterPay, userRepository.findById("2").getPoints());
assertEquals(expectedAfterRefund, userRepository.findById("2").getPoints());
```

### Task 4: 接入火车与度假支付退款

**Files:**
- Modify: `backend/src/main/java/top/ortus/lightmark/backend/service/OrderServiceImpl.java`
- Test: `backend/src/test/java/top/ortus/lightmark/backend/TrainVacationPointsIntegrationTests.java`

- [ ] **Step 1: 在火车支付与退款路径接入统一积分服务**

```java
pointsMembershipService.awardPoints(userId, orderId, "TRAIN_PAY", order.getPayAmount());
pointsMembershipService.revokePoints(userId, orderId, "TRAIN_REFUND", order.getPayAmount());
```

- [ ] **Step 2: 在度假支付与退款路径接入统一积分服务**

```java
pointsMembershipService.awardPoints(userId, orderId, "VACATION_PAY", order.getPayAmount());
pointsMembershipService.revokePoints(userId, orderId, "VACATION_REFUND", order.getPayAmount());
```

- [ ] **Step 3: 增加火车与度假积分联动测试**

```java
assertThat(pointsLogsFor("2"))
        .extracting("source")
        .contains("TRAIN_PAY", "TRAIN_REFUND", "VACATION_PAY", "VACATION_REFUND");
```

### Task 5: 验证余额展示与等级联动

**Files:**
- Verify: `backend/src/main/java/top/ortus/lightmark/backend/controller/UserController.java`
- Test: `backend/src/test/java/top/ortus/lightmark/backend/controller/UserLevelUpgradeInfoControllerTest.java`

- [ ] **Step 1: 确认用户中心读取的仍是 `user.points` 与 `user.level`**

```java
return ApiResponse.ok(new UserCurrentDTO(user, identity, Collections.emptyList()));
```

- [ ] **Step 2: 在等级接口测试中增加积分变化后的等级断言**

```java
assertThat(userRepository.findById("2").getLevel()).isEqualTo((short) 2);
```

- [ ] **Step 3: 运行回归测试与构建**

Run:

```bash
cd backend
.\mvnw -Dtest=FlightSearchApiIntegrationTests,UserLevelUpgradeInfoControllerTest,UserProfileUpdateControllerTest,HotelControllerPointsIntegrationTest,TrainVacationPointsIntegrationTests test
cd ..\frontend
npm run build
```

Expected: 相关后端测试全部通过，前端构建成功，用户中心积分余额与等级接口返回更新后的值。
