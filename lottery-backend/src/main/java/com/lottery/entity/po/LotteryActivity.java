package com.lottery.entity.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("lottery_activities")
public class LotteryActivity {
    @TableId
    private String activityId;
    private String activityName;
    private String activityType;
    private String description;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer totalParticipants;
    private Integer totalWinners;
    private String configJson;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
}
