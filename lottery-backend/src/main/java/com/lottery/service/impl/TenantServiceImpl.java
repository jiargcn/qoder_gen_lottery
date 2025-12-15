package com.lottery.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lottery.common.exception.BizException;
import com.lottery.common.util.BcryptUtil;
import com.lottery.entity.dto.RegisterDTO;
import com.lottery.entity.po.Tenant;
import com.lottery.entity.po.User;
import com.lottery.entity.vo.TenantVO;
import com.lottery.mapper.TenantMapper;
import com.lottery.mapper.UserMapper;
import com.lottery.service.ITenantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * 租户管理服务实现
 */
@Slf4j
@Service
public class TenantServiceImpl implements ITenantService {
    
    @Autowired
    private TenantMapper tenantMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private BcryptUtil bcryptUtil;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TenantVO register(RegisterDTO registerDTO) {
        // 1. 检查租户代码是否已存在
        LambdaQueryWrapper<Tenant> query = new LambdaQueryWrapper<>();
        query.eq(Tenant::getTenantCode, registerDTO.getTenantCode());
        Long count = tenantMapper.selectCount(query);
        
        if (count > 0) {
            throw new BizException("租户代码已存在");
        }
        
        // 2. 生成租户 ID 和 Schema 名称
        String tenantId = IdUtil.simpleUUID();
        String schemaName = "tenant_" + tenantId.replace("-", "_");
        
        // 3. 创建管理员用户 ID
        String adminUserId = IdUtil.simpleUUID();
        
        // 4. 插入租户记录
        Tenant tenant = new Tenant();
        tenant.setTenantId(tenantId);
        tenant.setTenantCode(registerDTO.getTenantCode());
        tenant.setTenantName(registerDTO.getTenantName());
        tenant.setSchemaName(schemaName);
        tenant.setAdminUserId(adminUserId);
        tenant.setStatus("ACTIVE");
        tenant.setMaxUsers(10);
        tenant.setMaxActivities(100);
        tenant.setStorageQuotaMb(1024);
        tenant.setCreatedAt(LocalDateTime.now());
        tenant.setUpdatedAt(LocalDateTime.now());
        
        tenantMapper.insert(tenant);
        
        log.info("租户创建成功: tenantId={}, tenantCode={}", tenantId, registerDTO.getTenantCode());
        
        // 5. 创建租户 Schema
        createTenantSchema(tenantId, schemaName);
        
        // 6. 在租户 Schema 中创建管理员用户
        User admin = new User();
        admin.setUserId(adminUserId);
        admin.setUsername(registerDTO.getAdminUsername());
        admin.setPasswordHash(bcryptUtil.encode(registerDTO.getAdminPassword()));
        admin.setEmail(registerDTO.getAdminEmail());
        admin.setPhone(registerDTO.getAdminPhone());
        admin.setRealName(registerDTO.getAdminRealName());
        admin.setRole("ADMIN");
        admin.setStatus("ACTIVE");
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());
        
        // 注意：这里需要在租户 Schema 中插入用户，需要设置租户上下文
        userMapper.insert(admin);
        
        log.info("管理员用户创建成功: userId={}, username={}", adminUserId, registerDTO.getAdminUsername());
        
        // 7. 返回租户信息
        return BeanUtil.copyProperties(tenant, TenantVO.class);
    }
    
    @Override
    public void createTenantSchema(String tenantId, String schemaName) {
        try {
            log.info("开始创建租户 Schema: schemaName={}", schemaName);
            
            // 1. 创建 Schema
            String createSchemaSql = "CREATE SCHEMA IF NOT EXISTS " + schemaName;
            jdbcTemplate.execute(createSchemaSql);
            
            log.info("Schema 创建成功: {}", schemaName);
            
            // 2. 设置 search_path
            String setSearchPathSql = "SET search_path TO " + schemaName + ", public";
            jdbcTemplate.execute(setSearchPathSql);
            
            // 3. 读取并执行初始化脚本
            ClassPathResource resource = new ClassPathResource("db/schema/init_tenant_schema_template.sql");
            try (InputStream inputStream = resource.getInputStream()) {
                String initScript = IoUtil.read(inputStream, StandardCharsets.UTF_8);
                
                // 执行初始化脚本
                jdbcTemplate.execute(initScript);
                
                log.info("租户 Schema 初始化成功: {}", schemaName);
            }
            
            // 4. 重置 search_path
            jdbcTemplate.execute("SET search_path TO public");
            
        } catch (Exception e) {
            log.error("创建租户 Schema 失败: schemaName={}", schemaName, e);
            throw new BizException("创建租户失败: " + e.getMessage());
        }
    }
    
    @Override
    public TenantVO getTenantInfo(String tenantId) {
        Tenant tenant = tenantMapper.selectById(tenantId);
        if (tenant == null) {
            throw new BizException("租户不存在");
        }
        
        return BeanUtil.copyProperties(tenant, TenantVO.class);
    }
    
    @Override
    public TenantVO getTenantByCode(String tenantCode) {
        LambdaQueryWrapper<Tenant> query = new LambdaQueryWrapper<>();
        query.eq(Tenant::getTenantCode, tenantCode);
        Tenant tenant = tenantMapper.selectOne(query);
        
        if (tenant == null) {
            throw new BizException("租户不存在");
        }
        
        return BeanUtil.copyProperties(tenant, TenantVO.class);
    }
}
