package com.lottery.service;

import com.lottery.entity.dto.LoginDTO;
import com.lottery.entity.vo.UserVO;

import java.util.Map;

/**
 * 认证授权服务接口
 */
public interface IAuthService {
    
    /**
     * 用户登录
     * 
     * @param loginDTO 登录信息
     * @return Token 和用户信息
     */
    Map<String, Object> login(LoginDTO loginDTO);
    
    /**
     * 用户登出
     * 
     * @param userId 用户 ID
     */
    void logout(String userId);
    
    /**
     * 获取当前用户信息
     * 
     * @param userId 用户 ID
     * @return 用户信息
     */
    UserVO getCurrentUser(String userId);
    
    /**
     * 刷新 Token
     * 
     * @param oldToken 旧 Token
     * @return 新 Token
     */
    String refreshToken(String oldToken);
}
