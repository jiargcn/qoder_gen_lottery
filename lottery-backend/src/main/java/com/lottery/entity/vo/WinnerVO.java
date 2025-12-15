package com.lottery.entity.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 中奖记录视图对象 VO
 */
@Data
public class WinnerVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 记录 ID
     */
    private String recordId;
    
    /**
     * 活动 ID
     */
    private String activityId;
    
    /**
     * 活动名称
     */
    private String activityName;
    
    /**
     * 奖项 ID
     */
    private String prizeId;
    
    /**
     * 奖项名称
     */
    private String prizeName;
    
    /**
     * 参与人员 ID
     */
    private String participantId;
    
    /**
     * 参与人员姓名
     */
    private String participantName;
    
    /**
     * 奖品名称
     */
    private String giftName;
    
    /**
     * 抽奖时间
     */
    private LocalDateTime drawTime;
    
    /**
     * 抽奖序号
     */
    private Integer drawSequence;
    
    /**
     * 操作员姓名
     */
    private String operatorName;
    
    /**
     * 备注
     */
    private String remarks;
}
