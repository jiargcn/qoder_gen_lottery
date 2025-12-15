package com.lottery.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 租户实体类 - 对应 public.tenant_registry 表
 */
@Data
@TableName("public.tenant_registry")
public class Tenant implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 租户唯一标识
     */
    @TableId(type = IdType.AUTO)
    private String tenantId;
    
    /**
     * 租户代码(子域名)
     */
    private String tenantCode;
    
    /**
     * 租户名称
     */
    private String tenantName;
    
    /**
     * Schema 名称
     */
    private String schemaName;
    
    /**
     * 管理员用户 ID
     */
    private String adminUserId;
    
    /**
     * 状态(ACTIVE/SUSPENDED)
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
