package com.lottery.entity.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("winner_records")
public class WinnerRecord {
    @TableId
    private String recordId;
    private String activityId;
    private String prizeId;
    private String participantId;
    private String participantName;
    private String prizeName;
    private String giftName;
    private LocalDateTime drawTime;
    private Integer drawSequence;
    private String operatorId;
    private String operatorName;
    private String remarks;
    private LocalDateTime createdAt;
}
