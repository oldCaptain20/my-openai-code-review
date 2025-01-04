curl -X POST \
        -H "Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiIsInNpZ25fdHlwZSI6IlNJR04ifQ.eyJhcGlfa2V5IjoiYTFhNjE0ZDFlZGJkNDcxYjg0NTM2YjQ2MjZlNDYxNWIiLCJleHAiOjE3MzU5NjI1OTk5MTksInRpbWVzdGFtcCI6MTczNTk2MDc5OTkyNX0.hzCmELTWkkZ8wXe2X4s5up0Mdl1oNt2wEJiTw0Yk5yw" \
        -H "Content-Type: application/json" \
        -H "User-Agent: Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)" \
        -d '{
          "model":"glm-4",
          "stream": "true",
          "messages": [
              {
                  "role": "user",
                  "content": "1+1"
              }
          ]
        }' \
  https://open.bigmodel.cn/api/paas/v4/chat/completions