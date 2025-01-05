$url = "https://open.bigmodel.cn/api/paas/v4/chat/completions"
$headers = @{
    "Authorization" = "Bearer  eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiIsInNpZ25fdHlwZSI6IlNJR04ifQ.eyJhcGlfa2V5IjoiYTFhNjE0ZDFlZGJkNDcxYjg0NTM2YjQ2MjZlNDYxNWIiLCJleHAiOjE3MzYwNTAxOTM0NjgsInRpbWVzdGFtcCI6MTczNjA0ODM5MzQ3M30.eVRQtjAQiqvdYX7k3LW3RpG4dQJeYR9PPWztBaR1Trw
"
    "Content-Type"  = "application/json"
    "User-Agent"    = "User-Agent: Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)"
}

$body = @{
    "model"  = "glm-4"
    "stream" = "true"
    "messages" = @(
    @{
        "role"    = "user"
        "content" = "炒股的好处有哪些"
    }
    )
}


$bodyJson = $body | ConvertTo-Json -Depth 3

$response = Invoke-RestMethod -Uri $url -Method Post -Headers $headers -Body $bodyJson

$response
