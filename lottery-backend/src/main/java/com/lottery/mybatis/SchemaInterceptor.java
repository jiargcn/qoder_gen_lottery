package com.lottery.mybatis;

import com.lottery.common.context.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * MyBatis Schema 动态切换拦截器
 * 核心功能：根据租户上下文动态切换数据库 Schema
 */
@Slf4j
@Component
@Intercepts({
    @Signature(
        type = Executor.class,
        method = "update",
        args = {MappedStatement.class, Object.class}
    ),
    @Signature(
        type = Executor.class,
        method = "query",
        args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}
    )
})
public class SchemaInterceptor implements Interceptor {
    
    private static final String PUBLIC_SCHEMA = "public";
    private static final String SCHEMA_PREFIX = "tenant_";
    
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 获取当前租户 ID
        String tenantId = TenantContext.getTenantId();
        
        // 如果没有租户上下文，或租户ID无效，使用 public schema
        if (tenantId == null || tenantId.isEmpty() || "undefined".equals(tenantId) || "null".equals(tenantId)) {
            log.debug("租户上下文为空或无效，使用 public schema: tenantId={}", tenantId);
            return invocation.proceed();
        }
        
        // 获取数据库连接
        Executor executor = (Executor) invocation.getTarget();
        Connection connection = executor.getTransaction().getConnection();
        
        try {
            // 查询租户的 schema 名称
            String schemaName = getSchemaName(tenantId, connection);
            
            if (schemaName != null) {
                // 设置 search_path
                setSearchPath(connection, schemaName);
                log.debug("切换到租户 Schema: {}", schemaName);
            }
            
            // 执行原始SQL
            return invocation.proceed();
            
        } finally {
            // 执行完成后重置 search_path (可选)
            // resetSearchPath(connection);
        }
    }
    
    /**
     * 获取租户的 Schema 名称
     */
    private String getSchemaName(String tenantId, Connection connection) {
        try {
            // 使用 JDBC 直接查询租户信息，避免循环依赖
            String sql = "SELECT schema_name FROM public.tenant_registry WHERE tenant_id = ?::uuid";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, tenantId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("schema_name");
                    }
                }
            }
            
            // 如果查询不到，使用默认命名规则
            log.warn("未找到租户 Schema，使用默认命名: tenantId={}", tenantId);
            return SCHEMA_PREFIX + tenantId.replace("-", "_");
            
        } catch (Exception e) {
            log.error("查询租户 Schema 失败: {}", e.getMessage());
            return SCHEMA_PREFIX + tenantId.replace("-", "_");
        }
    }
    
    /**
     * 设置 PostgreSQL search_path
     */
    private void setSearchPath(Connection connection, String schemaName) throws SQLException {
        String sql = "SET search_path TO " + schemaName + ", public";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.execute();
        }
    }
    
    /**
     * 重置 search_path 到 public
     */
    private void resetSearchPath(Connection connection) throws SQLException {
        String sql = "SET search_path TO public";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.execute();
        }
    }
    
    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }
}
