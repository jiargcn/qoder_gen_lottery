package com.lottery.service;

import com.lottery.entity.dto.RegisterDTO;
import com.lottery.entity.vo.TenantVO;

/**
 * 租户管理服务接口
 */
public interface ITenantService {
    
    /**
     * 租户注册
     * 
     * @param registerDTO 注册信息
     * @return 租户信息
     */
    TenantVO register(RegisterDTO registerDTO);
    
    /**
     * 创建租户 Schema
     * 
     * @param tenantId 租户 ID
     * @param schemaName Schema 名称
     */
    void createTenantSchema(String tenantId, String schemaName);
    
    /**
     * 获取租户信息
     * 
     * @param tenantId 租户 ID
     * @return 租户信息
     */
    TenantVO getTenantInfo(String tenantId);
    
    /**
     * 根据租户代码获取租户信息
     * 
     * @param tenantCode 租户代码
     * @return 租户信息
     */
    TenantVO getTenantByCode(String tenantCode);
}
