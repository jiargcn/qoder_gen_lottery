# 年会抽奖系统 SaaS 架构重构 - 任务清单检查

## 任务检查说明

本文档基于 `system-architecture-restructuring.md` 设计文档,对当前代码实现进行检查,列出已完成和未完成的任务。

---

## 一、数据库层面

### 1.1 数据库脚本

#### ✅ 已完成
- public schema 初始化脚本 (init_public_schema.sql)
- 租户 schema 模板脚本 (init_tenant_schema_template.sql)
- 系统配置初始数据脚本 (init_system_config.sql)

#### ❌ 未完成
- 缺少 MyBatis Schema 切换拦截器实现
- 缺少租户 Schema 动态创建服务
- 缺少数据库连接池隔离配置

---

## 二、后端实现

### 2.1 实体类 (Entity/PO)

#### ✅ 已完成
- LotteryActivity (抽奖活动表)
- Prize (奖项表)
- Participant (参与人员表)
- WinnerRecord (中奖记录表)

#### ❌ 未完成
- Tenant (租户实体类) - public.tenant_registry 对应
- User (用户实体类) - 租户 schema 的 users 表对应
- OperationLog (操作日志实体类)
- SystemConfig (系统配置实体类)

### 2.2 数据传输对象 (DTO)

#### ❌ 未完成
- LoginDTO (登录请求)
- RegisterDTO (注册请求)
- ActivityCreateDTO (活动创建请求)
- ParticipantImportDTO (人员批量导入请求)
- WinnerSaveDTO (中奖记录保存请求)
- PrizeCreateDTO (奖项创建请求)

### 2.3 视图对象 (VO)

#### ❌ 未完成
- UserVO (用户视图)
- ActivityVO (活动视图)
- LotteryDataVO (抽奖初始化数据视图)
- WinnerVO (中奖记录视图)
- TenantVO (租户信息视图)

### 2.4 Mapper 接口

#### ✅ 已完成
- WinnerRecordMapper

#### ❌ 未完成
- TenantMapper (租户数据访问)
- UserMapper (用户数据访问)
- ActivityMapper (活动数据访问)
- PrizeMapper (奖项数据访问)
- ParticipantMapper (参与人员数据访问)
- OperationLogMapper (操作日志数据访问)

### 2.5 Service 层

#### ✅ 已完成
- ILotteryService 接口
- LotteryServiceImpl 实现

#### ❌ 未完成
- ITenantService (租户管理服务) 及实现
- IAuthService (认证授权服务) 及实现
- IUserService (用户管理服务) 及实现
- IActivityService (活动管理服务) 及实现
- IQueryService (历史数据查询服务) 及实现
- IPrizeService (奖项管理服务) 及实现
- IParticipantService (人员管理服务) 及实现

### 2.6 Controller 层

#### ✅ 已完成
- LotteryController (基础框架)

#### ❌ 未完成 (需要完善和新增)
- LotteryController 缺少完整的 API 实现
- AuthController (认证接口)
- TenantController (租户管理接口)
- UserController (用户管理接口)
- ActivityController (活动管理接口)
- QueryController (历史查询接口)

### 2.7 配置类

#### ✅ 已完成
- WebMvcConfig (基础配置,已注册 TenantInterceptor)

#### ❌ 未完成
- SecurityConfig (Spring Security 配置)
- DataSourceConfig (数据源配置)
- RedisConfig (Redis 配置)
- MyBatisConfig (MyBatis 配置)
- Knife4j 配置 (API 文档配置)

### 2.8 拦截器与切面

#### ✅ 已完成
- TenantInterceptor (租户识别拦截器 - 仅实现 Header 识别)

#### ❌ 未完成
- TenantInterceptor 缺少子域名识别逻辑
- TenantInterceptor 缺少 JWT Token 提取租户 ID 逻辑
- AuthInterceptor (认证拦截器)
- LogAspect (操作日志 AOP 切面)
- PermissionAspect (权限检查 AOP 切面)
- SchemaInterceptor (MyBatis Schema 动态切换拦截器)

### 2.9 工具类

#### ❌ 未完成
- JwtUtil (JWT Token 生成与验证)
- BcryptUtil (密码加密工具)
- TenantUtil (租户工具类)
- RedisUtil (Redis 操作工具类)

### 2.10 过滤器

#### ❌ 未完成
- CorsFilter (跨域过滤器)

---

## 三、前端实现

### 3.1 页面组件 (Views)

#### ✅ 已完成
- Login.vue (登录页面)
- LotteryDraw.vue (抽奖页面)

