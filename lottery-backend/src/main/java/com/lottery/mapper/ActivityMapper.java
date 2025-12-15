package com.lottery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lottery.entity.po.LotteryActivity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 抽奖活动 Mapper 接口
 */
@Mapper
public interface ActivityMapper extends BaseMapper<LotteryActivity> {
}
