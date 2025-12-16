-- 批量插入100个参会人员到活动4444
-- 步骤1: 首先查找活动ID
DO $$
DECLARE
    v_activity_id VARCHAR(50);
    v_tenant_id VARCHAR(50);
    v_participant_id VARCHAR(50);
    i INTEGER;
BEGIN
    -- 查找活动4444的ID和租户ID
    SELECT activity_id, tenant_id INTO v_activity_id, v_tenant_id
    FROM lottery_activities 
    WHERE activity_name = '4444' 
    LIMIT 1;
    
    IF v_activity_id IS NULL THEN
        RAISE EXCEPTION '未找到名为4444的活动';
    END IF;
    
    RAISE NOTICE '找到活动: ID=%, 租户ID=%', v_activity_id, v_tenant_id;
    
    -- 批量插入100个参会人员
    FOR i IN 1..100 LOOP
        v_participant_id := replace(gen_random_uuid()::text, '-', '');
        
        INSERT INTO participants (
            participant_id,
            activity_id,
            name,
            employee_no,
            department,
            phone,
            email,
            is_winner,
            created_at,
            updated_at
        ) VALUES (
            v_participant_id,
            v_activity_id,
            '参会人员' || i,
            'EMP' || LPAD(i::text, 4, '0'),
            '部门' || ((i - 1) % 10 + 1),
            '138' || LPAD(i::text, 8, '0'),
            'user' || i || '@company.com',
            false,
            NOW(),
            NOW()
        );
        
        -- 每10个显示一次进度
        IF i % 10 = 0 THEN
            RAISE NOTICE '已插入 % 个人员...', i;
        END IF;
    END LOOP;
    
    -- 更新活动的总参与人数
    UPDATE lottery_activities 
    SET total_participants = total_participants + 100,
        updated_at = NOW()
    WHERE activity_id = v_activity_id;
    
    RAISE NOTICE '完成! 成功插入100个参会人员';
END $$;
