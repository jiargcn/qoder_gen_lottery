package com.lottery.controller;

import com.lottery.entity.dto.LoginDTO;
import com.lottery.entity.dto.RegisterDTO;
import com.lottery.entity.vo.TenantVO;
import com.lottery.entity.vo.UserVO;
import com.lottery.service.IAuthService;
import com.lottery.service.ITenantService;
import com.lottery.util.TestDataBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController 单元测试
 */
@DisplayName("认证授权接口测试")
public class AuthControllerTest extends BaseControllerTest {

    @MockBean
    private IAuthService authService;

    @MockBean
    private ITenantService tenantService;

    @Test
    @DisplayName("登录成功 - 有效用户名和密码")
    public void should_returnTokenAndUserInfo_when_loginWithValidCredentials() throws Exception {
        // 准备测试数据
        LoginDTO loginDTO = TestDataBuilder.buildLoginDTO("TEST001", "admin1", "password123");
        
        Map<String, Object> loginResult = new HashMap<>();
        loginResult.put("token", "test-jwt-token");
        loginResult.put("userInfo", new UserVO());
        
        when(authService.login(any(LoginDTO.class))).thenReturn(loginResult);

        // 执行测试
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("test-jwt-token"))
                .andExpect(jsonPath("$.data.userInfo").exists());

        // 验证
        verify(authService, times(1)).login(any(LoginDTO.class));
    }

    @Test
    @DisplayName("登录失败 - 用户名不存在")
    public void should_returnUnauthorized_when_loginWithNonExistentUsername() throws Exception {
        // 准备测试数据
        LoginDTO loginDTO = TestDataBuilder.buildLoginDTO("TEST001", "nonexistent", "password123");
        
        when(authService.login(any(LoginDTO.class)))
            .thenThrow(new com.lottery.common.exception.BizException(500, "用户名或密码错误"));

        // 执行测试
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));

        verify(authService, times(1)).login(any(LoginDTO.class));
    }

    @Test
    @DisplayName("登录失败 - 密码错误")
    public void should_returnUnauthorized_when_loginWithWrongPassword() throws Exception {
        // 准备测试数据
        LoginDTO loginDTO = TestDataBuilder.buildLoginDTO("TEST001", "admin1", "wrongpassword");
        
        when(authService.login(any(LoginDTO.class)))
            .thenThrow(new com.lottery.common.exception.BizException(500, "用户名或密码错误"));

        // 执行测试
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));

        verify(authService, times(1)).login(any(LoginDTO.class));
    }

    @Test
    @DisplayName("登录失败 - 租户不存在")
    public void should_returnNotFound_when_loginWithNonExistentTenant() throws Exception {
        // 准备测试数据
        LoginDTO loginDTO = TestDataBuilder.buildLoginDTO("NONEXISTENT", "admin1", "password123");
        
        when(authService.login(any(LoginDTO.class)))
            .thenThrow(new com.lottery.common.exception.BizException(404, "租户不存在"));

        // 执行测试
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("租户不存在"));

        verify(authService, times(1)).login(any(LoginDTO.class));
    }

    @Test
    @DisplayName("注册成功 - 完整注册信息")
    public void should_returnTenantInfo_when_registerWithValidData() throws Exception {
        // 准备测试数据
        RegisterDTO registerDTO = TestDataBuilder.buildRegisterDTO(
            "TEST003", "测试租户3", "admin", "password123");
        
        TenantVO tenantVO = new TenantVO();
        tenantVO.setTenantId("tenant-3");
        tenantVO.setTenantCode("TEST003");
        tenantVO.setTenantName("测试租户3");
        
        when(tenantService.register(any(RegisterDTO.class))).thenReturn(tenantVO);

        // 执行测试
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(registerDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.tenantCode").value("TEST003"))
                .andExpect(jsonPath("$.data.tenantName").value("测试租户3"));

        verify(tenantService, times(1)).register(any(RegisterDTO.class));
    }

    @Test
    @DisplayName("注册失败 - 租户代码重复")
    public void should_returnConflict_when_registerWithDuplicateTenantCode() throws Exception {
        // 准备测试数据
        RegisterDTO registerDTO = TestDataBuilder.buildRegisterDTO(
            "TEST001", "重复租户", "admin", "password123");
        
        when(tenantService.register(any(RegisterDTO.class)))
            .thenThrow(new com.lottery.common.exception.BizException(409, "租户代码已存在"));

        // 执行测试
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(registerDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("租户代码已存在"));

        verify(tenantService, times(1)).register(any(RegisterDTO.class));
    }

    @Test
    @DisplayName("登出成功 - 有效认证信息")
    @WithMockUser(username = "user-1-1")
    public void should_returnSuccess_when_logoutWithValidAuth() throws Exception {
        // 执行测试
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(authService, times(1)).logout(anyString());
    }

    @Test
    @DisplayName("获取当前用户信息成功")
    @WithMockUser(username = "user-1-1")
    public void should_returnUserInfo_when_getCurrentUserWithValidAuth() throws Exception {
        // 准备测试数据
        UserVO userVO = new UserVO();
        userVO.setUserId("user-1-1");
        userVO.setUsername("admin1");
        userVO.setRole("ADMIN");
        
        when(authService.getCurrentUser(anyString())).thenReturn(userVO);

        // 执行测试
        mockMvc.perform(get("/auth/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.userId").value("user-1-1"))
                .andExpect(jsonPath("$.data.username").value("admin1"))
                .andExpect(jsonPath("$.data.role").value("ADMIN"));

        verify(authService, times(1)).getCurrentUser(anyString());
    }

    @Test
    @DisplayName("刷新Token成功 - 有效的旧Token")
    public void should_returnNewToken_when_refreshWithValidToken() throws Exception {
        // 准备测试数据
        String oldToken = "old-jwt-token";
        String newToken = "new-jwt-token";
        
        when(authService.refreshToken(anyString())).thenReturn(newToken);

        // 执行测试
        mockMvc.perform(post("/auth/refresh")
                .header("Authorization", "Bearer " + oldToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value(newToken));

        verify(authService, times(1)).refreshToken(anyString());
    }

    @Test
    @DisplayName("刷新Token失败 - 无效Token")
    public void should_returnUnauthorized_when_refreshWithInvalidToken() throws Exception {
        // 准备测试数据
        String invalidToken = "invalid-token";
        
        when(authService.refreshToken(anyString()))
            .thenThrow(new com.lottery.common.exception.BizException(401, "Token无效或已过期"));

        // 执行测试
        mockMvc.perform(post("/auth/refresh")
                .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("Token无效或已过期"));

        verify(authService, times(1)).refreshToken(anyString());
    }
}
