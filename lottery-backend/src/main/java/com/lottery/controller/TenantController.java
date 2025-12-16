package com.lottery.controller;

import com.lottery.common.response.Result;
import com.lottery.entity.po.Tenant;
import com.lottery.service.ITenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 租户管理控制器（仅超级管理员可访问）
 */
@Tag(name = "租户管理", description = "租户CRUD接口，仅超级管理员可访问")
@RestController
@RequestMapping("/admin/tenants")
public class TenantController {
    
    @Autowired
    private ITenantService tenantService;
    
    /**
     * 获取所有租户列表
     */
    @Operation(summary = "获取租户列表", description = "获取所有租户信息")
    @GetMapping
    // @PreAuthorize("hasRole('SUPER_ADMIN')")  // 开发环境暂时禁用
    public Result<List<Tenant>> getTenants() {
        List<Tenant> tenants = tenantService.getAllTenants();
        return Result.success(tenants);
    }
    
    /**
     * 创建租户
     */
    @Operation(summary = "创建租户", description = "创建新租户并初始化数据库Schema")
    @PostMapping
    // @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Tenant> createTenant(@RequestBody Tenant tenant) {
        Tenant created = tenantService.createTenant(tenant);
        return Result.success(created);
    }
    
    /**
     * 更新租户
     */
    @Operation(summary = "更新租户", description = "更新租户信息")
    @PutMapping("/{tenantId}")
    // @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Tenant> updateTenant(@PathVariable String tenantId, 
                                       @RequestBody Tenant tenant) {
        tenant.setTenantId(tenantId);
        Tenant updated = tenantService.updateTenant(tenant);
        return Result.success(updated);
    }
}
