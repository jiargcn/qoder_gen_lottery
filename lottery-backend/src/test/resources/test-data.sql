-- 测试租户数据
INSERT INTO tenants (tenant_id, tenant_code, tenant_name, schema_name, status, created_at, updated_at)
VALUES ('test-tenant-1', 'TEST001', '测试租户1', 'tenant_test001', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('test-tenant-2', 'TEST002', '测试租户2', 'tenant_test002', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 测试用户数据（租户1）
-- 注意：密码为 'password123' 的 BCrypt 哈希值
INSERT INTO tenant_test001.users (user_id, tenant_id, username, password, role, status, created_at, updated_at)
VALUES ('user-1-1', 'test-tenant-1', 'admin1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8w8W5HZJbqRvdJXkYm', 'ADMIN', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('user-1-2', 'test-tenant-1', 'operator1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8w8W5HZJbqRvdJXkYm', 'OPERATOR', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('user-1-3', 'test-tenant-1', 'viewer1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8w8W5HZJbqRvdJXkYm', 'VIEWER', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 测试活动数据（租户1）
INSERT INTO tenant_test001.lottery_activities (activity_id, activity_name, activity_date, status, created_at, updated_at)
VALUES ('activity-1-1', '2025年春节年会', '2025-01-25 18:00:00', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('activity-1-2', '2025年中秋晚会', '2025-09-15 19:00:00', 'DRAFT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 测试奖项数据（活动1-1）
INSERT INTO tenant_test001.prizes (prize_id, activity_id, prize_name, prize_level, total_count, remaining_count, created_at, updated_at)
VALUES ('prize-1-1-1', 'activity-1-1', '特等奖', 1, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('prize-1-1-2', 'activity-1-1', '一等奖', 2, 3, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('prize-1-1-3', 'activity-1-1', '二等奖', 3, 5, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('prize-1-1-4', 'activity-1-1', '三等奖', 4, 10, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 测试参会人员数据（活动1-1）
INSERT INTO tenant_test001.participants (participant_id, activity_id, name, department, employee_id, has_won, created_at, updated_at)
VALUES ('participant-1-1-1', 'activity-1-1', '张三', '技术部', 'E001', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('participant-1-1-2', 'activity-1-1', '李四', '产品部', 'E002', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('participant-1-1-3', 'activity-1-1', '王五', '市场部', 'E003', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('participant-1-1-4', 'activity-1-1', '赵六', '销售部', 'E004', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('participant-1-1-5', 'activity-1-1', '孙七', '人力资源部', 'E005', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('participant-1-1-6', 'activity-1-1', '周八', '财务部', 'E006', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('participant-1-1-7', 'activity-1-1', '吴九', '运营部', 'E007', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('participant-1-1-8', 'activity-1-1', '郑十', '客服部', 'E008', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('participant-1-1-9', 'activity-1-1', '钱十一', '行政部', 'E009', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('participant-1-1-10', 'activity-1-1', '陈十二', '技术部', 'E010', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
