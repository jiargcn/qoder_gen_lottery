package com.lottery.entity.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 活动视图对象 VO
 */
@Data
public class ActivityVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 活动 ID
     */
    private String activityId;
    
    /**
     * 活动名称
     */
    private String activityName;
    
    /**
     * 活动类型
     */
    private String activityType;
    
    /**
     * 活动描述
     */
    private String description;
    
    /**
     * 状态
     */
    private String status;
    
    /**
     * 活动开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 活动结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 参与总人数
     */
    private Integer totalParticipants;
    
    /**
     * 中奖总人数
     */
    private Integer totalWinners;
    
    /**
     * 活动配置
     */
    private Map<String, Object> configJson;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 创建人 ID
     */
    private String createdBy;
}
