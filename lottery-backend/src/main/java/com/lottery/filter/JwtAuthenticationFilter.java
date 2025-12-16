package com.lottery.filter;

import com.lottery.common.context.TenantContext;
import com.lottery.common.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT 认证过滤器
 * 拦截请求，验证 JWT Token，设置认证信息
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // 从请求头中获取 Token
            String token = extractToken(request);
            
            if (token != null && jwtUtil.validateToken(token)) {
                // 解析 Token
                String userId = jwtUtil.getUserIdFromToken(token);
                String tenantId = jwtUtil.getTenantIdFromToken(token);
                String username = jwtUtil.getUsernameFromToken(token);
                String role = jwtUtil.getRoleFromToken(token);
                
                log.debug("解析JWT Token: userId={}, tenantId={}, username={}, role={}, requestURI={}", 
                         userId, tenantId, username, role, request.getRequestURI());
                
                // 设置租户上下文
                if (tenantId != null) {
                    TenantContext.setTenantId(tenantId);
                    log.debug("设置租户上下文: tenantId={}", tenantId);
                }
                
                // 创建认证对象
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        userId, 
                        null, 
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                    );
                
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // 设置到 Security 上下文
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                log.debug("JWT 认证成功: userId={}, tenantId={}, role={}", userId, tenantId, role);
            }
            
        } catch (Exception e) {
            log.error("JWT 认证失败: {}", e.getMessage());
        }
        
        try {
            filterChain.doFilter(request, response);
        } finally {
            // 请求处理完成后，清理租户上下文，避免线程复用时出现脏数据
            String clearedTenantId = TenantContext.getTenantId();
            TenantContext.clear();
            log.debug("清理租户上下文: tenantId={}, requestURI={}", clearedTenantId, request.getRequestURI());
        }
    }
    
    /**
     * 从请求头中提取 Token
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        
        return null;
    }
}
