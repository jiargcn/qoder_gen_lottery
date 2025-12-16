package com.lottery.controller;

import com.lottery.entity.dto.WinnerSaveDTO;
import com.lottery.entity.po.LotteryActivity;
import com.lottery.entity.po.Participant;
import com.lottery.entity.po.Prize;
import com.lottery.entity.vo.LotteryDataVO;
import com.lottery.entity.vo.WinnerVO;
import com.lottery.service.ILotteryService;
import com.lottery.util.TestDataBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * LotteryController 单元测试
 */
@DisplayName("抽奖管理接口测试")
@WithMockUser(username = "user-1-1")
public class LotteryControllerTest extends BaseControllerTest {

    @MockBean
    private ILotteryService lotteryService;

    // ==================== 抽奖数据相关测试 ====================

    @Test
    @DisplayName("获取抽奖初始化数据成功 - 存在的活动ID")
    public void should_returnLotteryData_when_getDataWithValidActivityId() throws Exception {
        // 准备测试数据
        String activityId = "activity-1-1";
        LotteryDataVO dataVO = new LotteryDataVO();
        
        LotteryDataVO.ActivityInfo activityInfo = new LotteryDataVO.ActivityInfo();
        activityInfo.setActivityId(activityId);
        activityInfo.setActivityName("春节年会");
        dataVO.setActivity(activityInfo);
        
        dataVO.setPrizes(new ArrayList<>());
        dataVO.setParticipants(new ArrayList<>());
        dataVO.setWinners(new java.util.HashMap<>());
        
        when(lotteryService.getLotteryData(activityId)).thenReturn(dataVO);

        // 执行测试
        mockMvc.perform(get("/lottery/activities/{activityId}/data", activityId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.activity").exists())
                .andExpect(jsonPath("$.data.prizes").isArray())
                .andExpect(jsonPath("$.data.participants").isArray())
                .andExpect(jsonPath("$.data.winners").exists());

        verify(lotteryService, times(1)).getLotteryData(activityId);
    }

    @Test
    @DisplayName("获取抽奖初始化数据失败 - 活动不存在")
    public void should_returnNotFound_when_getDataWithNonExistentActivityId() throws Exception {
        // 准备测试数据
        String activityId = "non-existent";
        
        when(lotteryService.getLotteryData(activityId))
            .thenThrow(new com.lottery.common.exception.BizException(404, "活动不存在"));

        // 执行测试
        mockMvc.perform(get("/lottery/activities/{activityId}/data", activityId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("活动不存在"));

        verify(lotteryService, times(1)).getLotteryData(activityId);
    }

    @Test
    @DisplayName("保存中奖记录成功 - 有效的中奖信息")
    public void should_returnWinnerRecord_when_saveWinnerWithValidData() throws Exception {
        // 准备测试数据
        WinnerSaveDTO winnerDTO = TestDataBuilder.buildWinnerSaveDTO(
            "activity-1-1", "prize-1-1-1", "participant-1-1-1");
        
        WinnerVO winnerVO = new WinnerVO();
        winnerVO.setRecordId("winner-1");
        winnerVO.setParticipantName("张三");
        winnerVO.setPrizeName("特等奖");
        
        when(lotteryService.saveWinner(any(WinnerSaveDTO.class))).thenReturn(winnerVO);

        // 执行测试
        mockMvc.perform(post("/lottery/winners")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(winnerDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.recordId").value("winner-1"))
                .andExpect(jsonPath("$.data.participantName").value("张三"));

        verify(lotteryService, times(1)).saveWinner(any(WinnerSaveDTO.class));
    }

    @Test
    @DisplayName("保存中奖记录失败 - 参与人员已中奖")
    public void should_returnConflict_when_saveWinnerForAlreadyWonParticipant() throws Exception {
        // 准备测试数据
        WinnerSaveDTO winnerDTO = TestDataBuilder.buildWinnerSaveDTO(
            "activity-1-1", "prize-1-1-2", "participant-1-1-1");
        
        when(lotteryService.saveWinner(any(WinnerSaveDTO.class)))
            .thenThrow(new com.lottery.common.exception.BizException(409, "该参与人员已中奖"));

        // 执行测试
        mockMvc.perform(post("/lottery/winners")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(winnerDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("该参与人员已中奖"));

        verify(lotteryService, times(1)).saveWinner(any(WinnerSaveDTO.class));
    }

    @Test
    @DisplayName("保存中奖记录失败 - 奖项已抽完")
    public void should_returnConflict_when_saveWinnerForExhaustedPrize() throws Exception {
        // 准备测试数据
        WinnerSaveDTO winnerDTO = TestDataBuilder.buildWinnerSaveDTO(
            "activity-1-1", "prize-1-1-1", "participant-1-1-2");
        
        when(lotteryService.saveWinner(any(WinnerSaveDTO.class)))
            .thenThrow(new com.lottery.common.exception.BizException(409, "该奖项已抽完"));

        // 执行测试
        mockMvc.perform(post("/lottery/winners")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(winnerDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("该奖项已抽完"));

        verify(lotteryService, times(1)).saveWinner(any(WinnerSaveDTO.class));
    }

    @Test
    @DisplayName("查询中奖记录列表成功 - 存在的活动ID")
    public void should_returnWinnerList_when_getWinnersWithValidActivityId() throws Exception {
        // 准备测试数据
        String activityId = "activity-1-1";
        List<WinnerVO> winners = Arrays.asList(new WinnerVO(), new WinnerVO());
        
        when(lotteryService.getWinners(activityId)).thenReturn(winners);

        // 执行测试
        mockMvc.perform(get("/lottery/activities/{activityId}/winners", activityId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));

        verify(lotteryService, times(1)).getWinners(activityId);
    }

    @Test
    @DisplayName("查询中奖记录列表 - 无中奖记录")
    public void should_returnEmptyList_when_getWinnersForActivityWithNoWinners() throws Exception {
        // 准备测试数据
        String activityId = "activity-1-2";
        
        when(lotteryService.getWinners(activityId)).thenReturn(new ArrayList<>());

        // 执行测试
        mockMvc.perform(get("/lottery/activities/{activityId}/winners", activityId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());

        verify(lotteryService, times(1)).getWinners(activityId);
    }

    @Test
    @DisplayName("重置抽奖成功 - 存在的活动ID")
    public void should_returnSuccess_when_resetLotteryWithValidActivityId() throws Exception {
        // 准备测试数据
        String activityId = "activity-1-1";
        
        doNothing().when(lotteryService).resetLottery(activityId);

        // 执行测试
        mockMvc.perform(post("/lottery/activities/{activityId}/reset", activityId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(lotteryService, times(1)).resetLottery(activityId);
    }

    @Test
    @DisplayName("重置抽奖失败 - 活动不存在")
    public void should_returnNotFound_when_resetLotteryWithNonExistentActivityId() throws Exception {
        // 准备测试数据
        String activityId = "non-existent";
        
        doThrow(new com.lottery.common.exception.BizException(404, "活动不存在")).when(lotteryService).resetLottery(activityId);

        // 执行测试
        mockMvc.perform(post("/lottery/activities/{activityId}/reset", activityId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("活动不存在"));

        verify(lotteryService, times(1)).resetLottery(activityId);
    }

    // ==================== 活动管理测试 ====================

    @Test
    @DisplayName("获取活动列表成功")
    public void should_returnActivityList_when_getActivities() throws Exception {
        // 准备测试数据
        List<LotteryActivity> activities = Arrays.asList(
            TestDataBuilder.buildActivity("activity-1", "春节年会"),
            TestDataBuilder.buildActivity("activity-2", "中秋晚会")
        );
        
        when(lotteryService.getActivities()).thenReturn(activities);

        // 执行测试
        mockMvc.perform(get("/lottery/activities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));

        verify(lotteryService, times(1)).getActivities();
    }

    @Test
    @DisplayName("获取活动列表 - 租户无活动")
    public void should_returnEmptyList_when_getTenantActivitiesWithNoActivities() throws Exception {
        when(lotteryService.getActivities()).thenReturn(new ArrayList<>());

        // 执行测试
        mockMvc.perform(get("/lottery/activities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());

        verify(lotteryService, times(1)).getActivities();
    }

    @Test
    @DisplayName("获取活动详情成功 - 存在的活动ID")
    public void should_returnActivity_when_getActivityWithValidId() throws Exception {
        // 准备测试数据
        String activityId = "activity-1-1";
        LotteryActivity activity = TestDataBuilder.buildActivity(activityId, "春节年会");
        
        when(lotteryService.getActivity(activityId)).thenReturn(activity);

        // 执行测试
        mockMvc.perform(get("/lottery/activities/{activityId}", activityId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.activityId").value(activityId))
                .andExpect(jsonPath("$.data.activityName").value("春节年会"));

        verify(lotteryService, times(1)).getActivity(activityId);
    }

    @Test
    @DisplayName("获取活动详情失败 - 活动不存在")
    public void should_returnNotFound_when_getActivityWithNonExistentId() throws Exception {
        // 准备测试数据
        String activityId = "non-existent";
        
        when(lotteryService.getActivity(activityId))
            .thenThrow(new com.lottery.common.exception.BizException(404, "活动不存在"));

        // 执行测试
        mockMvc.perform(get("/lottery/activities/{activityId}", activityId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("活动不存在"));

        verify(lotteryService, times(1)).getActivity(activityId);
    }

    @Test
    @DisplayName("创建活动成功 - 完整活动信息")
    public void should_returnCreatedActivity_when_createActivityWithValidData() throws Exception {
        // 准备测试数据
        LotteryActivity activity = TestDataBuilder.buildActivity("activity-new", "新年会");
        LotteryActivity created = TestDataBuilder.buildActivity("activity-new", "新年会");
        created.setActivityId("activity-generated");
        
        when(lotteryService.createActivity(any(LotteryActivity.class))).thenReturn(created);

        // 执行测试
        mockMvc.perform(post("/lottery/activities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(activity)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.activityId").value("activity-generated"))
                .andExpect(jsonPath("$.data.activityName").value("新年会"));

        verify(lotteryService, times(1)).createActivity(any(LotteryActivity.class));
    }

    @Test
    @DisplayName("更新活动成功 - 有效的活动信息")
    public void should_returnUpdatedActivity_when_updateActivityWithValidData() throws Exception {
        // 准备测试数据
        String activityId = "activity-1-1";
        LotteryActivity activity = TestDataBuilder.buildActivity(activityId, "更新的年会名称");
        
        when(lotteryService.updateActivity(any(LotteryActivity.class))).thenReturn(activity);

        // 执行测试
        mockMvc.perform(put("/lottery/activities/{activityId}", activityId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(activity)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.activityName").value("更新的年会名称"));

        verify(lotteryService, times(1)).updateActivity(any(LotteryActivity.class));
    }

    @Test
    @DisplayName("更新活动失败 - 活动不存在")
    public void should_returnNotFound_when_updateActivityWithNonExistentId() throws Exception {
        // 准备测试数据
        String activityId = "non-existent";
        LotteryActivity activity = TestDataBuilder.buildActivity(activityId, "测试");
        
        when(lotteryService.updateActivity(any(LotteryActivity.class)))
            .thenThrow(new com.lottery.common.exception.BizException(404, "活动不存在"));

        // 执行测试
        mockMvc.perform(put("/lottery/activities/{activityId}", activityId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(activity)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("活动不存在"));

        verify(lotteryService, times(1)).updateActivity(any(LotteryActivity.class));
    }

    // ==================== 奖项管理测试 ====================

    @Test
    @DisplayName("获取奖项列表成功 - 存在的活动ID")
    public void should_returnPrizeList_when_getPrizesWithValidActivityId() throws Exception {
        // 准备测试数据
        String activityId = "activity-1-1";
        List<Prize> prizes = Arrays.asList(
            TestDataBuilder.buildPrize("prize-1", activityId, "特等奖", 1, 1),
            TestDataBuilder.buildPrize("prize-2", activityId, "一等奖", 2, 3)
        );
        
        when(lotteryService.getPrizes(activityId)).thenReturn(prizes);

        // 执行测试
        mockMvc.perform(get("/lottery/activities/{activityId}/prizes", activityId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));

        verify(lotteryService, times(1)).getPrizes(activityId);
    }

    @Test
    @DisplayName("获取奖项列表 - 无奖项")
    public void should_returnEmptyList_when_getPrizesForActivityWithNoPrizes() throws Exception {
        // 准备测试数据
        String activityId = "activity-1-2";
        
        when(lotteryService.getPrizes(activityId)).thenReturn(new ArrayList<>());

        // 执行测试
        mockMvc.perform(get("/lottery/activities/{activityId}/prizes", activityId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());

        verify(lotteryService, times(1)).getPrizes(activityId);
    }

    @Test
    @DisplayName("创建奖项成功 - 完整奖项信息")
    public void should_returnCreatedPrize_when_createPrizeWithValidData() throws Exception {
        // 准备测试数据
        Prize prize = TestDataBuilder.buildPrize("prize-new", "activity-1-1", "特等奖", 1, 1);
        Prize created = TestDataBuilder.buildPrize("prize-generated", "activity-1-1", "特等奖", 1, 1);
        
        when(lotteryService.createPrize(any(Prize.class))).thenReturn(created);

        // 执行测试
        mockMvc.perform(post("/lottery/prizes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(prize)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.prizeName").value("特等奖"));

        verify(lotteryService, times(1)).createPrize(any(Prize.class));
    }

    @Test
    @DisplayName("更新奖项成功 - 有效的奖项信息")
    public void should_returnUpdatedPrize_when_updatePrizeWithValidData() throws Exception {
        // 准备测试数据
        String prizeId = "prize-1-1-1";
        Prize prize = TestDataBuilder.buildPrize(prizeId, "activity-1-1", "特等奖（更新）", 1, 2);
        
        when(lotteryService.updatePrize(any(Prize.class))).thenReturn(prize);

        // 执行测试
        mockMvc.perform(put("/lottery/prizes/{prizeId}", prizeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(prize)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.prizeName").value("特等奖（更新）"));

        verify(lotteryService, times(1)).updatePrize(any(Prize.class));
    }

    @Test
    @DisplayName("更新奖项失败 - 奖项不存在")
    public void should_returnNotFound_when_updatePrizeWithNonExistentId() throws Exception {
        // 准备测试数据
        String prizeId = "non-existent";
        Prize prize = TestDataBuilder.buildPrize(prizeId, "activity-1-1", "测试", 1, 1);
        
        when(lotteryService.updatePrize(any(Prize.class)))
            .thenThrow(new com.lottery.common.exception.BizException(404, "奖项不存在"));

        // 执行测试
        mockMvc.perform(put("/lottery/prizes/{prizeId}", prizeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(prize)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("奖项不存在"));

        verify(lotteryService, times(1)).updatePrize(any(Prize.class));
    }

    @Test
    @DisplayName("删除奖项成功 - 未抽取的奖项")
    public void should_returnSuccess_when_deletePrizeNotDrawn() throws Exception {
        // 准备测试数据
        String prizeId = "prize-1-1-4";
        
        doNothing().when(lotteryService).deletePrize(prizeId);

        // 执行测试
        mockMvc.perform(delete("/lottery/prizes/{prizeId}", prizeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(lotteryService, times(1)).deletePrize(prizeId);
    }

    @Test
    @DisplayName("删除奖项失败 - 已抽取的奖项")
    public void should_returnConflict_when_deletePrizeAlreadyDrawn() throws Exception {
        // 准备测试数据
        String prizeId = "prize-1-1-1";
        
        doThrow(new com.lottery.common.exception.BizException(409, "该奖项已有中奖记录，不能删除"))
            .when(lotteryService).deletePrize(prizeId);

        // 执行测试
        mockMvc.perform(delete("/lottery/prizes/{prizeId}", prizeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("该奖项已有中奖记录，不能删除"));

        verify(lotteryService, times(1)).deletePrize(prizeId);
    }

    // ==================== 参会人员管理测试 ====================

    @Test
    @DisplayName("获取参会人员列表成功 - 存在的活动ID")
    public void should_returnParticipantList_when_getParticipantsWithValidActivityId() throws Exception {
        // 准备测试数据
        String activityId = "activity-1-1";
        List<Participant> participants = Arrays.asList(
            TestDataBuilder.buildParticipant("p-1", activityId, "张三", "技术部"),
            TestDataBuilder.buildParticipant("p-2", activityId, "李四", "产品部")
        );
        
        when(lotteryService.getParticipants(activityId)).thenReturn(participants);

        // 执行测试
        mockMvc.perform(get("/lottery/activities/{activityId}/participants", activityId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));

        verify(lotteryService, times(1)).getParticipants(activityId);
    }

    @Test
    @DisplayName("获取参会人员列表 - 无参会人员")
    public void should_returnEmptyList_when_getParticipantsForActivityWithNoParticipants() throws Exception {
        // 准备测试数据
        String activityId = "activity-1-2";
        
        when(lotteryService.getParticipants(activityId)).thenReturn(new ArrayList<>());

        // 执行测试
        mockMvc.perform(get("/lottery/activities/{activityId}/participants", activityId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());

        verify(lotteryService, times(1)).getParticipants(activityId);
    }

    @Test
    @DisplayName("添加参会人员成功 - 完整参会人员信息")
    public void should_returnCreatedParticipant_when_createParticipantWithValidData() throws Exception {
        // 准备测试数据
        Participant participant = TestDataBuilder.buildParticipant("p-new", "activity-1-1", "王五", "市场部");
        Participant created = TestDataBuilder.buildParticipant("p-generated", "activity-1-1", "王五", "市场部");
        
        when(lotteryService.createParticipant(any(Participant.class))).thenReturn(created);

        // 执行测试
        mockMvc.perform(post("/lottery/participants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(participant)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("王五"))
                .andExpect(jsonPath("$.data.department").value("市场部"));

        verify(lotteryService, times(1)).createParticipant(any(Participant.class));
    }

    @Test
    @DisplayName("更新参会人员成功 - 有效的参会人员信息")
    public void should_returnUpdatedParticipant_when_updateParticipantWithValidData() throws Exception {
        // 准备测试数据
        String participantId = "participant-1-1-1";
        Participant participant = TestDataBuilder.buildParticipant(participantId, "activity-1-1", "张三（更新）", "技术部");
        
        when(lotteryService.updateParticipant(any(Participant.class))).thenReturn(participant);

        // 执行测试
        mockMvc.perform(put("/lottery/participants/{participantId}", participantId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(participant)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("张三（更新）"));

        verify(lotteryService, times(1)).updateParticipant(any(Participant.class));
    }

    @Test
    @DisplayName("更新参会人员失败 - 参会人员不存在")
    public void should_returnNotFound_when_updateParticipantWithNonExistentId() throws Exception {
        // 准备测试数据
        String participantId = "non-existent";
        Participant participant = TestDataBuilder.buildParticipant(participantId, "activity-1-1", "测试", "测试部");
        
        when(lotteryService.updateParticipant(any(Participant.class)))
            .thenThrow(new com.lottery.common.exception.BizException(404, "参会人员不存在"));

        // 执行测试
        mockMvc.perform(put("/lottery/participants/{participantId}", participantId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(participant)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("参会人员不存在"));

        verify(lotteryService, times(1)).updateParticipant(any(Participant.class));
    }

    @Test
    @DisplayName("删除参会人员成功 - 未中奖的参会人员")
    public void should_returnSuccess_when_deleteParticipantNotWon() throws Exception {
        // 准备测试数据
        String participantId = "participant-1-1-10";
        
        doNothing().when(lotteryService).deleteParticipant(participantId);

        // 执行测试
        mockMvc.perform(delete("/lottery/participants/{participantId}", participantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(lotteryService, times(1)).deleteParticipant(participantId);
    }

    @Test
    @DisplayName("删除参会人员失败 - 已中奖的参会人员")
    public void should_returnConflict_when_deleteParticipantAlreadyWon() throws Exception {
        // 准备测试数据
        String participantId = "participant-1-1-1";
        
        doThrow(new com.lottery.common.exception.BizException(409, "该参会人员已中奖，不能删除"))
            .when(lotteryService).deleteParticipant(participantId);

        // 执行测试
        mockMvc.perform(delete("/lottery/participants/{participantId}", participantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("该参会人员已中奖，不能删除"));

        verify(lotteryService, times(1)).deleteParticipant(participantId);
    }
}