#### ❌ 未完成
- Register.vue (注册页面)
- Dashboard.vue (仪表盘)
- ActivityList.vue (活动列表)
- ActivityCreate.vue (创建活动)
- HistoryQuery.vue (历史查询)
- UserManage.vue (用户管理,仅 ADMIN)

### 3.2 公共组件 (Components)

#### ❌ 未完成
- layout/Header.vue (头部导航)
- layout/Sidebar.vue (侧边栏)
- layout/Footer.vue (底部)
- lottery/PrizeDisplay.vue (奖项展示)
- lottery/RollingDisplay.vue (滚动显示区)
- lottery/ControlPanel.vue (控制面板)
- lottery/WinnerList.vue (中奖名单)
- lottery/ParticipantGrid.vue (参与人员网格)
- lottery/Fireworks.vue (烟花动画)
- common/FileUpload.vue (文件上传)
- common/DataTable.vue (数据表格)

### 3.3 状态管理 (Stores)

#### ✅ 已完成
- lottery.js (抽奖状态管理 - 基础框架)

#### ❌ 未完成
- user.js (用户状态)
- tenant.js (租户状态)
- activity.js (活动状态)
- lottery.js 缺少完整的状态管理逻辑和方法

### 3.4 API 请求封装

#### ✅ 已完成
- request.js (Axios 封装 - 基础框架)

#### ❌ 未完成
- api/auth.js (认证 API)
- api/activity.js (活动管理 API)
- api/lottery.js (抽奖 API)
- api/query.js (查询 API)
- api/user.js (用户管理 API)
- api/tenant.js (租户管理 API)
- request.js 缺少租户识别逻辑 (子域名提取、Header 添加)

### 3.5 路由配置

#### ✅ 已完成
- router/index.js (基础路由框架)

#### ❌ 未完成
- 缺少完整的路由定义 (注册、仪表盘、活动管理等页面路由)
- 缺少路由守卫 (权限验证、登录验证)

### 3.6 工具函数

#### ❌ 未完成
- utils/auth.js (认证工具 - Token 存储、用户信息管理)
- utils/tenant.js (租户识别工具)
- utils/file-parser.js (Excel/TXT 文件解析)

### 3.7 样式资源

#### ❌ 未完成
- assets/styles/variables.scss (全局变量)
- assets/styles/lottery.scss (抽奖页面样式 - 复用原有 lottery.html 样式)

---

## 四、核心功能模块

### 4.1 多租户架构

#### ✅ 已完成
- 租户上下文管理 (TenantContext)
- 租户拦截器 (TenantInterceptor - 仅 Header 识别)
- Public Schema 表结构

#### ❌ 未完成
- 租户注册流程实现
- 租户 Schema 动态创建逻辑
- MyBatis Schema 动态切换拦截器
- 子域名租户识别
- JWT Token 中携带租户 ID

### 4.2 认证授权

#### ❌ 未完成
- Spring Security 配置
- JWT Token 生成与验证
- 登录接口实现
- 注册接口实现
- 密码加密存储 (BCrypt)
- 角色权限验证
- Redis 会话管理

### 4.3 抽奖核心功能

#### ✅ 部分完成
- 基础 Service 和 Controller 框架

#### ❌ 未完成
- 获取抽奖初始化数据接口
- 保存中奖记录接口
- 重置抽奖接口
- 前端滚动抽奖逻辑
- 中奖记录实时更新
- 防并发抽奖 (Redis 分布式锁)

### 4.4 活动管理

#### ❌ 未完成
- 创建活动接口
- 查询活动列表接口
- 活动详情接口
- 更新活动接口
- 删除活动接口
- 活动统计接口

### 4.5 奖项管理

#### ❌ 未完成
- 添加奖项接口
- 查询奖项列表接口
- 更新奖项接口
- 删除奖项接口

### 4.6 人员管理

#### ❌ 未完成
- 单个添加参与人员接口
- 批量导入参与人员接口
- 前端 Excel/TXT 文件解析
- 参与人员列表查询接口
- 更新参与人员接口
- 删除参与人员接口

### 4.7 用户管理

#### ❌ 未完成
- 用户列表查询接口 (ADMIN)
- 创建用户接口 (ADMIN)
- 更新用户接口 (ADMIN)
- 删除用户接口 (ADMIN)
- 启用/禁用用户接口 (ADMIN)

### 4.8 历史数据查询

