package com.lottery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lottery.entity.po.Participant;
import org.apache.ibatis.annotations.Mapper;

/**
 * 参与人员 Mapper 接口
 */
@Mapper
public interface ParticipantMapper extends BaseMapper<Participant> {
}
