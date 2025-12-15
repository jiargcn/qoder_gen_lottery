package com.lottery.common.constant;

/**
 * 状态常量
 */
public class StatusConstant {
    
    /** 租户状态 */
    public static final String TENANT_ACTIVE = "ACTIVE";
    public static final String TENANT_SUSPENDED = "SUSPENDED";
    
    /** 用户状态 */
    public static final String USER_ACTIVE = "ACTIVE";
    public static final String USER_DISABLED = "DISABLED";
    
    /** 活动状态 */
    public static final String ACTIVITY_DRAFT = "DRAFT";
    public static final String ACTIVITY_ACTIVE = "ACTIVE";
    public static final String ACTIVITY_COMPLETED = "COMPLETED";
    public static final String ACTIVITY_ARCHIVED = "ARCHIVED";
    
    /** 奖项状态 */
    public static final String PRIZE_PENDING = "PENDING";
    public static final String PRIZE_DRAWING = "DRAWING";
    public static final String PRIZE_COMPLETED = "COMPLETED";
}
