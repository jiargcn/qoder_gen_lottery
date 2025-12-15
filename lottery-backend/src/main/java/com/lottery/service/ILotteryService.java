package com.lottery.service;

import com.lottery.entity.dto.WinnerSaveDTO;
import com.lottery.entity.vo.LotteryDataVO;
import com.lottery.entity.vo.WinnerVO;

import java.util.List;

/**
 * 抽奖服务接口
 */
public interface ILotteryService {
    
    /**
     * 获取抽奖初始化数据
     * 
     * @param activityId 活动 ID
     * @return 抽奖数据
     */
    LotteryDataVO getLotteryData(String activityId);
    
    /**
     * 保存中奖记录
     * 
     * @param winnerSaveDTO 中奖记录信息
     * @return 中奖记录
     */
    WinnerVO saveWinner(WinnerSaveDTO winnerSaveDTO);
    
    /**
     * 查询中奖记录列表
     * 
     * @param activityId 活动 ID
     * @return 中奖记录列表
     */
    List<WinnerVO> getWinners(String activityId);
    
    /**
     * 重置抽奖(清空中奖记录)
     * 
     * @param activityId 活动 ID
     */
    void resetLottery(String activityId);
}
