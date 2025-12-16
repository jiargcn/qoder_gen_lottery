package com.lottery.controller;

import com.lottery.common.response.Result;
import com.lottery.entity.dto.WinnerSaveDTO;
import com.lottery.entity.po.LotteryActivity;
import com.lottery.entity.po.Participant;
import com.lottery.entity.po.Prize;
import com.lottery.entity.vo.LotteryDataVO;
import com.lottery.entity.vo.WinnerVO;
import com.lottery.service.ILotteryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 抽奖控制器
 */
@Tag(name = "抽奖管理", description = "抽奖数据初始化、中奖记录保存、抽奖重置等接口")
@RestController
@RequestMapping("/lottery")
public class LotteryController {
    
    @Autowired
    private ILotteryService lotteryService;
    
    /**
     * 获取抽奖初始化数据
     */
    @Operation(summary = "获取抽奖初始化数据", description = "获取活动、奖项、参与人员、中奖记录等完整数据")
    @GetMapping("/activities/{activityId}/data")
    // @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")  // 开发环境暂时禁用
    public Result<LotteryDataVO> getLotteryData(@PathVariable String activityId) {
        LotteryDataVO data = lotteryService.getLotteryData(activityId);
        return Result.success(data);
    }
    
    /**
     * 保存中奖记录
     */
    @Operation(summary = "保存中奖记录", description = "保存单个中奖记录，更新相关状态")
    @PostMapping("/winners")
    // @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")  // 开发环境暂时禁用
    public Result<WinnerVO> saveWinner(@Valid @RequestBody WinnerSaveDTO winnerSaveDTO) {
        WinnerVO winner = lotteryService.saveWinner(winnerSaveDTO);
        return Result.success(winner);
    }
    
    /**
     * 查询中奖记录列表
     */
    @Operation(summary = "查询中奖记录列表", description = "查询指定活动的所有中奖记录")
    @GetMapping("/activities/{activityId}/winners")
    // @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")  // 开发环境暂时禁用
    public Result<List<WinnerVO>> getWinners(@PathVariable String activityId) {
        List<WinnerVO> winners = lotteryService.getWinners(activityId);
        return Result.success(winners);
    }
    
    /**
     * 重置抽奖
     */
    @Operation(summary = "重置抽奖", description = "清空所有中奖记录，重置活动状态（仅ADMIN权限）")
    @PostMapping("/activities/{activityId}/reset")
    // @PreAuthorize("hasRole('ADMIN')")  // 开发环境暂时禁用
    public Result<Void> resetLottery(@PathVariable String activityId) {
        lotteryService.resetLottery(activityId);
        return Result.success();
    }
    
    /**
     * 撤销已结束活动（删除中奖记录，允许再次抽奖）
     */
    @Operation(summary = "撤销已结束活动", description = "将已结束的活动恢复为进行中状态，删除所有中奖记录")
    @PostMapping("/activities/{activityId}/revoke")
    public Result<Void> revokeActivity(@PathVariable String activityId) {
        lotteryService.revokeActivity(activityId);
        return Result.success();
    }
    
    // ==================== 活动管理 ====================
    
    /**
     * 获取活动列表
     */
    @Operation(summary = "获取活动列表", description = "获取当前租户的所有活动")
    @GetMapping("/activities")
    public Result<List<LotteryActivity>> getActivities() {
        List<LotteryActivity> activities = lotteryService.getActivities();
        return Result.success(activities);
    }
    
    /**
     * 获取活动详情
     */
    @Operation(summary = "获取活动详情", description = "根据ID获取活动详细信息")
    @GetMapping("/activities/{activityId}")
    public Result<LotteryActivity> getActivity(@PathVariable String activityId) {
        LotteryActivity activity = lotteryService.getActivity(activityId);
        return Result.success(activity);
    }
    
    /**
     * 创建活动
     */
    @Operation(summary = "创建活动", description = "创建新的抽奖活动")
    @PostMapping("/activities")
    public Result<LotteryActivity> createActivity(@RequestBody LotteryActivity activity) {
        LotteryActivity created = lotteryService.createActivity(activity);
        return Result.success(created);
    }
    
    /**
     * 更新活动
     */
    @Operation(summary = "更新活动", description = "更新活动信息")
    @PutMapping("/activities/{activityId}")
    public Result<LotteryActivity> updateActivity(@PathVariable String activityId, 
                                                   @RequestBody LotteryActivity activity) {
        activity.setActivityId(activityId);
        LotteryActivity updated = lotteryService.updateActivity(activity);
        return Result.success(updated);
    }
    
    // ==================== 奖项管理 ====================
    
    /**
     * 获取活动的奖项列表
     */
    @Operation(summary = "获取奖项列表", description = "获取指定活动的所有奖项")
    @GetMapping("/activities/{activityId}/prizes")
    public Result<List<Prize>> getPrizes(@PathVariable String activityId) {
        List<Prize> prizes = lotteryService.getPrizes(activityId);
        return Result.success(prizes);
    }
    
    /**
     * 创建奖项
     */
    @Operation(summary = "创建奖项", description = "为活动添加奖项")
    @PostMapping("/prizes")
    public Result<Prize> createPrize(@RequestBody Prize prize) {
        Prize created = lotteryService.createPrize(prize);
        return Result.success(created);
    }
    
    /**
     * 更新奖项
     */
    @Operation(summary = "更新奖项", description = "更新奖项信息")
    @PutMapping("/prizes/{prizeId}")
    public Result<Prize> updatePrize(@PathVariable String prizeId, 
                                     @RequestBody Prize prize) {
        prize.setPrizeId(prizeId);
        Prize updated = lotteryService.updatePrize(prize);
        return Result.success(updated);
    }
    
    /**
     * 删除奖项
     */
    @Operation(summary = "删除奖项", description = "删除指定奖项（不能删除已抽取的奖项）")
    @DeleteMapping("/prizes/{prizeId}")
    public Result<Void> deletePrize(@PathVariable String prizeId) {
        lotteryService.deletePrize(prizeId);
        return Result.success();
    }
    
    // ==================== 参会人员管理 ====================
    
    /**
     * 获取活动的参会人员列表
     */
    @Operation(summary = "获取参会人员列表", description = "获取指定活动的所有参会人员")
    @GetMapping("/activities/{activityId}/participants")
    public Result<List<Participant>> getParticipants(@PathVariable String activityId) {
        List<Participant> participants = lotteryService.getParticipants(activityId);
        return Result.success(participants);
    }
    
    /**
     * 添加参会人员
     */
    @Operation(summary = "添加参会人员", description = "为活动添加参会人员")
    @PostMapping("/participants")
    public Result<Participant> createParticipant(@RequestBody Participant participant) {
        Participant created = lotteryService.createParticipant(participant);
        return Result.success(created);
    }
    
    /**
     * 更新参会人员
     */
    @Operation(summary = "更新参会人员", description = "更新参会人员信息")
    @PutMapping("/participants/{participantId}")
    public Result<Participant> updateParticipant(@PathVariable String participantId, 
                                                  @RequestBody Participant participant) {
        participant.setParticipantId(participantId);
        Participant updated = lotteryService.updateParticipant(participant);
        return Result.success(updated);
    }
    
    /**
     * 删除参会人员
     */
    @Operation(summary = "删除参会人员", description = "删除指定参会人员（不能删除已中奖的人员）")
    @DeleteMapping("/participants/{participantId}")
    public Result<Void> deleteParticipant(@PathVariable String participantId) {
        lotteryService.deleteParticipant(participantId);
        return Result.success();
    }
}
