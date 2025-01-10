package plus.gaga.middleware.sdk;


import com.alibaba.fastjson2.JSON;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.StringUtils;
import plus.gaga.middleware.sdk.domain.model.Model;
import plus.gaga.middleware.sdk.infrustracture.openai.dto.ChatCompletionRequestDTO;
import plus.gaga.middleware.sdk.infrustracture.openai.dto.ChatCompletionSyncResponseDTO;
import plus.gaga.middleware.sdk.type.utils.BearerTokenUtils;
import plus.gaga.middleware.sdk.type.utils.RandomUtil;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;

public class OpenAiCodeReview {

    public static void main(String[] args) throws Exception {
        System.out.println("\"测试执行\" === " + "测试执行");

//        String githubToken = "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";
        String githubToken = System.getenv("GITHUB_TOKEN");
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

        // 写入日志库
        String writeLog = writeLog(log, githubToken);
        System.out.println("写入日志库URL\n" + writeLog);

    }

    /**
     * 代码审查
     *
     * @param diffCode 需要审查的代码
     * @return 返回建议说明，字符串形式 log日志
     * @throws IOException
     */
    public static String codeReview(String diffCode) throws Exception {
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
        // 解析实体类 返回数据
        String log = responseDTO.getChoices().get(0).getMessage().getContent();
        System.out.println(log);
        return log;

    }

    /**
     * 写入日志库
     *
     * @param log         评审日志
     * @param githubToken 仓库的钥匙
     * @return 地址url
     * @throws Exception
     */
    public static String writeLog(String log, String githubToken) throws Exception {
        if (StringUtils.isEmptyOrNull(githubToken)) {
            throw new RuntimeException("Github token 不能为空");
        }
        Git git = Git.cloneRepository()
                .setURI("https://github.com/oldCaptain20/my-openai-code-review-log.git")
                .setDirectory(new File("repo"))
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(githubToken, ""))
                .call();

        String dateFolderName = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        File dateFolder = new File("repo/" + dateFolderName);
        if (!dateFolder.exists()) {
            dateFolder.mkdirs();
        }

        String fileName = RandomUtil.generateRandomString(12) + ".md";
        File newFile = new File(dateFolder, fileName);
        try (FileWriter writer = new FileWriter(newFile)) {
            writer.write(log);
        }

        git.add().addFilepattern(dateFolderName + "/" + fileName).call();
        git.commit().setMessage("Add new file").call();
        git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(githubToken, ""));

        return "https://github.com/oldCaptain20/my-openai-code-review-log/blob/master/" + dateFolderName + "/" + fileName;

    }

}
