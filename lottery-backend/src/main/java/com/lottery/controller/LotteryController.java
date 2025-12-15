package com.lottery.controller;

import com.lottery.common.response.Result;
import com.lottery.entity.dto.WinnerSaveDTO;
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
}
