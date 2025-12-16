# 重置活动4444的PowerShell脚本

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  重置活动4444" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. 先登录获取token (使用默认的测试账号)
Write-Host "[1/3] 正在登录获取token..." -ForegroundColor Yellow
$loginBody = @{
    username = "admin"
    password = "admin123"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
    $token = $loginResponse.data.token
    Write-Host "✓ 登录成功!" -ForegroundColor Green
} catch {
    Write-Host "✗ 登录失败: $_" -ForegroundColor Red
    exit 1
}

# 2. 获取活动列表,找到4444的活动ID
Write-Host ""
Write-Host "[2/3] 正在查找活动4444..." -ForegroundColor Yellow
try {
    $headers = @{
        "Authorization" = "Bearer $token"
        "Content-Type" = "application/json"
    }
    
    $activitiesResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/lottery/activities" -Method Get -Headers $headers
    $activity = $activitiesResponse.data | Where-Object { $_.activityName -eq '4444' } | Select-Object -First 1
    
    if (!$activity) {
        Write-Host "✗ 未找到名为'4444'的活动" -ForegroundColor Red
        exit 1
    }
    
    $activityId = $activity.activityId
    $currentStatus = $activity.status
    Write-Host "✓ 找到活动: ID=$activityId, 当前状态=$currentStatus" -ForegroundColor Green
} catch {
    Write-Host "✗ 查询活动失败: $_" -ForegroundColor Red
    exit 1
}

# 3. 调用reset接口
Write-Host ""
Write-Host "[3/3] 正在重置抽奖..." -ForegroundColor Yellow
try {
    $resetResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/lottery/activities/$activityId/reset" -Method Post -Headers $headers
    Write-Host "✓ 重置成功!所有中奖记录已删除" -ForegroundColor Green
} catch {
    Write-Host "✗ 重置失败: $_" -ForegroundColor Red
    exit 1
}

# 4. 修改活动状态为进行中
Write-Host ""
Write-Host "[4/4] 正在修改活动状态为进行中..." -ForegroundColor Yellow
try {
    $updateBody = @{
        activityId = $activityId
        activityName = $activity.activityName
        status = "ACTIVE"
        startTime = $activity.startTime
        location = $activity.location
        description = $activity.description
    } | ConvertTo-Json
    
    $updateResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/lottery/activities/$activityId" -Method Put -Headers $headers -Body $updateBody -ContentType "application/json"
    Write-Host "✓ 状态修改成功!活动现在是'进行中'" -ForegroundColor Green
} catch {
    Write-Host "✗ 修改状态失败: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  操作完成!" -ForegroundColor Cyan
Write-Host "  - 中奖记录已删除" -ForegroundColor Green
Write-Host "  - 活动状态已改为进行中(ACTIVE)" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
