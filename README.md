# 年会抽奖系统 - SaaS 版本

一个基于 Vue3 + Spring Boot + PostgreSQL + Redis 的多租户抽奖系统，支持租户独立数据隔离。

## 项目概述

将原有单页面抽奖系统重构为前后端分离的多租户 SaaS 服务，保持核心抽奖功能和用户体验不变，增强数据持久化、多租户隔离、用户权限管理能力。

## 技术架构

### 前端技术栈
- Vue 3 + Vite
- Element Plus
- Pinia (状态管理)
- Vue Router
- Axios

### 后端技术栈
- Spring Boot 3.x
- Spring Security + JWT
- MyBatis-Plus
- PostgreSQL 15+
- Redis 7.x

### 部署方案
- Docker + Docker Compose
- Nginx (前端静态资源 + 反向代理)

## 项目结构

```
qoder1/
├── lottery-backend/          # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/lottery/
│   │   │   │   ├── LotteryApplication.java
│   │   │   │   ├── common/          # 公共模块
│   │   │   │   ├── config/          # 配置类
│   │   │   │   ├── interceptor/     # 拦截器
│   │   │   │   ├── entity/          # 实体类
│   │   │   │   ├── mapper/          # MyBatis Mapper
│   │   │   │   ├── service/         # 业务逻辑
│   │   │   │   └── controller/      # 控制器
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       └── db/schema/       # 数据库脚本
│   │   └── test/
│   ├── pom.xml
│   └── Dockerfile
├── lottery-frontend/         # 前端项目
│   ├── src/
│   │   ├── main.js
│   │   ├── App.vue
│   │   ├── router/              # 路由配置
│   │   ├── stores/              # Pinia 状态管理
│   │   ├── views/               # 页面组件
│   │   ├── components/          # 公共组件
│   │   └── utils/               # 工具函数
│   ├── package.json
│   ├── vite.config.js
│   └── Dockerfile
├── docker-compose.yml
└── README.md
```

## 核心特性

### 多租户架构
- 独立 Schema 方案，每个租户拥有独立数据库架构
- 租户间数据完全隔离
- 支持子域名识别：`tenant1.lottery.com`

### 权限管理
- 三级权限：管理员(ADMIN)、操作员(OPERATOR)、查看者(VIEWER)
- 基于角色的访问控制
- JWT Token 认证

### 抽奖功能
- 保持原有纯前端滚动抽奖体验
- 后端仅负责数据持久化
- 支持多轮抽奖
- 防止重复中奖
- 实时保存中奖记录

### 数据持久化
- 所有业务数据保存到 PostgreSQL
- 支持历史数据查询
- Redis 缓存优化性能

## 快速开始

### 前置要求
- Docker & Docker Compose
- JDK 17+
- Node.js 20+
- Maven 3.9+

### 使用 Docker Compose 启动（推荐）

```bash
# 启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

访问地址：
- 前端：http://localhost
- 后端API：http://localhost:8080
- PostgreSQL：localhost:5432
- Redis：localhost:6379

### 本地开发

#### 后端启动

```bash
cd lottery-backend

# 确保 PostgreSQL 和 Redis 已启动
# 执行数据库初始化脚本
psql -U lottery_user -d lottery_db -f src/main/resources/db/schema/init_public_schema.sql

# 启动后端
mvn spring-boot:run
```

#### 前端启动

```bash
cd lottery-frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

访问 http://localhost:3000

## 数据库初始化

### 1. 创建数据库和用户

```sql
CREATE DATABASE lottery_db;
CREATE USER lottery_user WITH PASSWORD 'lottery_pass';
GRANT ALL PRIVILEGES ON DATABASE lottery_db TO lottery_user;
```

### 2. 初始化 public schema

```bash
psql -U lottery_user -d lottery_db -f lottery-backend/src/main/resources/db/schema/init_public_schema.sql
```

### 3. 租户 Schema 自动创建

租户注册时，系统会自动创建独立的 Schema 并初始化表结构。

## API 文档

后端启动后访问 Swagger UI：
http://localhost:8080/doc.html

## 配置说明

### 后端配置 (application.yml)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/lottery_db
    username: lottery_user
    password: lottery_pass
  
  redis:
    host: localhost
    port: 6379

jwt:
  secret: your-secret-key-change-this-in-production
  expiration: 7200
```

### 前端配置 (vite.config.js)

```javascript
export default defineConfig({
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

## 设计文档

详细的系统架构设计文档：[.qoder/quests/system-architecture-restructuring.md](.qoder/quests/system-architecture-restructuring.md)

包含：
- 完整的系统架构设计
- 数据库表结构设计
- API 接口设计
- 前后端交互流程
- 部署架构
- 安全设计
- 性能优化

## 开发进度

- [x] 数据库初始化脚本
- [x] Spring Boot 后端框架
- [x] 租户上下文和拦截器
- [x] Vue3 前端框架
- [x] 登录页面
- [x] Docker 部署配置
- [ ] JWT 认证实现
- [ ] 完整的后端 API
- [ ] 完整的前端页面
- [ ] 单元测试
- [ ] 集成测试

## 注意事项

1. **生产环境部署前务必修改默认密码和密钥**
2. 建议使用 HTTPS 加密传输
3. 定期备份 PostgreSQL 数据库
4. 配置合适的资源限制（CPU、内存）
5. 监控系统性能和日志

## 许可证

MIT License

## 联系方式

如有问题，请提交 Issue。
