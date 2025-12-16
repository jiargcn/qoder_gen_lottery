package com.lottery.util;

import com.lottery.entity.dto.LoginDTO;
import com.lottery.entity.dto.RegisterDTO;
import com.lottery.entity.dto.WinnerSaveDTO;
import com.lottery.entity.po.*;

import java.time.LocalDateTime;

/**
 * 测试数据构建器
 * 用于快速构建测试数据对象
 */
public class TestDataBuilder {

    /**
     * 构建登录DTO
     */
    public static LoginDTO buildLoginDTO(String tenantCode, String username, String password) {
        LoginDTO dto = new LoginDTO();
        dto.setTenantCode(tenantCode);
        dto.setUsername(username);
        dto.setPassword(password);
        return dto;
    }

    /**
     * 构建注册DTO
     */
    public static RegisterDTO buildRegisterDTO(String tenantCode, String tenantName, 
                                               String adminUsername, String adminPassword) {
        RegisterDTO dto = new RegisterDTO();
        dto.setTenantCode(tenantCode);
        dto.setTenantName(tenantName);
        dto.setAdminUsername(adminUsername);
        dto.setAdminPassword(adminPassword);
        return dto;
    }

    /**
     * 构建中奖保存DTO
     */
    public static WinnerSaveDTO buildWinnerSaveDTO(String activityId, String prizeId, 
                                                    String participantId) {
        WinnerSaveDTO dto = new WinnerSaveDTO();
        dto.setActivityId(activityId);
        dto.setPrizeId(prizeId);
        dto.setParticipantId(participantId);
        return dto;
    }

    /**
     * 构建租户对象
     */
    public static Tenant buildTenant(String tenantId, String tenantCode, String tenantName) {
        Tenant tenant = new Tenant();
        tenant.setTenantId(tenantId);
        tenant.setTenantCode(tenantCode);
        tenant.setTenantName(tenantName);
        tenant.setSchemaName("tenant_" + tenantCode.toLowerCase());
        tenant.setStatus("ACTIVE");
        return tenant;
    }

    /**
     * 构建活动对象
     */
    public static LotteryActivity buildActivity(String activityId, String activityName) {
        LotteryActivity activity = new LotteryActivity();
        activity.setActivityId(activityId);
        activity.setActivityName(activityName);
        activity.setStatus("ACTIVE");
        return activity;
    }

    /**
     * 构建奖项对象
     */
    public static Prize buildPrize(String prizeId, String activityId, String prizeName, 
                                   Integer prizeLevel, Integer totalQuota) {
        Prize prize = new Prize();
        prize.setPrizeId(prizeId);
        prize.setActivityId(activityId);
        prize.setPrizeName(prizeName);
        prize.setPrizeLevel(prizeLevel);
        prize.setTotalQuota(totalQuota);
        prize.setDrawnCount(0);
        prize.setStatus("ACTIVE");
        return prize;
    }

    /**
     * 构建参会人员对象
     */
    public static Participant buildParticipant(String participantId, String activityId, 
                                              String name, String department) {
        Participant participant = new Participant();
        participant.setParticipantId(participantId);
        participant.setActivityId(activityId);
        participant.setName(name);
        participant.setDepartment(department);
        participant.setIsWinner(false);
        return participant;
    }

    /**
     * 构建用户对象
     */
    public static User buildUser(String userId, String username, 
                                 String passwordHash, String role) {
        User user = new User();
        user.setUserId(userId);
        user.setUsername(username);
        user.setPasswordHash(passwordHash);
        user.setRole(role);
        user.setStatus("ACTIVE");
        return user;
    }
}
