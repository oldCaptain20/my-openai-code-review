package plus.gaga.middleware.sdk;


import com.alibaba.fastjson2.JSON;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import plus.gaga.middleware.sdk.domain.model.Model;
import plus.gaga.middleware.sdk.infrustracture.openai.dto.ChatCompletionRequestDTO;
import plus.gaga.middleware.sdk.infrustracture.openai.dto.ChatCompletionSyncResponseDTO;
import plus.gaga.middleware.sdk.type.utils.BearerTokenUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class OpenAiCodeReview {

    public static void main(String[] args) throws Exception {
        System.out.println("\"测试执行\" === " + "测试执行");

        // 代码检出
        ProcessBuilder processBuilder = new ProcessBuilder("git", "diff", "HEAD~1", "HEAD");
        // 执行命令时的工作目录。new File(".") 表示当前目录
        processBuilder.directory(new File("."));

        // 读取命令输出
        Process process = processBuilder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder diffCode = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            diffCode.append(line);
        }
        int exitCode = process.waitFor();
        System.out.println("Exited with code: " + exitCode);
        System.out.println("待评审代码\n" + diffCode);
        String log = codeReview(diffCode.toString());
        System.out.println("评审结果：\n" + log);
    }

    /**
     * 代码审查
     *
     * @param diffCode 需要审查的代码
     * @return 返回建议说明，字符串形式
     * @throws IOException
     */
    public static String codeReview(String diffCode) throws IOException {
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
                add(new ChatCompletionRequestDTO.Prompt("user", diffCode));
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

    public static boolean writeLog(String log) throws Exception {
        String githubToken = "";
        String password = "";
        String repository = "repo";

        Git repo = Git.cloneRepository()
                .setURI("https://github.com/oldCaptain20/my-openai-code-review-log.git")
                // 创建一个文件夹，克隆操作会把仓库的代码下载到repo文件夹中
                .setDirectory(new File(repository))
                // GitHub 仓库需要身份验证(例如私人仓库)
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(githubToken, password))
                .call();

        String fileName = LocalDate.now().toString();
        File file = new File(repository + "/" + fileName);
        if (!file.exists()) {
            file.mkdirs();
        }


        return true;
    }


}
