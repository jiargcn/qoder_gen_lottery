package com.lottery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lottery.entity.po.Prize;
import org.apache.ibatis.annotations.Mapper;

/**
 * 奖项 Mapper 接口
 */
@Mapper
public interface PrizeMapper extends BaseMapper<Prize> {
}
