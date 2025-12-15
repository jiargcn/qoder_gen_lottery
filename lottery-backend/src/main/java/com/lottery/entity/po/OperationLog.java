package com.lottery.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 操作日志实体类 - 对应租户 schema 的 operation_logs 表
 */
@Data
@TableName(value = "operation_logs", autoResultMap = true)
public class OperationLog implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 日志 ID
     */
    @TableId(type = IdType.AUTO)
    private String logId;
    
    /**
     * 关联活动 ID
     */
    private String activityId;
    
    /**
     * 操作人 ID
     */
    private String operatorId;
    
    /**
     * 操作人姓名
     */
    private String operatorName;
    
    /**
     * 操作类型
     */
    private String operationType;
    
    /**
     * 操作描述
     */
    private String operationDesc;
    
    /**
     * 目标类型(ACTIVITY/PRIZE/PARTICIPANT)
     */
    private String targetType;
    
    /**
     * 目标 ID
     */
    private String targetId;
    
    /**
     * 请求参数(JSON)
     */
    private Map<String, Object> requestParams;
    
    /**
     * IP 地址
     */
    private String ipAddress;
    
    /**
     * User Agent
     */
    private String userAgent;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
