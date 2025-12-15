package com.lottery.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lottery.common.exception.BizException;
import com.lottery.entity.dto.WinnerSaveDTO;
import com.lottery.entity.po.*;
import com.lottery.entity.vo.LotteryDataVO;
import com.lottery.entity.vo.WinnerVO;
import com.lottery.mapper.*;
import com.lottery.service.ILotteryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 抽奖服务实现
 */
@Slf4j
@Service
public class LotteryServiceImpl implements ILotteryService {
    
    @Autowired
    private ActivityMapper activityMapper;
    
    @Autowired
    private PrizeMapper prizeMapper;
    
    @Autowired
    private ParticipantMapper participantMapper;
    
    @Autowired
    private WinnerRecordMapper winnerRecordMapper;
    
    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;
    
    @Override
    public LotteryDataVO getLotteryData(String activityId) {
        // 尝试从缓存获取
        String cacheKey = "lottery:" + activityId;
        if (redisTemplate != null) {
            LotteryDataVO cached = (LotteryDataVO) redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.debug("从缓存获取抽奖数据: activityId={}", activityId);
                return cached;
            }
        }
        
        // 1. 查询活动信息
        LotteryActivity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BizException("活动不存在");
        }
        
        // 2. 查询奖项列表
        LambdaQueryWrapper<Prize> prizeWrapper = new LambdaQueryWrapper<>();
        prizeWrapper.eq(Prize::getActivityId, activityId)
                   .orderByAsc(Prize::getDrawOrder);
        List<Prize> prizes = prizeMapper.selectList(prizeWrapper);
        
        // 3. 查询参与人员列表
        LambdaQueryWrapper<Participant> participantWrapper = new LambdaQueryWrapper<>();
        participantWrapper.eq(Participant::getActivityId, activityId);
        List<Participant> participants = participantMapper.selectList(participantWrapper);
        
        // 4. 查询中奖记录
        LambdaQueryWrapper<WinnerRecord> winnerWrapper = new LambdaQueryWrapper<>();
        winnerWrapper.eq(WinnerRecord::getActivityId, activityId)
                    .orderByAsc(WinnerRecord::getDrawTime);
        List<WinnerRecord> winners = winnerRecordMapper.selectList(winnerWrapper);
        
        // 5. 组装返回数据
        LotteryDataVO result = new LotteryDataVO();
        
        // 活动信息
        LotteryDataVO.ActivityInfo activityInfo = new LotteryDataVO.ActivityInfo();
        activityInfo.setActivityId(activity.getActivityId());
        activityInfo.setActivityName(activity.getActivityName());
        activityInfo.setStatus(activity.getStatus());
        activityInfo.setTotalParticipants(activity.getTotalParticipants());
        activityInfo.setTotalWinners(activity.getTotalWinners());
        result.setActivity(activityInfo);
        
        // 奖项信息
        List<LotteryDataVO.PrizeInfo> prizeInfos = prizes.stream().map(prize -> {
            LotteryDataVO.PrizeInfo info = new LotteryDataVO.PrizeInfo();
            BeanUtil.copyProperties(prize, info);
            return info;
        }).collect(Collectors.toList());
        result.setPrizes(prizeInfos);
        
        // 参与人员信息
        List<LotteryDataVO.ParticipantInfo> participantInfos = participants.stream().map(p -> {
            LotteryDataVO.ParticipantInfo info = new LotteryDataVO.ParticipantInfo();
            info.setParticipantId(p.getParticipantId());
            info.setName(p.getName());
            info.setEmployeeNo(p.getEmployeeNo());
            info.setDepartment(p.getDepartment());
            info.setIsWinner(p.getIsWinner());
            return info;
        }).collect(Collectors.toList());
        result.setParticipants(participantInfos);
        
        // 中奖记录按奖项分组
        Map<String, List<LotteryDataVO.WinnerInfo>> winnersMap = winners.stream()
            .map(w -> {
                LotteryDataVO.WinnerInfo info = new LotteryDataVO.WinnerInfo();
                info.setRecordId(w.getRecordId());
                info.setParticipantId(w.getParticipantId());
                info.setParticipantName(w.getParticipantName());
                info.setPrizeName(w.getPrizeName());
                info.setGiftName(w.getGiftName());
                info.setDrawSequence(w.getDrawSequence());
                return info;
            })
            .collect(Collectors.groupingBy(w -> w.getPrizeName()));
        result.setWinners(winnersMap);
        
        // 缓存数据
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(cacheKey, result, 30, TimeUnit.MINUTES);
        }
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public WinnerVO saveWinner(WinnerSaveDTO winnerSaveDTO) {
        // 1. 查询参与人员和奖项信息
        Participant participant = participantMapper.selectById(winnerSaveDTO.getParticipantId());
        if (participant == null) {
            throw new BizException("参与人员不存在");
        }
        
        if (participant.getIsWinner()) {
            throw new BizException("该人员已中奖，不能重复中奖");
        }
        
        Prize prize = prizeMapper.selectById(winnerSaveDTO.getPrizeId());
        if (prize == null) {
            throw new BizException("奖项不存在");
        }
        
        if (prize.getDrawnCount() >= prize.getTotalQuota()) {
            throw new BizException("该奖项名额已满");
        }
        
        // 2. 插入中奖记录
        WinnerRecord record = new WinnerRecord();
        record.setActivityId(winnerSaveDTO.getActivityId());
        record.setPrizeId(winnerSaveDTO.getPrizeId());
        record.setParticipantId(winnerSaveDTO.getParticipantId());
        record.setParticipantName(participant.getName());
        record.setPrizeName(prize.getPrizeName());
        record.setGiftName(prize.getGiftName());
        record.setDrawTime(winnerSaveDTO.getDrawTime() != null ? winnerSaveDTO.getDrawTime() : LocalDateTime.now());
        record.setDrawSequence(winnerSaveDTO.getDrawSequence());
        record.setRemarks(winnerSaveDTO.getRemarks());
        record.setCreatedAt(LocalDateTime.now());
        
        winnerRecordMapper.insert(record);
        
        // 3. 更新参与人员状态
        participant.setIsWinner(true);
        participant.setUpdatedAt(LocalDateTime.now());
        participantMapper.updateById(participant);
        
        // 4. 更新奖项已抽取数量
        prize.setDrawnCount(prize.getDrawnCount() + 1);
        prize.setUpdatedAt(LocalDateTime.now());
        
        // 如果抽完了，更新奖项状态
        if (prize.getDrawnCount().equals(prize.getTotalQuota())) {
            prize.setStatus("COMPLETED");
        }
        prizeMapper.updateById(prize);
        
        // 5. 更新活动中奖总人数
        LotteryActivity activity = activityMapper.selectById(winnerSaveDTO.getActivityId());
        activity.setTotalWinners(activity.getTotalWinners() + 1);
        activity.setUpdatedAt(LocalDateTime.now());
        activityMapper.updateById(activity);
        
        // 6. 清除缓存
        if (redisTemplate != null) {
            redisTemplate.delete("lottery:" + winnerSaveDTO.getActivityId());
        }
        
        log.info("保存中奖记录成功: activityId={}, prizeId={}, participantId={}", 
                winnerSaveDTO.getActivityId(), winnerSaveDTO.getPrizeId(), winnerSaveDTO.getParticipantId());
        
        // 7. 返回中奖记录
        WinnerVO winnerVO = BeanUtil.copyProperties(record, WinnerVO.class);
        winnerVO.setActivityName(activity.getActivityName());
        return winnerVO;
    }
    
    @Override
    public List<WinnerVO> getWinners(String activityId) {
        LambdaQueryWrapper<WinnerRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WinnerRecord::getActivityId, activityId)
               .orderByAsc(WinnerRecord::getDrawTime);
        List<WinnerRecord> records = winnerRecordMapper.selectList(wrapper);
        
        // 查询活动名称
        LotteryActivity activity = activityMapper.selectById(activityId);
        String activityName = activity != null ? activity.getActivityName() : "";
        
        return records.stream().map(record -> {
            WinnerVO vo = BeanUtil.copyProperties(record, WinnerVO.class);
            vo.setActivityName(activityName);
            return vo;
        }).collect(Collectors.toList());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetLottery(String activityId) {
        log.info("开始重置抽奖: activityId={}", activityId);
        
        // 1. 删除中奖记录
        LambdaQueryWrapper<WinnerRecord> winnerWrapper = new LambdaQueryWrapper<>();
        winnerWrapper.eq(WinnerRecord::getActivityId, activityId);
        winnerRecordMapper.delete(winnerWrapper);
        
        // 2. 重置参与人员中奖状态
        LambdaQueryWrapper<Participant> participantWrapper = new LambdaQueryWrapper<>();
        participantWrapper.eq(Participant::getActivityId, activityId);
        List<Participant> participants = participantMapper.selectList(participantWrapper);
        for (Participant p : participants) {
            p.setIsWinner(false);
            p.setUpdatedAt(LocalDateTime.now());
            participantMapper.updateById(p);
        }
        
        // 3. 重置奖项已抽取数量
        LambdaQueryWrapper<Prize> prizeWrapper = new LambdaQueryWrapper<>();
        prizeWrapper.eq(Prize::getActivityId, activityId);
        List<Prize> prizes = prizeMapper.selectList(prizeWrapper);
        for (Prize prize : prizes) {
            prize.setDrawnCount(0);
            prize.setStatus("PENDING");
            prize.setUpdatedAt(LocalDateTime.now());
            prizeMapper.updateById(prize);
        }
        
        // 4. 重置活动中奖总人数
        LotteryActivity activity = activityMapper.selectById(activityId);
        if (activity != null) {
            activity.setTotalWinners(0);
            activity.setUpdatedAt(LocalDateTime.now());
            activityMapper.updateById(activity);
        }
        
        // 5. 清除缓存
        if (redisTemplate != null) {
            redisTemplate.delete("lottery:" + activityId);
        }
        
        log.info("抽奖重置成功: activityId={}", activityId);
    }
    
    // ==================== 活动管理 ====================
    
    @Override
    public List<LotteryActivity> getActivities() {
        // TODO: 根据当前租户ID查询，这里先查询全部
        return activityMapper.selectList(null);
    }
    
    @Override
    public LotteryActivity getActivity(String activityId) {
        LotteryActivity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BizException("活动不存在");
        }
        return activity;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LotteryActivity createActivity(LotteryActivity activity) {
        // 生成活动ID
        activity.setActivityId(UUID.randomUUID().toString().replace("-", ""));
        activity.setStatus("ACTIVE");
        activity.setTotalParticipants(0);
        activity.setTotalWinners(0);
        activity.setCreatedAt(LocalDateTime.now());
        activity.setUpdatedAt(LocalDateTime.now());
        // TODO: 设置当前租户ID
        activityMapper.insert(activity);
        log.info("创建活动成功: activityId={}, activityName={}", 
                activity.getActivityId(), activity.getActivityName());
        return activity;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LotteryActivity updateActivity(LotteryActivity activity) {
        LotteryActivity existing = activityMapper.selectById(activity.getActivityId());
        if (existing == null) {
            throw new BizException("活动不存在");
        }
        activity.setUpdatedAt(LocalDateTime.now());
        activityMapper.updateById(activity);
        
        // 清除缓存
        if (redisTemplate != null) {
            redisTemplate.delete("lottery:" + activity.getActivityId());
        }
        
        log.info("更新活动成功: activityId={}", activity.getActivityId());
        return activity;
    }
    
    // ==================== 奖项管理 ====================
    
    @Override
    public List<Prize> getPrizes(String activityId) {
        LambdaQueryWrapper<Prize> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Prize::getActivityId, activityId)
               .orderByAsc(Prize::getDrawOrder);
        return prizeMapper.selectList(wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Prize createPrize(Prize prize) {
        // 生成奖项ID
        prize.setPrizeId(UUID.randomUUID().toString().replace("-", ""));
        prize.setDrawnCount(0);
        prize.setStatus("PENDING");
        
        // 设置抽奖顺序（prizeLevel 就是 drawOrder）
        if (prize.getDrawOrder() == null && prize.getPrizeLevel() != null) {
            prize.setDrawOrder(prize.getPrizeLevel());
        }
        
        prize.setCreatedAt(LocalDateTime.now());
        prize.setUpdatedAt(LocalDateTime.now());
        prizeMapper.insert(prize);
        
        // 清除缓存
        if (redisTemplate != null) {
            redisTemplate.delete("lottery:" + prize.getActivityId());
        }
        
        log.info("创建奖项成功: prizeId={}, prizeName={}", 
                prize.getPrizeId(), prize.getPrizeName());
        return prize;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Prize updatePrize(Prize prize) {
        Prize existing = prizeMapper.selectById(prize.getPrizeId());
        if (existing == null) {
            throw new BizException("奖项不存在");
        }
        
        // 更新 drawOrder
        if (prize.getPrizeLevel() != null) {
            prize.setDrawOrder(prize.getPrizeLevel());
        }
        
        prize.setUpdatedAt(LocalDateTime.now());
        prizeMapper.updateById(prize);
        
        // 清除缓存
        if (redisTemplate != null) {
            redisTemplate.delete("lottery:" + existing.getActivityId());
        }
        
        log.info("更新奖项成功: prizeId={}", prize.getPrizeId());
        return prize;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePrize(String prizeId) {
        Prize prize = prizeMapper.selectById(prizeId);
        if (prize == null) {
            throw new BizException("奖项不存在");
        }
        
        // 检查是否已抽取
        if (prize.getDrawnCount() > 0) {
            throw new BizException("奖项已抽取，不能删除");
        }
        
        prizeMapper.deleteById(prizeId);
        
        // 清除缓存
        if (redisTemplate != null) {
            redisTemplate.delete("lottery:" + prize.getActivityId());
        }
        
        log.info("删除奖项成功: prizeId={}", prizeId);
    }
}
