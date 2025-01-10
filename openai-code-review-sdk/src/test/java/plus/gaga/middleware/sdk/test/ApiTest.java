package plus.gaga.middleware.sdk.test;


import com.alibaba.fastjson2.JSON;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.HttpTransport;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.junit.Test;
import plus.gaga.middleware.sdk.infrustracture.openai.dto.ChatCompletionSyncResponseDTO;
import plus.gaga.middleware.sdk.type.utils.BearerTokenUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ApiTest {

    public static void main(String[] args) {
        String apiKeySecret = "a1a614d1edbd471b84536b4626e4615b.DofbLZQYkFOfrBtM";
        String token = BearerTokenUtils.getToken(apiKeySecret);
        System.out.println(token);
    }

    @Test
    public void test_http() throws IOException {
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

        String code = "1+1";
        String jsonInputString = "{" +
                "  \"model\": \"glm-4-flash\"," +
                "  \"messages\": [" +
                "    {" +
                "      \"role\": \"user\"," +
                "      \"content\": \"你是一个高级java开发工程师，精通各类场景方案、架构设计和设计模式，性能优化请您根据git diff记录，对代码做出评审。代码为: " +
                code + "\"" +
                "    }" +
                "  ]" +
                "}";
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
        System.out.println(responseDTO.getChoices().get(0).getMessage().getContent());


    }

    @Test
    public void test_json_parse() {
        String json = "{\"choices\":[{\"finish_reason\":\"stop\",\"index\":0,\"message\":{\"content\":\"为了对给定的代码 \\\"1+1\\\" 进行评审，我需要查看相关的 `git diff` 记录。`git diff` 命令用于显示两个提交（或更早的版本）之间的差异。以下是假设的几种情况及其对应的评审：\\n\\n### 情况1：`git diff` 显示代码从 \\\"1+1\\\" 变为 \\\"2\\\"\\n```plaintext\\ndiff --git a/original.java b/modified.java\\nindex 12345..67890 100644\\n--- a/original.java\\n+++ b/modified.java\\n@@ -1,2 +1,2 @@\\n-1+1\\n+2\\n```\\n**评审：**\\n- **优点**：代码简化了表达式，从字符串表示的数学表达式 \\\"1+1\\\" 变成了结果值 \\\"2\\\"。这种改动在数学计算上是有意义的，减少了不必要的计算和转换。\\n- **缺点**：如果这段代码是作为示例或测试，直接从 \\\"1+1\\\" 改成 \\\"2\\\" 可能会导致代码功能丢失或测试失败。需要确保这种改动不会影响程序的预期行为。\\n\\n### 情况2：`git diff` 显示代码从 \\\"1+1\\\" 变为 \\\"1 + 1\\\"\\n```plaintext\\ndiff --git a/original.java b/modified.java\\nindex 12345..67890 100644\\n--- a/original.java\\n+++ b/modified.java\\n@@ -1,2 +1,2 @@\\n-1+1\\n+1 + 1\\n```\\n**评审：**\\n- **优点**：在数学表达式中添加空格通常可以提高代码的可读性。\\n- **缺点**：这种改动在没有其他上下文的情况下可能是不必要的，并且可能会引起误解，因为 \\\"1 + 1\\\" 仍然等于 \\\"2\\\"。\\n\\n### 情况3：`git diff` 显示代码从 \\\"1+1\\\" 变为 \\\"1 + 2\\\"\\n```plaintext\\ndiff --git a/original.java b/modified.java\\nindex 12345..67890 100644\\n--- a/original.java\\n+++ b/modified.java\\n@@ -1,2 +1,2 @@\\n-1+1\\n+1 + 2\\n```\\n**评审：**\\n- **优点**：如果这个改动是为了改变表达式的结果，那么它是有意义的，从数学角度来看，\\\"1 + 2\\\" 等于 \\\"3\\\"。\\n- **缺点**：这种改动如果是在不相关的代码中，可能会引起混淆。需要确保这种改动是符合程序逻辑的。\\n\\n请注意，上述评审是基于假设的 `git diff` 输出。实际的代码改动可能包含更多上下文信息，例如代码的功能、位置以及与其他代码模块的关系，这些都可能影响评审结果。在实际工作中，评审应结合具体的代码上下文和项目要求进行。\",\"role\":\"assistant\"}}],\"created\":1736072868,\"id\":\"202501051827291024b85bdc1d4699\",\"model\":\"glm-4-flash\",\"request_id\":\"202501051827291024b85bdc1d4699\",\"usage\":{\"completion_tokens\":598,\"prompt_tokens\":42,\"total_tokens\":640}}";
        ChatCompletionSyncResponseDTO responseDTO = JSON.parseObject(json.toString(), ChatCompletionSyncResponseDTO.class);
        System.out.println(responseDTO.getChoices().get(0).getMessage().getContent());
    }

    @Test
    public void test_log_repository_clone() throws GitAPIException {
        String githubToken = "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";
        Git git = Git.cloneRepository()
                .setURI("https://github.com/oldCaptain20/my-openai-code-review-log.git")
                .setDirectory(new File("repo"))
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(githubToken, ""))
                .call();
        System.out.println("克隆代码");

    }

    @Test
    public void test_log_repository_write() throws GitAPIException, IOException {
        String githubToken = "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";
        Git git = Git.open(new File("D:\\Project\\my-openai-code-review\\openai-code-review-sdk\\repo"));
        // 获取 Git 对象

        // 设置远程仓库 URL 和认证信息
        String remoteName = "origin"; // 通常使用 "origin" 作为默认远程仓库名
        String remoteURL = "https://github.com/oldCaptain20/my-openai-code-review-log.git"; // 远程仓库 URL
        // 配置推送

        git.add().addFilepattern(".").call();
        git.commit().setMessage("Add new file 1").call();
        git.push()
                .setRemote(remoteName)
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(githubToken, ""))
                .setPushAll() // 推送所有分支
                .call();

        // 输出成功信息
        System.out.println("Successfully pushed to remote repository: " + remoteURL);

        // 关闭 Git 对象
        git.close();

    }

}