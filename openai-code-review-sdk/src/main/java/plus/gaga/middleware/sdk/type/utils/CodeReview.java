package plus.gaga.middleware.sdk.type.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import plus.gaga.middleware.sdk.domain.model.Model;
import plus.gaga.middleware.sdk.infrustracture.openai.dto.ChatCompletionRequestDTO;
import plus.gaga.middleware.sdk.infrustracture.openai.dto.ChatCompletionSyncResponseDTO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class CodeReview {

    public static String codeReview(String diff) throws IOException {
        String apiKeySecret = "a1a614d1edbd471b84536b4626e4615b.DofbLZQYkFOfrBtM";
        String token = BearerTokenUtils.getToken(apiKeySecret);

        // 代码调用
        URL url = new URL("https://open.bigmodel.cn/api/paas/v4/chat/completions");
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Authorization", "Bearer " + token);
        httpURLConnection.setRequestProperty("Content-Type", "application/json");
        httpURLConnection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        httpURLConnection.setDoOutput(true);

/*        String jsonInputString = "{" +
                "  \"model\": \"glm-4-flash\"," +
                "  \"messages\": [" +
                "    {" +
                "      \"role\": \"user\"," +
                "      \"content\": \"你是一个高级java开发工程师，精通各类场景方案、架构设计和设计模式，性能优化请您根据git diff记录，对代码做出评审。代码为: " +
                "农夫过河的问题" + "\"" +
                "    }" +
                "  ]" +
                "}";
        */

        // 传递参数
        ChatCompletionRequestDTO chatCompletionRequest = new ChatCompletionRequestDTO();
        chatCompletionRequest.setModel(Model.GLM_4_FLASH.getCode());
        chatCompletionRequest.setMessages(new ArrayList<ChatCompletionRequestDTO.Prompt>() {
            private static final long serialVersionUID = -7988151926241837899L;

            {
                add(new ChatCompletionRequestDTO.Prompt("user", "你是一个高级编程架构师，精通各类场景方案、架构设计和编程语言请，请您根据git diff记录，对代码做出评审。代码如下:"));
                add(new ChatCompletionRequestDTO.Prompt("user", diff));
            }
        });
        String jsonInputString = JSON.toJSONString(chatCompletionRequest);
        System.out.println("jsonInputString = " + jsonInputString);

        // 传递参数
        OutputStream outputStream = httpURLConnection.getOutputStream();
        byte[] bytes = jsonInputString.getBytes(StandardCharsets.UTF_8);
        outputStream.write(bytes);

        // 获取返回
        int responseCode = httpURLConnection.getResponseCode();
        System.out.println("responseCode = " + responseCode);

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = bufferedReader.readLine()) != null) {
            content.append(inputLine);
        }

        bufferedReader.close();
        httpURLConnection.disconnect();

        System.out.println("content = " + content);

        ChatCompletionSyncResponseDTO responseDTO = JSON.parseObject(content.toString(), ChatCompletionSyncResponseDTO.class);
        String log = responseDTO.getChoices().get(0).getMessage().getContent();
        System.out.println(log);
        return log;

    }

}
