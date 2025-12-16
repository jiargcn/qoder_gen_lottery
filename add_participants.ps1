# 批量添加参会人员脚本
param(
    [string]$Token = ""
)

$baseUrl = "http://localhost:8080/api/lottery"
$activityName = "4444"

# 获取Token
if ([string]::IsNullOrEmpty($Token)) {
    Write-Host "请打开浏览器按 F12，在 Console 中输入: localStorage.getItem('token')" -ForegroundColor Cyan
    Write-Host "然后复制Token值(去掉引号)`n" -ForegroundColor Cyan
    $Token = Read-Host "请输入 Token"
}

$headers = @{
    "Authorization" = "Bearer $Token"
    "Content-Type" = "application/json"
}

# 查找活动
Write-Host "`n正在查找活动: $activityName..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/activities" -Method Get -Headers $headers
    $activity = $response.data | Where-Object { $_.activityName -eq $activityName } | Select-Object -First 1
    
    if ($null -eq $activity) {
        Write-Host "错误: 未找到名为 '$activityName' 的活动" -ForegroundColor Red
        exit 1
    }
    
    $activityId = $activity.activityId
    Write-Host "找到活动: $($activity.activityName) (ID: $activityId)" -ForegroundColor Green
} catch {
    Write-Host "错误: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# 批量添加100个人员
Write-Host "`n开始添加100个参会人员..." -ForegroundColor Yellow
$successCount = 0
$failCount = 0

for ($i = 1; $i -le 100; $i++) {
    $participant = @{
        activityId = $activityId
        name = "参会人员$i"
        employeeNo = "EMP" + $i.ToString("D4")
        department = "部门" + (($i - 1) % 10 + 1)
        phone = "138" + $i.ToString("D8")
        email = "user$i@company.com"
    } | ConvertTo-Json -Compress
    
    try {
        $result = Invoke-RestMethod -Uri "$baseUrl/participants" -Method Post -Headers $headers -Body $participant
        $successCount++
        if ($i % 10 -eq 0) {
            Write-Host "已添加 $i 个人员..." -ForegroundColor Green
        }
    } catch {
        $failCount++
        Write-Host "第 $i 个失败: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`n完成!" -ForegroundColor Cyan
Write-Host "成功: $successCount 个" -ForegroundColor Green
Write-Host "失败: $failCount 个" -ForegroundColor $(if($failCount -eq 0){"Green"}else{"Red"})
