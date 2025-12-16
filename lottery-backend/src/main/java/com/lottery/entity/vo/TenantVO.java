package com.lottery.entity.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 租户信息视图对象 VO
 */
@Data
public class TenantVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 租户 ID
     */
    private String tenantId;
    
    /**
     * 租户代码
     */
    private String tenantCode;
    
    /**
     * 租户名称
     */
    private String tenantName;
    
    /**
     * 状态
     */
    private String status;
    
    /**
     * 订阅计划
     */
    private String subscriptionPlan;
    
    /**
     * 最大用户数限制
     */
    private Integer maxUsers;
    
    /**
     * 最大活动数限制
     */
    private Integer maxActivities;
    
    /**
     * 存储配额(MB)
     */
    private Integer storageQuotaMb;
    
    /**
     * 当前用户数（统计）
     */
    private Integer currentUsers;
    
    /**
     * 当前活动数（统计）
     */
    private Integer currentActivities;
    
    /**
     * 已使用存储（统计，单位MB）
     */
    private Integer storageUsedMb;
    
    /**
     * Schema 名称
     */
    private String schemaName;
    
    /**
     * 管理员用户名
     */
    private String adminUsername;
    
    /**
     * 管理员邮箱
     */
    private String adminEmail;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 过期时间
     */
    private LocalDateTime expiredAt;
}
