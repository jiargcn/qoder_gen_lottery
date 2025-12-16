package com.lottery.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serializable;

/**
 * 租户状态更新 DTO
 */
@Data
public class TenantStatusDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 状态（ACTIVE/SUSPENDED）
     */
    @NotBlank(message = "状态不能为空")
    @Pattern(regexp = "^(ACTIVE|SUSPENDED)$", message = "状态必须是ACTIVE或SUSPENDED")
    private String status;
}
