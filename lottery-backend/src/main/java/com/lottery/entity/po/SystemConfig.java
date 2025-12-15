package com.lottery.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统配置实体类 - 对应 public.system_config 表
 */
@Data
@TableName("public.system_config")
public class SystemConfig implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 配置 ID
     */
    @TableId(type = IdType.AUTO)
    private String configId;
    
    /**
     * 配置键
     */
    private String configKey;
    
    /**
     * 配置值
     */
    private String configValue;
    
    /**
     * 配置类型(STRING/JSON/NUMBER)
     */
    private String configType;
    
    /**
     * 配置描述
     */
    private String description;
    
    /**
     * 是否公开
     */
    private Boolean isPublic;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
