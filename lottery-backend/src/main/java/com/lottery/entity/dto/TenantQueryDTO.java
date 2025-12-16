package com.lottery.entity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 租户查询请求 DTO
 */
@Data
public class TenantQueryDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 页码，默认 1
     */
    private Integer page = 1;
    
    /**
     * 每页数量，默认 20
     */
    private Integer pageSize = 20;
    
    /**
     * 状态过滤（ACTIVE/SUSPENDED）
     */
    private String status;
    
    /**
     * 搜索关键字（租户代码或名称）
     */
    private String keyword;
}
