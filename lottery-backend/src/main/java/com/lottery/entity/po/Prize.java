package com.lottery.entity.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("prizes")
public class Prize {
    @TableId
    private String prizeId;
    private String activityId;
    private String prizeName;
    private Integer prizeLevel;
    private String giftName;
    private String giftImageUrl;
    private Integer totalQuota;
    private Integer drawnCount;
    private Integer drawOrder;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
