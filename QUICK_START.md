# 年会抽奖系统 - 快速启动指南

## 问题说明

如果你遇到 `ERROR: relation "users" does not exist` 错误,这是因为:

1. **users 表在租户 schema 中**,不在 public schema 中
2. **你还没有创建任何租户**,所以没有租户 schema
3. 需要先注册一个租户,才能登录

## 解决步骤

### 步骤 1: 初始化数据库

连接到你的 PostgreSQL 数据库,执行以下 SQL 脚本:

```sql
-- 1. 执行 public schema 初始化
-- 位置: lottery-backend/src/main/resources/db/schema/init_public_schema.sql

-- 2. 验证表是否创建成功
SELECT * FROM public.tenant_registry;
SELECT * FROM public.system_config;
```

### 步骤 2: 注册第一个租户

有两种方式:

#### 方式 A: 通过 API 注册(推荐)

1. 启动后端服务:
```bash
cd lottery-backend
mvn clean package
java -jar target/lottery-backend.jar
```

2. 调用注册接口:
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "tenantCode": "demo",
    "tenantName": "演示企业",
    "adminUsername": "admin",
    "adminPassword": "admin123",
    "adminEmail": "admin@demo.com",
    "adminPhone": "13800138000",
    "adminRealName": "管理员"
  }'
```

或使用 PowerShell:
```powershell
$headers = @{
    "Content-Type" = "application/json"
}

$body = @{
    tenantCode = "demo"
    tenantName = "演示企业"
    adminUsername = "admin"
    adminPassword = "admin123"
    adminEmail = "admin@demo.com"
    adminPhone = "13800138000"
    adminRealName = "管理员"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method POST -Headers $headers -Body $body
```

3. 注册成功后,你会得到租户信息,包括 `tenantId` 和 `tenantCode`

#### 方式 B: 手动在数据库中创建(适合测试)

```sql
-- 1. 插入租户记录
INSERT INTO public.tenant_registry (
    tenant_id, tenant_code, tenant_name, schema_name, 
    admin_user_id, status, created_at, updated_at
) VALUES (
    gen_random_uuid()::text, 
    'demo', 
    '演示企业', 
    'tenant_demo', 
    gen_random_uuid()::text, 
    'ACTIVE', 
    CURRENT_TIMESTAMP, 
    CURRENT_TIMESTAMP
) RETURNING *;

-- 2. 创建租户 schema
CREATE SCHEMA tenant_demo;

-- 3. 设置 search_path
SET search_path TO tenant_demo, public;

-- 4. 在租户 schema 中执行表初始化
-- 复制 init_tenant_schema_template.sql 的内容并执行

-- 5. 创建管理员用户
INSERT INTO tenant_demo.users (
    user_id, username, password_hash, role, status, created_at, updated_at
) VALUES (
    gen_random_uuid()::text,
    'admin',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM/JUEYAwMTEL8jEP3ma', -- 密码: admin123
    'ADMIN',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- 6. 重置 search_path
SET search_path TO public;
```

### 步骤 3: 登录

注册成功后,使用以下信息登录:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "tenantCode": "demo",
    "username": "admin",
    "password": "admin123"
  }'
```

或使用 PowerShell:
```powershell
$body = @{
    tenantCode = "demo"
    username = "admin"
    password = "admin123"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method POST -Headers @{"Content-Type"="application/json"} -Body $body
```

登录成功后,你会得到 JWT Token,之后的请求都需要在请求头中携带:
```
Authorization: Bearer {token}
X-Tenant-Id: {tenantId}
```

## 验证步骤

1. 检查数据库:
```sql
-- 查看租户列表
SELECT * FROM public.tenant_registry;

-- 查看租户 schema
SELECT schema_name FROM information_schema.schemata WHERE schema_name LIKE 'tenant_%';

-- 查看租户用户(替换 tenant_demo 为你的 schema)
SET search_path TO tenant_demo, public;
SELECT * FROM users;
```

2. 检查后端日志,确认:
   - ✅ Schema 切换拦截器已启动
   - ✅ 租户拦截器已注册
   - ✅ 登录时正确设置租户上下文

## 常见问题

### Q1: 登录时还是报 "users does not exist"
**A:** 确认:
1. SchemaInterceptor 已启用 (@Component 注解未被注释)
2. 登录时传入了正确的 tenantCode
3. 数据库中存在对应的租户记录
4. 租户 schema 已创建并初始化

### Q2: 注册时报错
**A:** 检查:
1. public schema 是否已初始化
2. tenant_registry 表是否存在
3. 数据库连接是否正常
4. 后端日志中的具体错误信息

### Q3: Schema 切换不生效
**A:** 确认:
1. SchemaInterceptor.java 的 @Component 注解已启用
2. MyBatis 拦截器已正确注册
3. 重启应用后再试

## 下一步

注册并登录成功后,你可以:
1. 创建抽奖活动
2. 导入参与人员
3. 配置奖项
4. 开始抽奖

详细使用说明请参考 [README.md](./README.md)
