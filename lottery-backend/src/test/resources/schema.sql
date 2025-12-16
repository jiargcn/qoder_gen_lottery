-- 公共Schema：租户表
CREATE TABLE IF NOT EXISTS tenants (
    tenant_id VARCHAR(50) PRIMARY KEY,
    tenant_code VARCHAR(50) UNIQUE NOT NULL,
    tenant_name VARCHAR(100) NOT NULL,
    schema_name VARCHAR(50) NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建租户Schema
CREATE SCHEMA IF NOT EXISTS tenant_test001;
CREATE SCHEMA IF NOT EXISTS tenant_test002;

-- 租户Schema：用户表
CREATE TABLE IF NOT EXISTS tenant_test001.users (
    user_id VARCHAR(50) PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tenant_test002.users (
    user_id VARCHAR(50) PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 租户Schema：活动表
CREATE TABLE IF NOT EXISTS tenant_test001.lottery_activities (
    activity_id VARCHAR(50) PRIMARY KEY,
    activity_name VARCHAR(100) NOT NULL,
    activity_date TIMESTAMP,
    status VARCHAR(20) DEFAULT 'DRAFT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tenant_test002.lottery_activities (
    activity_id VARCHAR(50) PRIMARY KEY,
    activity_name VARCHAR(100) NOT NULL,
    activity_date TIMESTAMP,
    status VARCHAR(20) DEFAULT 'DRAFT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 租户Schema：奖项表
CREATE TABLE IF NOT EXISTS tenant_test001.prizes (
    prize_id VARCHAR(50) PRIMARY KEY,
    activity_id VARCHAR(50) NOT NULL,
    prize_name VARCHAR(100) NOT NULL,
    prize_level INTEGER NOT NULL,
    total_count INTEGER NOT NULL,
    remaining_count INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tenant_test002.prizes (
    prize_id VARCHAR(50) PRIMARY KEY,
    activity_id VARCHAR(50) NOT NULL,
    prize_name VARCHAR(100) NOT NULL,
    prize_level INTEGER NOT NULL,
    total_count INTEGER NOT NULL,
    remaining_count INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 租户Schema：参会人员表
CREATE TABLE IF NOT EXISTS tenant_test001.participants (
    participant_id VARCHAR(50) PRIMARY KEY,
    activity_id VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    department VARCHAR(100),
    employee_id VARCHAR(50),
    has_won BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tenant_test002.participants (
    participant_id VARCHAR(50) PRIMARY KEY,
    activity_id VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    department VARCHAR(100),
    employee_id VARCHAR(50),
    has_won BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 租户Schema：中奖记录表
CREATE TABLE IF NOT EXISTS tenant_test001.winner_records (
    record_id VARCHAR(50) PRIMARY KEY,
    activity_id VARCHAR(50) NOT NULL,
    prize_id VARCHAR(50) NOT NULL,
    participant_id VARCHAR(50) NOT NULL,
    won_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tenant_test002.winner_records (
    record_id VARCHAR(50) PRIMARY KEY,
    activity_id VARCHAR(50) NOT NULL,
    prize_id VARCHAR(50) NOT NULL,
    participant_id VARCHAR(50) NOT NULL,
    won_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
