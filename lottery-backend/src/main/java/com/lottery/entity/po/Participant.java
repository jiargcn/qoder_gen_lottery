package com.lottery.entity.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("participants")
public class Participant {
    @TableId
    private String participantId;
    private String activityId;
    private String name;
    private String employeeNo;
    private String department;
    private String phone;
    private String email;
    private String extraInfo;
    private Boolean isWinner;
    private String importBatch;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
