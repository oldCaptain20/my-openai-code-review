$url = "https://open.bigmodel.cn/api/paas/v4/chat/completions"
$headers = @{
    "Authorization" = "Bearer  eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiIsInNpZ25fdHlwZSI6IlNJR04ifQ.eyJhcGlfa2V5IjoiYTFhNjE0ZDFlZGJkNDcxYjg0NTM2YjQ2MjZlNDYxNWIiLCJleHAiOjE3MzYwNzYxNzc2MDcsInRpbWVzdGFtcCI6MTczNjA3NDM3NzYxMn0.Hp4VyM_oL1sa6tjrC5s8ILxla3n7IEZwgFbXSVwSpWM
"
    "Content-Type"  = "application/json"
    "User-Agent"    = "User-Agent: Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)"
}

$body = @{
    "model"  = "glm-4-flash"
    "stream" = "true"
    "messages" = @(
    @{
        "role"    = "user"
        "content" = "1+1"
    }
    )
}


$bodyJson = $body | ConvertTo-Json -Depth 3

$response = Invoke-RestMethod -Uri $url -Method Post -Headers $headers -Body $bodyJson

$response
