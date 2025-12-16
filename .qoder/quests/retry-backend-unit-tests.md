# 后端单元测试修复与执行设计

## 1. 任务目标

重新执行所有后端接口单元测试，识别失败原因并进行修复，直到所有单元测试通过为止。

## 2. 当前状态分析

### 2.1 测试执行现状

| 指标 | 数值 |
|------|------|
| 总测试用例数 | 48个 |
| 通过数量 | 25个 (52%) |
| 失败数量 | 23个 (48%) |
| 测试类数量 | 3个 |

### 2.2 测试覆盖范围

| 测试类 | 测试用例数 | 通过率 | 状态 |
|--------|-----------|--------|------|
| AuthControllerTest | 10 | 20% | 需修复 |
| LotteryControllerTest | 30 | 63.3% | 需修复 |
| TenantControllerTest | 8 | 50% | 需修复 |

### 2.3 核心失败原因

**主要问题**：测试断言与系统异常处理机制不匹配

- 系统采用全局异常处理器，所有异常都返回HTTP 200状态码
- 错误信息封装在Result对象的code字段中（500表示失败）
- 当前测试期望HTTP 5xx状态码，导致18个异常场景测试失败

**次要问题**：Service层Mock配置不完整

- 部分测试的Service方法未正确Mock
- 影响约5个测试用例

## 3. 修复策略

### 3.1 异常测试修复方案

**原断言逻辑**（错误）：
```
期望 HTTP 状态码 = 5xx
```

**新断言逻辑**（正确）：
```
期望 HTTP 状态码 = 200
且 Result.code = 500
且 Result.message 包含错误信息
```

### 3.2 异常抛出方式调整

**调整前**（当前方式）：
- Mock方法抛出RuntimeException
- 全局异常处理器捕获后返回通用错误

**调整后**（推荐方式）：
- Mock方法抛出BizException，并指定错误码和错误消息
- 全局异常处理器捕获后返回精确的业务错误信息

### 3.3 修复范围划分

| 修复类型 | 涉及测试用例 | 优先级 |
|---------|------------|--------|
| 异常断言修复 | 18个 | P0 - 立即处理 |
| Service Mock完善 | 5个 | P1 - 紧急处理 |
| 测试数据优化 | 若干 | P2 - 视情况处理 |

## 4. 具体修复内容

### 4.1 AuthControllerTest修复清单

#### 需要修复的测试方法（8个）

| 测试方法 | 修复类型 | 修复内容 |
|---------|---------|---------|
| should_returnUnauthorized_when_loginWithNonExistentUsername | 异常断言 | 改为检查Result.code=500 |
| should_returnUnauthorized_when_loginWithWrongPassword | 异常断言 | 改为检查Result.code=500 |
| should_returnNotFound_when_loginWithNonExistentTenant | 异常断言 | 改为检查Result.code=500 |
| should_returnConflict_when_registerWithDuplicateTenantCode | 异常断言 | 改为检查Result.code=500 |
| should_returnUnauthorized_when_refreshWithInvalidToken | 异常断言 | 改为检查Result.code=500 |
| should_returnTenantInfo_when_registerWithValidData | Service Mock | 完善tenantService.register的Mock返回 |
| should_returnSuccess_when_logoutWithValidAuth | Service Mock | 添加authService.logout的Mock配置 |
| should_returnUserInfo_when_getCurrentUserWithValidAuth | Service Mock | 可能需要调整租户上下文设置 |

#### 修复模式说明

**异常断言修复模式**：

将测试断言从：
```
期望状态码为5xx
```

改为：
```
期望状态码为200
期望Result.code为500
期望Result.message包含特定错误信息
```

**异常抛出调整**：

将Mock配置从：
```
抛出RuntimeException("错误消息")
```

改为：
```
抛出BizException(500, "错误消息")
```

### 4.2 LotteryControllerTest修复清单

#### 需要修复的测试方法（11个）

| 测试方法 | 修复类型 | 修复内容 |
|---------|---------|---------|
| should_returnNotFound_when_getDataWithNonExistentActivityId | 异常断言 | 改为检查Result.code=500 |
| should_returnConflict_when_saveWinnerForAlreadyWonParticipant | 异常断言 | 改为检查Result.code=500 |
| should_returnConflict_when_saveWinnerForExhaustedPrize | 异常断言 | 改为检查Result.code=500 |
| should_returnNotFound_when_resetLotteryWithNonExistentActivityId | 异常断言 | 改为检查Result.code=500 |
| should_returnNotFound_when_getActivityWithNonExistentId | 异常断言 | 改为检查Result.code=500 |
| should_returnNotFound_when_updateActivityWithNonExistentId | 异常断言 | 改为检查Result.code=500 |
| should_returnNotFound_when_updatePrizeWithNonExistentId | 异常断言 | 改为检查Result.code=500 |
| should_returnConflict_when_deletePrizeAlreadyDrawn | 异常断言 | 改为检查Result.code=500 |
| should_returnNotFound_when_updateParticipantWithNonExistentId | 异常断言 | 改为检查Result.code=500 |
| should_returnConflict_when_deleteParticipantAlreadyWon | 异常断言 | 改为检查Result.code=500 |
| should_returnWinnerRecord_when_saveWinnerWithValidData | Service Mock | 完善saveWinner的Mock返回 |

