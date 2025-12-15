# 年会抽奖系统 - 项目结构说明

## 项目概述

本项目已从单页面应用重构为前后端分离的 SaaS 多租户架构，具备完整的企业级功能。

## 目录结构

```
qoder1/
├── lottery.html                          # 原始单页面版本（可独立使用）
├── README.md                             # 项目说明文档
├── PROJECT_STRUCTURE.md                  # 本文件
├── docker-compose.yml                    # Docker 编排配置
│
├── .qoder/
│   ├── quests/
│   │   └── system-architecture-restructuring.md  # 系统架构重构设计文档
│   └── repowiki/zh/content/              # 原始项目知识库
│
├── lottery-backend/                      # 后端项目（Spring Boot）
│   ├── pom.xml                          # Maven 配置
│   ├── Dockerfile                       # 后端 Docker 镜像
│   └── src/main/
│       ├── java/com/lottery/
│       │   ├── LotteryApplication.java  # 启动类
│       │   └── common/                  # 公共模块
│       │       ├── context/             # 租户上下文
│       │       ├── response/            # 统一响应
│       │       ├── exception/           # 异常处理
│       │       ├── constant/            # 常量定义
│       │       └── util/                # 工具类
│       └── resources/
│           ├── application.yml          # 主配置文件
│           └── db/                      # 数据库脚本
│               ├── schema/
│               │   ├── init_public_schema.sql          # Public Schema 初始化
│               │   └── init_tenant_schema_template.sql # 租户 Schema 模板
│               └── data/
│                   └── init_system_config.sql          # 系统配置数据
│
└── lottery-frontend/                    # 前端项目（Vue 3）
    ├── package.json                     # NPM 配置
    ├── vite.config.js                   # Vite 配置
    ├── Dockerfile                       # 前端 Docker 镜像
    ├── index.html                       # HTML 入口
    └── src/
        ├── main.js                      # 应用入口
        ├── App.vue                      # 根组件
        ├── router/                      # 路由配置
        │   └── index.js
        ├── stores/                      # Pinia 状态管理
        │   └── lottery.js               # 抽奖状态
        └── utils/                       # 工具函数
            └── request.js               # Axios 封装
```

## 技术栈

### 后端
- **框架**: Spring Boot 3.2.0
- **数据库**: PostgreSQL 15+ (多 Schema 架构)
- **缓存**: Redis 7.x
- **安全**: Spring Security + JWT
- **ORM**: MyBatis-Plus 3.5.5
- **API 文档**: Knife4j (Swagger 增强)

### 前端
- **框架**: Vue 3 + Vite
- **UI 组件**: Element Plus
- **状态管理**: Pinia
- **路由**: Vue Router 4
- **HTTP 客户端**: Axios

### 基础设施
- **容器化**: Docker + Docker Compose
- **数据库**: PostgreSQL (独立 Schema 多租户)
- **缓存**: Redis

## 快速开始

### 1. 使用单页面版本（最简单）
```bash
# 直接在浏览器中打开
lottery.html
```

### 2. 部署 SaaS 版本（Docker）
```bash
# 启动所有服务
docker-compose up -d

# 访问前端
http://localhost

# 访问后端 API
http://localhost:8080/api

# 访问 API 文档
http://localhost:8080/api/doc.html
```

### 3. 本地开发

#### 后端开发
```bash
cd lottery-backend

# 安装依赖并启动
mvn spring-boot:run
```

#### 前端开发
```bash
cd lottery-frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

## 核心特性对比

| 特性 | 单页面版本 | SaaS 版本 |
|------|-----------|----------|
| 部署复杂度 | ⭐ 极简 | ⭐⭐⭐ 需要服务器 |
| 数据持久化 | ❌ | ✅ PostgreSQL |
| 多租户支持 | ❌ | ✅ Schema 隔离 |
| 权限管理 | ❌ | ✅ 三级权限 |
| 历史查询 | ❌ | ✅ 完整记录 |
| 用户管理 | ❌ | ✅ 租户内用户 |
| 数据统计 | ❌ | ✅ 实时统计 |

## 数据库初始化

### PostgreSQL 初始化步骤
```sql
-- 1. 创建数据库和用户
CREATE DATABASE lottery_db;
CREATE USER lottery_user WITH PASSWORD 'lottery_pass';
GRANT ALL PRIVILEGES ON DATABASE lottery_db TO lottery_user;

-- 2. 执行 Public Schema 初始化
\i lottery-backend/src/main/resources/db/schema/init_public_schema.sql

-- 3. 执行系统配置初始化
\i lottery-backend/src/main/resources/db/data/init_system_config.sql
```

## 开发进度

### 已完成 ✅
- [x] 数据库设计和初始化脚本
- [x] 后端项目框架搭建
- [x] 后端公共模块（上下文、异常、响应）
- [x] 前端项目框架搭建
- [x] 前端路由和状态管理
- [x] Docker 部署配置

### 进行中 🚧
- [ ] 后端多租户核心实现
- [ ] 后端业务服务层
- [ ] 前端页面组件开发

### 待开发 📋
- [ ] 认证授权模块
- [ ] 活动管理模块
- [ ] 抽奖核心模块
- [ ] 数据查询模块
- [ ] 前端完整页面

## 参考文档

- 📐 [系统架构设计](./.qoder/quests/system-architecture-restructuring.md)
- 📖 [原始项目概述](./.qoder/repowiki/zh/content/项目概述.md)
- 🎯 [核心功能说明](./.qoder/repowiki/zh/content/核心功能.md)
- 🏗️ [技术架构](./.qoder/repowiki/zh/content/技术架构.md)

## 联系方式

如有问题或建议，请提交 Issue。
