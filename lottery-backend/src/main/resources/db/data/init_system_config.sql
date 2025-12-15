-- =====================================================
-- 系统配置初始数据
-- 用途：插入系统默认配置
-- =====================================================

INSERT INTO public.system_config (config_key, config_value, config_type, description, is_public)
VALUES 
    ('system.name', '年会抽奖系统', 'STRING', '系统名称', true),
    ('system.version', '1.0.0', 'STRING', '系统版本', true),
    ('system.description', 'SaaS 多租户抽奖平台', 'STRING', '系统描述', true),
    ('tenant.default_max_users', '10', 'NUMBER', '租户默认最大用户数', false),
    ('tenant.default_max_activities', '100', 'NUMBER', '租户默认最大活动数', false),
    ('tenant.default_storage_quota_mb', '1024', 'NUMBER', '租户默认存储配额(MB)', false),
    ('jwt.expiration_seconds', '7200', 'NUMBER', 'JWT Token 过期时间(秒)', false),
    ('jwt.refresh_expiration_seconds', '604800', 'NUMBER', 'JWT 刷新Token过期时间(7天)', false),
    ('file.max_upload_size_mb', '10', 'NUMBER', '文件上传最大大小(MB)', false),
    ('file.allowed_types', 'xlsx,xls,txt', 'STRING', '允许上传的文件类型', false),
    ('lottery.default_rolling_speed_ms', '50', 'NUMBER', '默认滚动速度(毫秒)', true),
    ('lottery.enable_fireworks', 'true', 'STRING', '是否启用烟花特效', true),
    ('lottery.enable_coins', 'true', 'STRING', '是否启用金币特效', true)
ON CONFLICT (config_key) DO NOTHING;

-- =====================================================
-- 初始化完成
-- =====================================================
