package com.lottery.common.context;

/**
 * 租户上下文 - 使用 ThreadLocal 存储当前请求的租户信息
 * 核心作用：在整个请求处理过程中传递租户标识
 */
public class TenantContext {
    
    private static final ThreadLocal<String> TENANT_ID = new InheritableThreadLocal<>();
    
    /**
     * 设置当前租户ID
     */
    public static void setTenantId(String tenantId) {
        TENANT_ID.set(tenantId);
    }
    
    /**
     * 获取当前租户ID
     */
    public static String getTenantId() {
        return TENANT_ID.get();
    }
    
    /**
     * 清除租户ID（请求结束时调用）
     */
    public static void clear() {
        TENANT_ID.remove();
    }
}
