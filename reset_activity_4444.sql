-- 重置活动4444：修改状态为进行中，删除中奖记录

DO $$
DECLARE
    v_activity_id VARCHAR(50);
    v_deleted_winners INTEGER;
BEGIN
    -- 查找活动4444的ID
    SELECT activity_id INTO v_activity_id
    FROM lottery_activities 
    WHERE activity_name = '4444' 
    LIMIT 1;
    
    IF v_activity_id IS NULL THEN
        RAISE EXCEPTION '未找到名为4444的活动';
    END IF;
    
    RAISE NOTICE '找到活动ID: %', v_activity_id;
    
    -- 删除该活动的所有中奖记录
    DELETE FROM winners 
    WHERE activity_id = v_activity_id;
    
    GET DIAGNOSTICS v_deleted_winners = ROW_COUNT;
    RAISE NOTICE '已删除 % 条中奖记录', v_deleted_winners;
    
    -- 重置该活动所有参会人员的中奖状态
    UPDATE participants 
    SET is_winner = false,
        prize_name = NULL,
        updated_at = NOW()
    WHERE activity_id = v_activity_id;
    
    RAISE NOTICE '已重置参会人员的中奖状态';
    
    -- 将活动状态修改为进行中(ACTIVE)
    UPDATE lottery_activities 
    SET status = 'ACTIVE',
        updated_at = NOW()
    WHERE activity_id = v_activity_id;
    
    RAISE NOTICE '活动状态已修改为进行中';
    
    RAISE NOTICE '操作完成！活动4444已重置为进行中状态';
END $$;
