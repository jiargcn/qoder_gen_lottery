-- =====================================================
-- PostgreSQL Public Schema 初始化脚本
-- 用途：创建公共区域的租户注册表和系统配置表
-- =====================================================

-- 创建租户注册表
CREATE TABLE IF NOT EXISTS public.tenant_registry (
    tenant_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_code VARCHAR(50) UNIQUE NOT NULL,
    tenant_name VARCHAR(100) NOT NULL,
    schema_name VARCHAR(63) UNIQUE NOT NULL,
    admin_user_id UUID,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    subscription_plan VARCHAR(50),
    max_users INTEGER DEFAULT 10,
    max_activities INTEGER DEFAULT 100,
    storage_quota_mb INTEGER DEFAULT 1024,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expired_at TIMESTAMP
);

-- 创建租户代码索引
CREATE INDEX IF NOT EXISTS idx_tenant_code ON public.tenant_registry(tenant_code);

-- 创建租户状态索引
CREATE INDEX IF NOT EXISTS idx_tenant_status ON public.tenant_registry(status);

-- 添加表注释
COMMENT ON TABLE public.tenant_registry IS '租户注册表，存储所有租户的基本信息和配额';
COMMENT ON COLUMN public.tenant_registry.tenant_id IS '租户唯一标识';
COMMENT ON COLUMN public.tenant_registry.tenant_code IS '租户代码，用于子域名识别';
COMMENT ON COLUMN public.tenant_registry.schema_name IS 'Schema 名称，格式为 tenant_{id}';
COMMENT ON COLUMN public.tenant_registry.status IS '租户状态：ACTIVE-激活, SUSPENDED-暂停';

-- =====================================================

-- 创建系统配置表
CREATE TABLE IF NOT EXISTS public.system_config (
    config_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    config_key VARCHAR(100) UNIQUE NOT NULL,
    config_value TEXT,
    config_type VARCHAR(20) DEFAULT 'STRING',
    description VARCHAR(500),
    is_public BOOLEAN DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 创建配置键索引
CREATE INDEX IF NOT EXISTS idx_config_key ON public.system_config(config_key);

-- 添加表注释
COMMENT ON TABLE public.system_config IS '系统配置表，存储全局配置信息';
COMMENT ON COLUMN public.system_config.config_type IS '配置类型：STRING-字符串, JSON-JSON对象, NUMBER-数字';
COMMENT ON COLUMN public.system_config.is_public IS '是否公开配置（可被前端访问）';

-- =====================================================

-- 创建更新时间自动触发器函数
CREATE OR REPLACE FUNCTION public.update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 为租户注册表添加更新时间触发器
DROP TRIGGER IF EXISTS update_tenant_registry_updated_at ON public.tenant_registry;
CREATE TRIGGER update_tenant_registry_updated_at
    BEFORE UPDATE ON public.tenant_registry
    FOR EACH ROW
    EXECUTE FUNCTION public.update_updated_at_column();

-- 为系统配置表添加更新时间触发器
DROP TRIGGER IF EXISTS update_system_config_updated_at ON public.system_config;
CREATE TRIGGER update_system_config_updated_at
    BEFORE UPDATE ON public.system_config
    FOR EACH ROW
    EXECUTE FUNCTION public.update_updated_at_column();

-- =====================================================
-- 初始化完成
-- =====================================================
