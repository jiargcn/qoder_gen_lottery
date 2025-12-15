package com.lottery.controller;

import com.lottery.common.response.Result;
import com.lottery.entity.dto.LoginDTO;
import com.lottery.entity.dto.RegisterDTO;
import com.lottery.entity.vo.TenantVO;
import com.lottery.entity.vo.UserVO;
import com.lottery.service.IAuthService;
import com.lottery.service.ITenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证授权控制器
 */
@Tag(name = "认证授权", description = "用户登录、注册、Token刷新等接口")
@RestController
@RequestMapping("/auth")
public class AuthController {
    
    @Autowired
    private IAuthService authService;
    
    @Autowired
    private ITenantService tenantService;
    
    /**
     * 用户登录
     */
    @Operation(summary = "用户登录", description = "用户名密码登录，返回 JWT Token")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO loginDTO) {
        Map<String, Object> loginResult = authService.login(loginDTO);
        return Result.success(loginResult);
    }
    
    /**
     * 租户注册
     */
    @Operation(summary = "租户注册", description = "注册新租户，自动创建Schema和管理员账号")
    @PostMapping("/register")
    public Result<TenantVO> register(@Valid @RequestBody RegisterDTO registerDTO) {
        TenantVO tenantVO = tenantService.register(registerDTO);
        return Result.success(tenantVO);
    }
    
    /**
     * 用户登出
     */
    @Operation(summary = "用户登出", description = "退出登录，清除会话信息")
    @PostMapping("/logout")
    public Result<Void> logout(Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        authService.logout(userId);
        return Result.success();
    }
    
    /**
     * 获取当前用户信息
     */
    @Operation(summary = "获取当前用户信息", description = "获取已登录用户的详细信息")
    @GetMapping("/current")
    public Result<UserVO> getCurrentUser(Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        UserVO userVO = authService.getCurrentUser(userId);
        return Result.success(userVO);
    }
    
    /**
     * 刷新 Token
     */
    @Operation(summary = "刷新Token", description = "使用旧Token刷新获取新Token")
    @PostMapping("/refresh")
    public Result<Map<String, String>> refreshToken(@RequestHeader("Authorization") String authorization) {
        String oldToken = authorization.replace("Bearer ", "");
        String newToken = authService.refreshToken(oldToken);
        
        Map<String, String> result = Map.of("token", newToken);
        return Result.success(result);
    }
}