### 4.3 TenantControllerTest修复清单

#### 需要修复的测试方法（4个）

| 测试方法 | 修复类型 | 修复内容 |
|---------|---------|---------|
| should_returnConflict_when_createTenantWithDuplicateCode | 异常断言 | 改为检查Result.code=500 |
| should_returnError_when_createTenantWithSchemaCreationFailure | 异常断言 | 改为检查Result.code=500 |
| should_returnNotFound_when_updateTenantWithNonExistentId | 异常断言 | 改为检查Result.code=500 |
| should_returnConflict_when_updateTenantWithDuplicateCode | 异常断言 | 改为检查Result.code=500 |

## 5. 修复实施步骤

### 5.1 第一阶段：修复异常断言（预计修复18个测试）

**步骤**：
1. 定位所有使用`.andExpect(status().is5xxServerError())`的测试方法
2. 将断言修改为：
   - `.andExpect(status().isOk())`
   - `.andExpect(jsonPath("$.code").value(500))`
   - `.andExpect(jsonPath("$.message").exists())`
3. 调整Mock异常抛出方式，使用BizException替代RuntimeException

**验证标准**：
- 异常场景测试能够通过
- 错误消息能够正确传递和验证

### 5.2 第二阶段：完善Service Mock（预计修复5个测试）

**步骤**：
1. 检查失败测试的具体日志输出
2. 识别未正确Mock的Service方法
3. 添加或修正Mock配置
4. 确保Mock返回值符合Controller预期

**需要关注的Service方法**：
- `authService.logout()` - 需要配置doNothing()
- `authService.getCurrentUser()` - 需要正确返回UserVO
- `authService.register()` - 需要正确返回TenantVO
- `lotteryService.saveWinner()` - 需要正确返回WinnerVO

### 5.3 第三阶段：执行完整测试

**步骤**：
1. 运行完整测试套件
2. 记录测试结果
3. 分析剩余失败用例（如有）
4. 针对性修复

**测试命令**：
```
mvn test
```

### 5.4 第四阶段：验证与报告

**验证要点**：
- 所有48个测试用例通过
- 无跳过或错误的用例
- 测试执行时间在合理范围内

**输出内容**：
- 测试通过率报告
- 各Controller测试覆盖情况
- 测试执行时间统计

## 6. 测试断言模板

### 6.1 正常场景断言模板

```
执行HTTP请求
断言状态码为200
断言Result.code为200
断言Result.data存在且包含预期字段
验证Service方法调用次数
```

### 6.2 异常场景断言模板

```
配置Mock抛出BizException(500, "具体错误消息")
执行HTTP请求
断言状态码为200
断言Result.code为500
断言Result.message包含错误消息关键字
验证Service方法调用次数
```

### 6.3 空数据场景断言模板

```
配置Mock返回空列表或空对象
执行HTTP请求
断言状态码为200
断言Result.code为200
断言Result.data为空列表或符合预期的空状态
验证Service方法调用次数
```

## 7. 质量保证措施

### 7.1 修复前检查清单

- [ ] 已理解系统全局异常处理机制
- [ ] 已识别所有需要修复的测试方法
- [ ] 已准备好BizException类和错误码定义
- [ ] 已了解各Controller的业务逻辑

### 7.2 修复后验证清单

- [ ] 所有测试用例执行通过
- [ ] 异常场景能正确验证错误信息
- [ ] 正常场景的业务逻辑验证完整
- [ ] Mock配置准确，未执行实际Service逻辑
- [ ] 测试数据构建器功能正常

### 7.3 代码一致性检查

- [ ] 所有异常测试使用统一的断言模式
- [ ] 所有Mock配置使用BizException而非RuntimeException
- [ ] 测试方法命名遵循should_when_模式
- [ ] 测试注释清晰描述测试场景

## 8. 潜在风险与应对

### 8.1 风险1：修复后仍有失败用例

**原因**：
- Service实现与预期不符
- 测试数据配置不完整
- 租户上下文未正确设置

**应对措施**：
- 逐一分析失败日志
- 对比实际Service实现代码
- 必要时调整测试数据或Mock配置

### 8.2 风险2：测试环境问题

**原因**：
- H2数据库兼容性问题
- Redis连接失败
- 多租户Schema初始化失败

**应对措施**：
- 检查测试配置文件application-test.yml
- 确保H2数据库模式设置正确
- Mock Redis相关操作或使用嵌入式Redis

### 8.3 风险3：测试执行性能问题

**原因**：
- Spring上下文重复加载
- 数据库初始化耗时
- 测试间数据污染

**应对措施**：
- 使用@DirtiesContext注解控制上下文刷新
- 优化测试数据初始化脚本
- 确保测试间数据隔离

## 9. 预期结果

### 9.1 测试通过率目标

| 指标 | 目标值 |
|------|-------|
| 测试通过率 | 100% (48/48) |
| 测试执行时间 | ≤ 20秒 |
| 代码覆盖率 | Controller层100% |

### 9.2 交付物

- 修复后的测试类文件（3个）
- 完整的测试执行报告
- 测试通过率统计数据
- 问题修复总结文档

### 9.3 成功标准

- 执行`mvn test`命令后，所有48个测试用例通过
- 控制台输出显示0个失败，0个错误
- 测试报告显示100%通过率
- 各类异常场景能够正确验证错误信息
