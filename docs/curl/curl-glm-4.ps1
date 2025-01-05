$url = "https://open.bigmodel.cn/api/paas/v4/chat/completions"
$headers = @{
    "Authorization" = "Bearer  eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiIsInNpZ25fdHlwZSI6IlNJR04ifQ.eyJhcGlfa2V5IjoiYTFhNjE0ZDFlZGJkNDcxYjg0NTM2YjQ2MjZlNDYxNWIiLCJleHAiOjE3MzYwNzkyNTA4MTksInRpbWVzdGFtcCI6MTczNjA3NzQ1MDgyNH0.fu_iBmKqNzLjIKDpbcpj4RJqGyct-w1YWFsviTKMfbM
"
    "Content-Type"  = "application/json"
    "User-Agent"    = "User-Agent: Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)"
}

$body = @{
    "model"  = "glm-4-flash"
    "messages" = @(
    @{
        "role"    = "user"
        "content" = "1+1"
    }
    )
}


$bodyJson = $body | ConvertTo-Json -Depth 10

$response = Invoke-RestMethod -Uri $url -Method Post -Headers $headers -Body $bodyJson

$response
