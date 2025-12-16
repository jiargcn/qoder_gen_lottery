package com.lottery.controller;

import com.lottery.entity.po.Tenant;
import com.lottery.service.ITenantService;
import com.lottery.util.TestDataBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TenantController 单元测试
 */
@DisplayName("租户管理接口测试")
@WithMockUser(username = "admin", roles = "SUPER_ADMIN")
public class TenantControllerTest extends BaseControllerTest {

    @MockBean
    private ITenantService tenantService;

    @Test
    @DisplayName("获取租户列表成功 - 超级管理员认证")
    public void should_returnTenantList_when_getTenantsWithSuperAdminAuth() throws Exception {
        // 准备测试数据
        List<Tenant> tenants = Arrays.asList(
            TestDataBuilder.buildTenant("tenant-1", "TEST001", "测试租户1"),
            TestDataBuilder.buildTenant("tenant-2", "TEST002", "测试租户2")
        );
        
        when(tenantService.getAllTenants()).thenReturn(tenants);

        // 执行测试
        mockMvc.perform(get("/admin/tenants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].tenantCode").value("TEST001"))
                .andExpect(jsonPath("$.data[1].tenantCode").value("TEST002"));

        verify(tenantService, times(1)).getAllTenants();
    }

    @Test
    @DisplayName("获取租户列表 - 租户为空")
    public void should_returnEmptyList_when_getTenantsWithNoTenants() throws Exception {
        // 准备测试数据
        when(tenantService.getAllTenants()).thenReturn(Arrays.asList());

        // 执行测试
        mockMvc.perform(get("/admin/tenants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());

        verify(tenantService, times(1)).getAllTenants();
    }

    @Test
    @DisplayName("创建租户成功 - 完整租户信息")
    public void should_returnCreatedTenant_when_createTenantWithValidData() throws Exception {
        // 准备测试数据
        Tenant tenant = TestDataBuilder.buildTenant("tenant-new", "TEST999", "新测试租户");
        Tenant created = TestDataBuilder.buildTenant("tenant-generated", "TEST999", "新测试租户");
        
        when(tenantService.createTenant(any(Tenant.class))).thenReturn(created);

        // 执行测试
        mockMvc.perform(post("/admin/tenants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(tenant)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.tenantCode").value("TEST999"))
                .andExpect(jsonPath("$.data.tenantName").value("新测试租户"));

        verify(tenantService, times(1)).createTenant(any(Tenant.class));
    }

    @Test
    @DisplayName("创建租户失败 - 租户代码重复")
    public void should_returnConflict_when_createTenantWithDuplicateCode() throws Exception {
        // 准备测试数据
        Tenant tenant = TestDataBuilder.buildTenant("tenant-new", "TEST001", "重复租户");
        
        when(tenantService.createTenant(any(Tenant.class)))
            .thenThrow(new com.lottery.common.exception.BizException(409, "租户代码已存在"));

        // 执行测试
        mockMvc.perform(post("/admin/tenants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(tenant)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("租户代码已存在"));

        verify(tenantService, times(1)).createTenant(any(Tenant.class));
    }

    @Test
    @DisplayName("创建租户失败 - Schema创建失败")
    public void should_returnError_when_createTenantWithSchemaCreationFailure() throws Exception {
        // 准备测试数据
        Tenant tenant = TestDataBuilder.buildTenant("tenant-new", "TEST998", "测试租户");
        
        when(tenantService.createTenant(any(Tenant.class)))
            .thenThrow(new com.lottery.common.exception.BizException(500, "Schema 创建失败"));

        // 执行测试
        mockMvc.perform(post("/admin/tenants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(tenant)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("Schema 创建失败"));

        verify(tenantService, times(1)).createTenant(any(Tenant.class));
    }

    @Test
    @DisplayName("更新租户成功 - 有效的租户信息")
    public void should_returnUpdatedTenant_when_updateTenantWithValidData() throws Exception {
        // 准备测试数据
        String tenantId = "test-tenant-1";
        Tenant tenant = TestDataBuilder.buildTenant(tenantId, "TEST001", "更新的租户名称");
        
        when(tenantService.updateTenant(any(Tenant.class))).thenReturn(tenant);

        // 执行测试
        mockMvc.perform(put("/admin/tenants/{tenantId}", tenantId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(tenant)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.tenantName").value("更新的租户名称"));

        verify(tenantService, times(1)).updateTenant(any(Tenant.class));
    }

    @Test
    @DisplayName("更新租户失败 - 租户不存在")
    public void should_returnNotFound_when_updateTenantWithNonExistentId() throws Exception {
        // 准备测试数据
        String tenantId = "non-existent";
        Tenant tenant = TestDataBuilder.buildTenant(tenantId, "TEST999", "测试");
        
        when(tenantService.updateTenant(any(Tenant.class)))
            .thenThrow(new com.lottery.common.exception.BizException(404, "租户不存在"));

        // 执行测试
        mockMvc.perform(put("/admin/tenants/{tenantId}", tenantId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(tenant)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("租户不存在"));

        verify(tenantService, times(1)).updateTenant(any(Tenant.class));
    }

    @Test
    @DisplayName("更新租户失败 - 租户代码重复")
    public void should_returnConflict_when_updateTenantWithDuplicateCode() throws Exception {
        // 准备测试数据
        String tenantId = "test-tenant-1";
        Tenant tenant = TestDataBuilder.buildTenant(tenantId, "TEST002", "测试租户");
        
        when(tenantService.updateTenant(any(Tenant.class)))
            .thenThrow(new com.lottery.common.exception.BizException(409, "租户代码已存在"));

        // 执行测试
        mockMvc.perform(put("/admin/tenants/{tenantId}", tenantId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(tenant)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("租户代码已存在"));

        verify(tenantService, times(1)).updateTenant(any(Tenant.class));
    }
}
