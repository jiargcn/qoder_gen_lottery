package com.lottery.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 中奖记录保存请求 DTO
 */
@Data
public class WinnerSaveDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 活动 ID
     */
    @NotBlank(message = "活动ID不能为空")
    private String activityId;
    
    /**
     * 奖项 ID
     */
    @NotBlank(message = "奖项ID不能为空")
    private String prizeId;
    
    /**
     * 参与人员 ID
     */
    @NotBlank(message = "参与人员ID不能为空")
    private String participantId;
    
    /**
     * 抽奖时间
     */
    private LocalDateTime drawTime;
    
    /**
     * 抽奖序号
     */
    @NotNull(message = "抽奖序号不能为空")
    private Integer drawSequence;
    
    /**
     * 备注
     */
    private String remarks;
}
