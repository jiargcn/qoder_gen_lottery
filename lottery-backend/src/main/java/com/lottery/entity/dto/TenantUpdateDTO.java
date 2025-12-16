package com.lottery.entity.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 租户更新请求 DTO
 */
@Data
public class TenantUpdateDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 租户名称
     */
    @Size(min = 1, max = 100, message = "租户名称长度必须在1-100字符之间")
    private String tenantName;
    
    /**
     * 租户代码（子域名）
     */
    @Pattern(regexp = "^[a-zA-Z0-9_]{2,50}$", message = "租户代码必须是2-50位字母、数字或下划线")
    private String tenantCode;
    
    /**
     * 订阅计划
     */
    private String subscriptionPlan;
    
    /**
     * 最大用户数限制
     */
    @Min(value = 1, message = "最大用户数必须大于等于1")
    private Integer maxUsers;
    
    /**
     * 最大活动数限制
     */
    @Min(value = 1, message = "最大活动数必须大于等于1")
    private Integer maxActivities;
    
    /**
     * 存储配额(MB)
     */
    @Min(value = 100, message = "存储配额必须大于等于100MB")
    private Integer storageQuotaMb;
    
    /**
     * 过期时间
     */
    private LocalDateTime expiredAt;
}
