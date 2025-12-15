package com.lottery.service;

import com.lottery.entity.dto.WinnerSaveDTO;
import com.lottery.entity.po.LotteryActivity;
import com.lottery.entity.po.Prize;
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
    
    // ==================== 活动管理 ====================
    
    /**
     * 获取活动列表
     * 
     * @return 活动列表
     */
    List<LotteryActivity> getActivities();
    
    /**
     * 获取活动详情
     * 
     * @param activityId 活动 ID
     * @return 活动详情
     */
    LotteryActivity getActivity(String activityId);
    
    /**
     * 创建活动
     * 
     * @param activity 活动信息
     * @return 创建的活动
     */
    LotteryActivity createActivity(LotteryActivity activity);
    
    /**
     * 更新活动
     * 
     * @param activity 活动信息
     * @return 更新后的活动
     */
    LotteryActivity updateActivity(LotteryActivity activity);
    
    // ==================== 奖项管理 ====================
    
    /**
     * 获取活动的奖项列表
     * 
     * @param activityId 活动 ID
     * @return 奖项列表
     */
    List<Prize> getPrizes(String activityId);
    
    /**
     * 创建奖项
     * 
     * @param prize 奖项信息
     * @return 创建的奖项
     */
    Prize createPrize(Prize prize);
    
    /**
     * 更新奖项
     * 
     * @param prize 奖项信息
     * @return 更新后的奖项
     */
    Prize updatePrize(Prize prize);
    
    /**
     * 删除奖项
     * 
     * @param prizeId 奖项 ID
     */
    void deletePrize(String prizeId);
}
