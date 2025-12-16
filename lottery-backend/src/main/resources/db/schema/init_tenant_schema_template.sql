-- =====================================================
-- 租户 Schema 初始化模板脚本
-- 用途：为每个新租户创建独立的表结构
-- 使用方式：将 {SCHEMA_NAME} 替换为实际的租户 schema 名称
-- =====================================================

-- 设置 search_path（在代码中动态替换）
-- SET search_path TO {SCHEMA_NAME}, public;

-- =====================================================
-- 1. 用户表
-- =====================================================
CREATE TABLE IF NOT EXISTS users (
    user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20),
    real_name VARCHAR(50),
    role VARCHAR(20) NOT NULL DEFAULT 'OPERATOR',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID
);

CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_status ON users(status);

COMMENT ON TABLE users IS '租户用户表';
COMMENT ON COLUMN users.role IS '角色：ADMIN-管理员, OPERATOR-操作员, VIEWER-查看者';
COMMENT ON COLUMN users.status IS '状态：ACTIVE-激活, DISABLED-禁用';

-- =====================================================
-- 2. 抽奖活动表
-- =====================================================
CREATE TABLE IF NOT EXISTS lottery_activities (
    activity_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(50) NOT NULL,
    activity_name VARCHAR(100) NOT NULL,
    activity_type VARCHAR(50) DEFAULT '年会',
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    total_participants INTEGER DEFAULT 0,
    total_winners INTEGER DEFAULT 0,
    config_json JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_activity_tenant ON lottery_activities(tenant_id);
CREATE INDEX IF NOT EXISTS idx_activity_status ON lottery_activities(status);
CREATE INDEX IF NOT EXISTS idx_activity_created_by ON lottery_activities(created_by);
CREATE INDEX IF NOT EXISTS idx_activity_created_at ON lottery_activities(created_at DESC);

COMMENT ON TABLE lottery_activities IS '抽奖活动表';
COMMENT ON COLUMN lottery_activities.status IS '状态：DRAFT-草稿, ACTIVE-进行中, COMPLETED-已完成, ARCHIVED-已归档';
COMMENT ON COLUMN lottery_activities.config_json IS '活动配置JSON：主题、动画设置、抽奖规则等';

-- =====================================================
-- 3. 奖项表
-- =====================================================
CREATE TABLE IF NOT EXISTS prizes (
    prize_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    activity_id UUID NOT NULL,
    prize_name VARCHAR(100) NOT NULL,
    prize_level INTEGER NOT NULL,
    gift_name VARCHAR(200),
    gift_image_url VARCHAR(500),
    total_quota INTEGER NOT NULL,
    drawn_count INTEGER DEFAULT 0,
    draw_order INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_prize_activity FOREIGN KEY (activity_id) 
        REFERENCES lottery_activities(activity_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_prize_activity ON prizes(activity_id);
CREATE INDEX IF NOT EXISTS idx_prize_draw_order ON prizes(activity_id, draw_order);
CREATE INDEX IF NOT EXISTS idx_prize_status ON prizes(status);

COMMENT ON TABLE prizes IS '奖项表';
COMMENT ON COLUMN prizes.prize_level IS '奖项等级，数字越小等级越高';
COMMENT ON COLUMN prizes.draw_order IS '抽奖顺序，决定抽奖先后';
COMMENT ON COLUMN prizes.status IS '状态：PENDING-待抽取, DRAWING-抽取中, COMPLETED-已抽完';

-- =====================================================
-- 4. 参与人员表
-- =====================================================
CREATE TABLE IF NOT EXISTS participants (
    participant_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    activity_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    employee_no VARCHAR(50),
    department VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(100),
    extra_info JSONB,
    is_winner BOOLEAN DEFAULT false,
    import_batch VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_participant_activity FOREIGN KEY (activity_id) 
        REFERENCES lottery_activities(activity_id) ON DELETE CASCADE,
    CONSTRAINT unique_participant UNIQUE (activity_id, name, employee_no)
);

CREATE INDEX IF NOT EXISTS idx_participant_activity ON participants(activity_id);
CREATE INDEX IF NOT EXISTS idx_participant_is_winner ON participants(activity_id, is_winner);
CREATE INDEX IF NOT EXISTS idx_participant_name ON participants(name);

COMMENT ON TABLE participants IS '参与人员表';
COMMENT ON COLUMN participants.is_winner IS '是否已中奖，用于快速过滤';
COMMENT ON COLUMN participants.import_batch IS '导入批次号，用于追溯导入来源';

-- =====================================================
-- 5. 中奖记录表
-- =====================================================
CREATE TABLE IF NOT EXISTS winner_records (
    record_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    activity_id UUID NOT NULL,
    prize_id UUID NOT NULL,
    participant_id UUID NOT NULL,
    participant_name VARCHAR(100) NOT NULL,
    prize_name VARCHAR(100) NOT NULL,
    gift_name VARCHAR(200),
    draw_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    draw_sequence INTEGER NOT NULL,
    operator_id UUID,
    operator_name VARCHAR(50),
    remarks TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_winner_activity FOREIGN KEY (activity_id) 
        REFERENCES lottery_activities(activity_id) ON DELETE CASCADE,
    CONSTRAINT fk_winner_prize FOREIGN KEY (prize_id) 
        REFERENCES prizes(prize_id) ON DELETE CASCADE,
    CONSTRAINT fk_winner_participant FOREIGN KEY (participant_id) 
        REFERENCES participants(participant_id) ON DELETE CASCADE,
    CONSTRAINT unique_winner UNIQUE (activity_id, participant_id)
);

CREATE INDEX IF NOT EXISTS idx_winner_activity ON winner_records(activity_id);
CREATE INDEX IF NOT EXISTS idx_winner_prize ON winner_records(prize_id);
CREATE INDEX IF NOT EXISTS idx_winner_draw_time ON winner_records(activity_id, draw_time);

COMMENT ON TABLE winner_records IS '中奖记录表';
COMMENT ON COLUMN winner_records.draw_sequence IS '抽奖序号，表示该中奖者是第几个抽出的';
COMMENT ON COLUMN winner_records.participant_name IS '中奖人姓名冗余字段，提升查询性能';
COMMENT ON COLUMN winner_records.prize_name IS '奖项名称冗余字段，提升查询性能';

-- =====================================================
-- 6. 操作日志表
-- =====================================================
CREATE TABLE IF NOT EXISTS operation_logs (
    log_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    activity_id UUID,
    operator_id UUID NOT NULL,
    operator_name VARCHAR(50),
    operation_type VARCHAR(50) NOT NULL,
    operation_desc VARCHAR(500),
    target_type VARCHAR(50),
    target_id UUID,
    request_params JSONB,
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_log_activity ON operation_logs(activity_id);
CREATE INDEX IF NOT EXISTS idx_log_operator ON operation_logs(operator_id);
CREATE INDEX IF NOT EXISTS idx_log_created_at ON operation_logs(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_log_operation_type ON operation_logs(operation_type);

COMMENT ON TABLE operation_logs IS '操作日志表，记录所有重要操作';
COMMENT ON COLUMN operation_logs.operation_type IS '操作类型：CREATE_ACTIVITY, DRAW_LOTTERY, RESET_LOTTERY等';
COMMENT ON COLUMN operation_logs.target_type IS '目标类型：ACTIVITY, PRIZE, PARTICIPANT等';

-- =====================================================
-- 7. 创建更新时间触发器
-- =====================================================

-- 为用户表添加更新时间触发器
DROP TRIGGER IF EXISTS update_users_updated_at ON users;
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION public.update_updated_at_column();

-- 为活动表添加更新时间触发器
DROP TRIGGER IF EXISTS update_lottery_activities_updated_at ON lottery_activities;
CREATE TRIGGER update_lottery_activities_updated_at
    BEFORE UPDATE ON lottery_activities
    FOR EACH ROW
    EXECUTE FUNCTION public.update_updated_at_column();

-- 为奖项表添加更新时间触发器
DROP TRIGGER IF EXISTS update_prizes_updated_at ON prizes;
CREATE TRIGGER update_prizes_updated_at
    BEFORE UPDATE ON prizes
    FOR EACH ROW
    EXECUTE FUNCTION public.update_updated_at_column();

-- 为参与人员表添加更新时间触发器
DROP TRIGGER IF EXISTS update_participants_updated_at ON participants;
CREATE TRIGGER update_participants_updated_at
    BEFORE UPDATE ON participants
    FOR EACH ROW
    EXECUTE FUNCTION public.update_updated_at_column();

-- =====================================================
-- 租户 Schema 初始化完成
-- =====================================================
