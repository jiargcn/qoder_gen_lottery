package com.lottery.entity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户信息更新 DTO
 */
@Data
public class UserProfileUpdateDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    private String email;
    
    /**
     * 手机号（11位数字）
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    /**
     * 真实姓名
     */
    @Size(min = 1, max = 50, message = "真实姓名长度必须在1-50字符之间")
    private String realName;
}
