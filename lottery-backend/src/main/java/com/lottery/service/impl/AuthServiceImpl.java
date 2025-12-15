package com.lottery.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lottery.common.exception.BizException;
import com.lottery.common.util.BcryptUtil;
import com.lottery.common.util.JwtUtil;
import com.lottery.entity.dto.LoginDTO;
import com.lottery.entity.po.Tenant;
import com.lottery.entity.po.User;
import com.lottery.entity.vo.UserVO;
import com.lottery.mapper.TenantMapper;
import com.lottery.mapper.UserMapper;
import com.lottery.service.IAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 认证授权服务实现
 */
@Slf4j
@Service
public class AuthServiceImpl implements IAuthService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private TenantMapper tenantMapper;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private BcryptUtil bcryptUtil;
    
    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;
    
    @Override
    public Map<String, Object> login(LoginDTO loginDTO) {
        // 1. 查询租户信息
        Tenant tenant = null;
        if (loginDTO.getTenantCode() != null && !loginDTO.getTenantCode().isEmpty()) {
            LambdaQueryWrapper<Tenant> tenantQuery = new LambdaQueryWrapper<>();
            tenantQuery.eq(Tenant::getTenantCode, loginDTO.getTenantCode());
            tenant = tenantMapper.selectOne(tenantQuery);
            
            if (tenant == null) {
                throw new BizException("租户不存在");
            }
            
            if (!"ACTIVE".equals(tenant.getStatus())) {
                throw new BizException("租户已被暂停");
            }
        }
        
        // 2. 查询用户信息（需要先设置租户上下文）
        LambdaQueryWrapper<User> userQuery = new LambdaQueryWrapper<>();
        userQuery.eq(User::getUsername, loginDTO.getUsername());
        User user = userMapper.selectOne(userQuery);
        
        if (user == null) {
            throw new BizException("用户名或密码错误");
        }
        
        // 3. 验证密码
        if (!bcryptUtil.matches(loginDTO.getPassword(), user.getPasswordHash())) {
            throw new BizException("用户名或密码错误");
        }
        
        // 4. 检查用户状态
        if (!"ACTIVE".equals(user.getStatus())) {
            throw new BizException("用户已被禁用");
        }
        
        // 5. 生成 Token
        String token = jwtUtil.generateToken(
            user.getUserId(), 
            tenant != null ? tenant.getTenantId() : null, 
            user.getUsername(), 
            user.getRole()
        );
        
        // 6. 保存会话信息到 Redis
        if (redisTemplate != null) {
            String sessionKey = "session:" + token;
            Map<String, Object> sessionData = new HashMap<>();
            sessionData.put("userId", user.getUserId());
            sessionData.put("tenantId", tenant != null ? tenant.getTenantId() : null);
            sessionData.put("username", user.getUsername());
            sessionData.put("role", user.getRole());
            
            redisTemplate.opsForValue().set(sessionKey, sessionData, 2, TimeUnit.HOURS);
        }
        
        // 7. 更新最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(user);
        
        // 8. 构造返回数据
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        
        UserVO userVO = BeanUtil.copyProperties(user, UserVO.class);
        result.put("userInfo", userVO);
        
        if (tenant != null) {
            result.put("tenantCode", tenant.getTenantCode());
            result.put("tenantName", tenant.getTenantName());
        }
        
        log.info("用户登录成功: username={}, tenantCode={}", user.getUsername(), loginDTO.getTenantCode());
        
        return result;
    }
    
    @Override
    public void logout(String userId) {
        // 清除 Redis 中的会话信息
        // 这里需要根据 userId 查找对应的 token，然后删除
        // 简化实现：前端直接删除 token 即可
        log.info("用户登出: userId={}", userId);
    }
    
    @Override
    public UserVO getCurrentUser(String userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        
        return BeanUtil.copyProperties(user, UserVO.class);
    }
    
    @Override
    public String refreshToken(String oldToken) {
        if (!jwtUtil.validateToken(oldToken)) {
            throw new BizException("Token 已过期或无效");
        }
        
        return jwtUtil.refreshToken(oldToken);
    }
}
