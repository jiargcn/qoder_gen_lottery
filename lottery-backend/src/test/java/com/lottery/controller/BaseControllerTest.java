package com.lottery.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lottery.common.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Controller 测试基类
 * 提供 MockMvc 配置和通用工具方法
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public abstract class BaseControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JwtUtil jwtUtil;

    @MockBean
    protected RedisTemplate<String, Object> redisTemplate;

    /**
     * 生成测试用的 JWT Token
     *
     * @param userId 用户 ID
     * @param tenantId 租户 ID
     * @param username 用户名
     * @param role 角色
     * @return JWT Token
     */
    protected String generateToken(String userId, String tenantId, String username, String role) {
        return jwtUtil.generateToken(userId, tenantId, username, role);
    }

    /**
     * 将对象转换为 JSON 字符串
     *
     * @param obj 对象
     * @return JSON 字符串
     */
    protected String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    @BeforeEach
    public void setUp() {
        // 子类可以重写此方法进行额外的初始化
    }
}
