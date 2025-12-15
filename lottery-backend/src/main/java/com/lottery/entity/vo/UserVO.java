package com.lottery.entity.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户视图对象 VO
 */
@Data
public class UserVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户 ID
     */
    private String userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 真实姓名
     */
    private String realName;
    
    /**
     * 角色
     */
    private String role;
    
    /**
     * 状态
     */
    private String status;
    
    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginAt;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
