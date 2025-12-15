package com.lottery.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 奖项创建请求 DTO
 */
@Data
public class PrizeCreateDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 活动 ID
     */
    @NotBlank(message = "活动ID不能为空")
    private String activityId;
    
    /**
     * 奖项名称
     */
    @NotBlank(message = "奖项名称不能为空")
    private String prizeName;
    
    /**
     * 奖项等级
     */
    @NotNull(message = "奖项等级不能为空")
    private Integer prizeLevel;
    
    /**
     * 奖品名称
     */
    private String giftName;
    
    /**
     * 奖品图片 URL
     */
    private String giftImageUrl;
    
    /**
     * 总名额
     */
    @NotNull(message = "总名额不能为空")
    private Integer totalQuota;
    
    /**
     * 抽奖顺序
     */
    @NotNull(message = "抽奖顺序不能为空")
    private Integer drawOrder;
}
