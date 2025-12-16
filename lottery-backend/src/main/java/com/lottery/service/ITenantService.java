package com.lottery.service;

import com.lottery.entity.dto.RegisterDTO;
import com.lottery.entity.po.Tenant;
import com.lottery.entity.vo.TenantVO;

import java.util.List;

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
    
    // ==================== 租户管理 ====================
    
    /**
     * 获取所有租户列表
     * 
     * @return 租户列表
     */
    List<Tenant> getAllTenants();
    
    /**
     * 创建租户
     * 
     * @param tenant 租户信息
     * @return 创建的租户
     */
    Tenant createTenant(Tenant tenant);
    
    /**
     * 更新租户
     * 
     * @param tenant 租户信息
     * @return 更新后的租户
     */
    Tenant updateTenant(Tenant tenant);
}
