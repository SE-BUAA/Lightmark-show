param(
  [string]$BaseUrl = "http://localhost:8080",
  [string]$Token = ""
)

$headers = @{
  "Content-Type" = "application/json"
}

if ($Token) {
  $headers["Authorization"] = "Bearer $Token"
}

$body = @{
  destination = "浙江杭州"
  days = 2
  startDate = "2026-07-01"
  budget = "人均3000元"
  preferences = "慢节奏、美食、城市漫步、少走路"
} | ConvertTo-Json -Depth 8

Write-Host "POST $BaseUrl/api/itinerary/ai/generate"
try {
  $response = Invoke-RestMethod `
    -Method Post `
    -Uri "$BaseUrl/api/itinerary/ai/generate" `
    -Headers $headers `
    -Body $body `
    -TimeoutSec 90

  $response | ConvertTo-Json -Depth 12
} catch {
  Write-Error $_
  exit 1
}
