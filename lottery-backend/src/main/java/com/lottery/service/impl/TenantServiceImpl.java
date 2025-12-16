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
import java.util.List;

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
        
        // 2. 生成 Schema 名称（使用 tenantCode）
        String schemaName = "tenant_" + registerDTO.getTenantCode();
        
        // 3. 插入租户记录（使用 JDBC 直接插入，让数据库生成 UUID）
        String insertTenantSql = "INSERT INTO public.tenant_registry " +
            "(tenant_code, tenant_name, schema_name, status, max_users, max_activities, storage_quota_mb) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING tenant_id";
        
        String tenantId = jdbcTemplate.queryForObject(insertTenantSql, String.class,
            registerDTO.getTenantCode(),
            registerDTO.getTenantName(),
            schemaName,
            "ACTIVE",
            10,
            100,
            1024
        );
        
        log.info("租户创建成功: tenantId={}, tenantCode={}", tenantId, registerDTO.getTenantCode());
        
        // 4. 创建租户 Schema
        createTenantSchema(tenantId, schemaName);
        
        // 5. 在租户 Schema 中创建管理员用户
        // 设置租户上下文，以便在正确的 schema 中插入用户
        com.lottery.common.context.TenantContext.setTenantId(tenantId);
        
        String adminUserId = null;
        try {
            // 使用 JDBC 直接插入用户，让数据库生成 UUID
            String insertUserSql = "INSERT INTO " + schemaName + ".users " +
                "(username, password_hash, email, phone, real_name, role, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING user_id";
            
            adminUserId = jdbcTemplate.queryForObject(insertUserSql, String.class,
                registerDTO.getAdminUsername(),
                bcryptUtil.encode(registerDTO.getAdminPassword()),
                registerDTO.getAdminEmail(),
                registerDTO.getAdminPhone(),
                registerDTO.getAdminRealName(),
                "ADMIN",
                "ACTIVE"
            );
            
            log.info("管理员用户创建成功: userId={}, username={}", adminUserId, registerDTO.getAdminUsername());
        } finally {
            // 清除租户上下文
            com.lottery.common.context.TenantContext.clear();
        }
        
        // 6. 更新租户表的 admin_user_id
        if (adminUserId != null) {
            String updateTenantSql = "UPDATE public.tenant_registry SET admin_user_id = ?::uuid WHERE tenant_id = ?::uuid";
            jdbcTemplate.update(updateTenantSql, adminUserId, tenantId);
        }
        
        // 7. 返回租户信息
        String selectTenantSql = "SELECT tenant_id, tenant_code, tenant_name, schema_name, admin_user_id, " +
            "status, subscription_plan, max_users, max_activities, storage_quota_mb, " +
            "created_at, updated_at, expired_at " +
            "FROM public.tenant_registry WHERE tenant_id = ?::uuid";
        
        Tenant tenant = jdbcTemplate.queryForObject(selectTenantSql, (rs, rowNum) -> {
            Tenant t = new Tenant();
            t.setTenantId(rs.getString("tenant_id"));
            t.setTenantCode(rs.getString("tenant_code"));
            t.setTenantName(rs.getString("tenant_name"));
            t.setSchemaName(rs.getString("schema_name"));
            t.setAdminUserId(rs.getString("admin_user_id"));
            t.setStatus(rs.getString("status"));
            t.setSubscriptionPlan(rs.getString("subscription_plan"));
            t.setMaxUsers(rs.getInt("max_users"));
            t.setMaxActivities(rs.getInt("max_activities"));
            t.setStorageQuotaMb(rs.getInt("storage_quota_mb"));
            t.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            t.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            if (rs.getTimestamp("expired_at") != null) {
                t.setExpiredAt(rs.getTimestamp("expired_at").toLocalDateTime());
            }
            return t;
        }, tenantId);
        
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
    
    // ==================== 租户管理 ====================
    
    @Override
    public List<Tenant> getAllTenants() {
        return tenantMapper.selectList(null);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Tenant createTenant(Tenant tenant) {
        // 生成租户ID和SchemaName
        String tenantId = IdUtil.fastSimpleUUID();
        String schemaName = "tenant_" + tenant.getTenantName().toLowerCase().replaceAll("\\s+", "_");
        
        tenant.setTenantId(tenantId);
        tenant.setSchemaName(schemaName);
        tenant.setTenantCode(tenant.getTenantName()); // 默认使用名称作为代码
        tenant.setStatus("ACTIVE");
        tenant.setCreatedAt(LocalDateTime.now());
        tenant.setUpdatedAt(LocalDateTime.now());
        
        tenantMapper.insert(tenant);
        
        // 创建租户Schema
        createTenantSchema(tenantId, schemaName);
        
        log.info("创建租户成功: tenantId={}, tenantName={}", tenantId, tenant.getTenantName());
        return tenant;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Tenant updateTenant(Tenant tenant) {
        Tenant existing = tenantMapper.selectById(tenant.getTenantId());
        if (existing == null) {
            throw new BizException("租户不存在");
        }
        
        tenant.setUpdatedAt(LocalDateTime.now());
        tenantMapper.updateById(tenant);
        
        log.info("更新租户成功: tenantId={}", tenant.getTenantId());
        return tenant;
    }
}