#### ❌ 未完成
- 历史活动查询接口 (分页+筛选)
- 历史中奖记录查询接口 (分页+筛选)
- 数据导出接口 (Excel)
- 操作日志查询接口 (ADMIN)

---

## 五、缓存与性能优化

### 5.1 Redis 缓存

#### ❌ 未完成
- Redis 配置类
- 会话缓存实现 (session:{token})
- 租户信息缓存 (tenant:{tenantId})
- 活动信息缓存 (activity:{activityId})
- 参与人员缓存 (participants:{activityId})
- 中奖记录缓存 (winners:{activityId})
- 用户信息缓存 (user:{userId})
- 分布式锁实现 (lottery:lock:{activityId})

### 5.2 缓存策略

#### ❌ 未完成
- Cache-Aside 读取策略
- 缓存更新策略 (删除缓存)
- 缓存预热逻辑
- 缓存穿透防护 (布隆过滤器)
- 缓存雪崩防护 (TTL 随机化)

---

## 六、安全与权限

### 6.1 数据安全

#### ❌ 未完成
- 密码 BCrypt 加密
- 敏感数据脱敏 (手机号、邮箱)
- SQL 注入防护验证
- 输入验证与过滤

### 6.2 接口安全

#### ❌ 未完成
- HTTPS 配置
- CORS 跨域配置
- 限流策略 (Redis + Lua)
- 防重放攻击机制

### 6.3 租户隔离安全

#### ✅ 部分完成
- 基础租户上下文隔离

#### ❌ 未完成
- 租户 ID 验证
- 跨租户访问禁止验证
- 操作审计日志记录

---

## 七、部署与运维

### 7.1 容器化部署

#### ❌ 未完成
- Docker Compose 配置文件
- 后端 Dockerfile
- 前端 Dockerfile
- Nginx 配置文件

### 7.2 环境配置

#### ✅ 已完成
- application.yml 基础配置

#### ❌ 未完成
- application-dev.yml (开发环境)
- application-prod.yml (生产环境)
- 环境变量配置

### 7.3 监控与日志

#### ❌ 未完成
- 日志配置 (Logback)
- 操作日志记录切面
- 监控指标配置
- 健康检查接口

---

## 八、文档与测试

### 8.1 API 文档

#### ❌ 未完成
- Knife4j 配置
- 接口注解完善 (Swagger)

### 8.2 测试

#### ❌ 未完成
- 单元测试
- 集成测试
- 性能测试

---

## 任务优先级建议

### P0 (核心必需,阻塞功能)

1. **多租户基础设施**
   - MyBatis Schema 切换拦截器
   - 租户 Schema 动态创建服务
   - 租户注册流程

2. **认证授权**
   - Spring Security 配置
   - JWT 工具类
   - 登录/注册接口
   - 密码加密

3. **核心实体与数据访问**
   - 缺失的实体类 (Tenant、User、OperationLog)
   - 缺失的 Mapper 接口
   - DTO 和 VO 类

4. **抽奖核心功能**
   - 抽奖初始化数据接口
   - 保存中奖记录接口
   - 前端抽奖逻辑完善

### P1 (重要功能)

5. **活动与人员管理**
   - 活动 CRUD 接口
   - 奖项 CRUD 接口
   - 人员导入功能

6. **前端页面完善**
   - 活动列表页
   - 活动创建页
   - 仪表盘
   - 注册页

7. **Redis 缓存**
   - Redis 配置
   - 会话缓存
   - 业务数据缓存
   - 分布式锁

### P2 (增强功能)

8. **历史查询**
   - 历史活动查询
   - 中奖记录查询
   - 数据导出

9. **用户管理**
   - 用户 CRUD (ADMIN)
   - 权限验证

10. **安全增强**
    - CORS 配置
    - 限流策略
    - 审计日志

### P3 (运维支持)

11. **部署配置**
    - Docker Compose
    - Nginx 配置
    - 环境配置文件

12. **监控与日志**
    - 日志配置
    - 监控指标

13. **文档与测试**
    - API 文档
    - 单元测试

---

## 估算工作量

- **P0 任务**: 约 10-15 个工作日
- **P1 任务**: 约 8-10 个工作日
- **P2 任务**: 约 5-7 个工作日
- **P3 任务**: 约 3-5 个工作日

**总计**: 约 26-37 个工作日 (1-2 个月)

---

## 下一步行动建议

1. 优先完成 **P0 任务**,建立系统核心骨架
2. 按模块划分任务,逐步实现 **P1 任务**
3. 在核心功能稳定后,补充 **P2 和 P3 任务**
4. 持续进行集成测试和代码优化
