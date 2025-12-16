-- 为 lottery_activities 表添加 tenant_id 字段
-- 执行时间: 2025-12-15

-- 添加 tenant_id 列（先允许为NULL）
ALTER TABLE lottery_activities 
ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(50);

-- 为现有数据设置默认的租户ID（假设现有数据都属于同一租户）
-- 注意：这里需要根据实际情况修改默认租户ID
UPDATE lottery_activities 
SET tenant_id = 'default-tenant' 
WHERE tenant_id IS NULL;

-- 设置为NOT NULL约束
ALTER TABLE lottery_activities 
ALTER COLUMN tenant_id SET NOT NULL;

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_activity_tenant ON lottery_activities(tenant_id);

-- 添加注释
COMMENT ON COLUMN lottery_activities.tenant_id IS '租户ID，用于多租户数据隔离';
