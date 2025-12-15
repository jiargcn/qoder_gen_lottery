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
import org.springframework.jdbc.core.JdbcTemplate;
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
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Override
    public Map<String, Object> login(LoginDTO loginDTO) {
        log.info("开始登录: username={}, tenantCode={}", loginDTO.getUsername(), loginDTO.getTenantCode());
        
        // 1. 查询租户信息
        Tenant tenant = null;
        if (loginDTO.getTenantCode() != null && !loginDTO.getTenantCode().isEmpty()) {
            LambdaQueryWrapper<Tenant> tenantQuery = new LambdaQueryWrapper<>();
            tenantQuery.eq(Tenant::getTenantCode, loginDTO.getTenantCode());
            tenant = tenantMapper.selectOne(tenantQuery);
            
            if (tenant == null) {
                log.warn("租户不存在: tenantCode={}", loginDTO.getTenantCode());
                throw new BizException("租户不存在");
            }
            
            if (!"ACTIVE".equals(tenant.getStatus())) {
                log.warn("租户已被暂停: tenantCode={}", loginDTO.getTenantCode());
                throw new BizException("租户已被暂停");
            }
            
            log.info("租户查询成功: tenantId={}, schemaName={}", tenant.getTenantId(), tenant.getSchemaName());
        } else {
            log.warn("登录请求缺少租户代码");
            throw new BizException("请输入租户代码");
        }
        
        // 2. 设置租户上下文，以便查询用户时切换到正确的 schema
        if (tenant != null) {
            com.lottery.common.context.TenantContext.setTenantId(tenant.getTenantId());
        }
        
        try {
            // 3. 查询用户信息（使用 JdbcTemplate 直接查询）
            String schemaName = tenant.getSchemaName();
            String selectUserSql = "SELECT user_id, username, password_hash, email, phone, real_name, role, status, " +
                "last_login_at, created_at, updated_at, created_by " +
                "FROM " + schemaName + ".users WHERE username = ?";
            
            User user = null;
            try {
                user = jdbcTemplate.queryForObject(selectUserSql, (rs, rowNum) -> {
                    User u = new User();
                    u.setUserId(rs.getString("user_id"));
                    u.setUsername(rs.getString("username"));
                    u.setPasswordHash(rs.getString("password_hash"));
                    u.setEmail(rs.getString("email"));
                    u.setPhone(rs.getString("phone"));
                    u.setRealName(rs.getString("real_name"));
                    u.setRole(rs.getString("role"));
                    u.setStatus(rs.getString("status"));
                    if (rs.getTimestamp("last_login_at") != null) {
                        u.setLastLoginAt(rs.getTimestamp("last_login_at").toLocalDateTime());
                    }
                    u.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    u.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                    u.setCreatedBy(rs.getString("created_by"));
                    return u;
                }, loginDTO.getUsername());
            } catch (Exception e) {
                // 用户不存在
                throw new BizException("用户名或密码错误");
            }
            
            // 4. 验证密码
            if (!bcryptUtil.matches(loginDTO.getPassword(), user.getPasswordHash())) {
                throw new BizException("用户名或密码错误");
            }
            
            // 5. 检查用户状态
            if (!"ACTIVE".equals(user.getStatus())) {
                throw new BizException("用户已被禁用");
            }
            
            // 6. 生成 Token
            String token = jwtUtil.generateToken(
                user.getUserId(), 
                tenant != null ? tenant.getTenantId() : null, 
                user.getUsername(), 
                user.getRole()
            );
            
            // 7. 保存会话信息到 Redis
            if (redisTemplate != null) {
                String sessionKey = "session:" + token;
                Map<String, Object> sessionData = new HashMap<>();
                sessionData.put("userId", user.getUserId());
                sessionData.put("tenantId", tenant != null ? tenant.getTenantId() : null);
                sessionData.put("username", user.getUsername());
                sessionData.put("role", user.getRole());
                
                redisTemplate.opsForValue().set(sessionKey, sessionData, 2, TimeUnit.HOURS);
            }
            
            // 8. 更新最后登录时间
            String updateUserSql = "UPDATE " + tenant.getSchemaName() + ".users " +
                "SET last_login_at = ? WHERE user_id = ?::uuid";
            jdbcTemplate.update(updateUserSql, LocalDateTime.now(), user.getUserId());
            
            // 9. 构造返回数据
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
        } finally {
            // 清除租户上下文
            com.lottery.common.context.TenantContext.clear();
        }
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
