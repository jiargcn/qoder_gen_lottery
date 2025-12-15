package com.lottery.entity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serializable;

/**
 * 注册请求 DTO
 */
@Data
public class RegisterDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 租户代码(用于子域名)
     */
    @NotBlank(message = "租户代码不能为空")
    @Pattern(regexp = "^[a-z0-9]{3,20}$", message = "租户代码只能包含小写字母和数字，长度3-20位")
    private String tenantCode;
    
    /**
     * 租户名称
     */
    @NotBlank(message = "租户名称不能为空")
    private String tenantName;
    
    /**
     * 管理员用户名
     */
    @NotBlank(message = "管理员用户名不能为空")
    private String adminUsername;
    
    /**
     * 管理员密码
     */
    @NotBlank(message = "管理员密码不能为空")
    private String adminPassword;
    
    /**
     * 管理员邮箱
     */
    @Email(message = "邮箱格式不正确")
    private String adminEmail;
    
    /**
     * 管理员手机号
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String adminPhone;
    
    /**
     * 管理员真实姓名
     */
    private String adminRealName;
}
