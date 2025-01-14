package plus.gaga.middleware.sdk.infrustracture.openai.impl;

import com.alibaba.fastjson2.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import plus.gaga.middleware.sdk.infrustracture.git.GitCommand;
import plus.gaga.middleware.sdk.infrustracture.openai.IOpenAI;
import plus.gaga.middleware.sdk.infrustracture.openai.dto.ChatCompletionRequestDTO;
import plus.gaga.middleware.sdk.infrustracture.openai.dto.ChatCompletionSyncResponseDTO;
import plus.gaga.middleware.sdk.type.utils.BearerTokenUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ChatGLM4Impl implements IOpenAI {

    private final Logger log = LoggerFactory.getLogger(ChatGLM4Impl.class);


    private final String apiHost;
    private final String apiKey;

    public ChatGLM4Impl(String apiHost, String apiKey) {
        this.apiHost = apiHost;
        this.apiKey = apiKey;
    }

    @Override
    public ChatCompletionSyncResponseDTO completions(ChatCompletionRequestDTO chatCompletionRequestDTO) throws IOException {
        // 设置请求头 请求属性
        String apiSecret = BearerTokenUtils.getToken(apiKey);
        HttpURLConnection httpConnection = (HttpURLConnection) new URL(apiHost).openConnection();
        httpConnection.setRequestMethod("POST");
        httpConnection.setRequestProperty("Authorization", "Bearer " + apiSecret);
        httpConnection.setRequestProperty("Content-Type", "application/json");
        httpConnection.setDoOutput(true);
        // 设置请求体
        byte[] bytes = JSON.toJSONString(chatCompletionRequestDTO).getBytes(StandardCharsets.UTF_8);
        try (OutputStream outputStream = httpConnection.getOutputStream()) {
            outputStream.write(bytes, 0, bytes.length);
        }
        // 解析响应
        int responseCode = httpConnection.getResponseCode();
        log.info("responseCode：{}", responseCode);

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line = "";
        while ((line = bufferedReader.readLine()) != null) {
            response.append(line);
        }
        bufferedReader.close();
        httpConnection.disconnect();
        return JSON.parseObject(response.toString(), ChatCompletionSyncResponseDTO.class);
    }
}
