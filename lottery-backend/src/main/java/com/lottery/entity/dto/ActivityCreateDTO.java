package com.lottery.entity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 活动创建请求 DTO
 */
@Data
public class ActivityCreateDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 活动名称
     */
    @NotBlank(message = "活动名称不能为空")
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
     * 活动开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 活动结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 活动配置(JSON格式)
     */
    private Map<String, Object> configJson;
}
