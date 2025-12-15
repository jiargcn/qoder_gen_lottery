package com.lottery.entity.po;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体类 - 对应租户 schema 的 users 表
 */
@Data
@TableName("users")
public class User implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户 ID（数据库自动生成UUID）
     */
    @TableId(type = IdType.NONE)
    @TableField(insertStrategy = FieldStrategy.NEVER)
    private String userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 密码哈希(BCrypt)
     */
    private String passwordHash;
    
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
     * 角色(ADMIN/OPERATOR/VIEWER)
     */
    private String role;
    
    /**
     * 状态(ACTIVE/DISABLED)
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
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 创建人 ID
     */
    private String createdBy;
}
