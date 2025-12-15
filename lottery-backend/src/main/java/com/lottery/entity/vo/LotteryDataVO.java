package com.lottery.entity.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 抽奖初始化数据视图对象 VO
 */
@Data
public class LotteryDataVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 活动信息
     */
    private ActivityInfo activity;
    
    /**
     * 奖项列表
     */
    private List<PrizeInfo> prizes;
    
    /**
     * 参与人员列表
     */
    private List<ParticipantInfo> participants;
    
    /**
     * 已中奖记录(按奖项分组)
     */
    private Map<String, List<WinnerInfo>> winners;
    
    /**
     * 活动信息
     */
    @Data
    public static class ActivityInfo implements Serializable {
        private String activityId;
        private String activityName;
        private String status;
        private Integer totalParticipants;
        private Integer totalWinners;
    }
    
    /**
     * 奖项信息
     */
    @Data
    public static class PrizeInfo implements Serializable {
        private String prizeId;
        private String prizeName;
        private Integer prizeLevel;
        private String giftName;
        private String giftImageUrl;
        private Integer totalQuota;
        private Integer drawnCount;
        private Integer drawOrder;
        private String status;
    }
    
    /**
     * 参与人员信息
     */
    @Data
    public static class ParticipantInfo implements Serializable {
        private String participantId;
        private String name;
        private String employeeNo;
        private String department;
        private Boolean isWinner;
    }
    
    /**
     * 中奖者信息
     */
    @Data
    public static class WinnerInfo implements Serializable {
        private String recordId;
        private String participantId;
        private String participantName;
        private String prizeName;
        private String giftName;
        private Integer drawSequence;
    }
}
