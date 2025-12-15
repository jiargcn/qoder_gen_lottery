package com.lottery.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 参与人员批量导入请求 DTO
 */
@Data
public class ParticipantImportDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 活动 ID
     */
    @NotBlank(message = "活动ID不能为空")
    private String activityId;
    
    /**
     * 导入批次号
     */
    private String importBatch;
    
    /**
     * 参与人员列表
     */
    @NotEmpty(message = "参与人员列表不能为空")
    private List<ParticipantItem> participants;
    
    /**
     * 参与人员明细
     */
    @Data
    public static class ParticipantItem implements Serializable {
        
        /**
         * 姓名
         */
        @NotBlank(message = "姓名不能为空")
        private String name;
        
        /**
         * 工号
         */
        private String employeeNo;
        
        /**
         * 部门
         */
        private String department;
        
        /**
         * 电话
         */
        private String phone;
        
        /**
         * 邮箱
         */
        private String email;
        
        /**
         * 扩展信息
         */
        private Map<String, Object> extraInfo;
    }
}
