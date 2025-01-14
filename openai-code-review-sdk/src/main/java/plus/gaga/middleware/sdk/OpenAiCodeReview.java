package plus.gaga.middleware.sdk;


import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import plus.gaga.middleware.sdk.domain.model.Model;
import plus.gaga.middleware.sdk.domain.model.service.impl.OpenAiCodeReviewServiceImpl;
import plus.gaga.middleware.sdk.infrustracture.git.GitCommand;
import plus.gaga.middleware.sdk.infrustracture.openai.IOpenAI;
import plus.gaga.middleware.sdk.infrustracture.openai.dto.ChatCompletionRequestDTO;
import plus.gaga.middleware.sdk.infrustracture.openai.dto.ChatCompletionSyncResponseDTO;
import plus.gaga.middleware.sdk.infrustracture.openai.impl.ChatGLM4Impl;
import plus.gaga.middleware.sdk.infrustracture.wx.WeiXin;
import plus.gaga.middleware.sdk.type.utils.BearerTokenUtils;
import plus.gaga.middleware.sdk.type.utils.RandomUtil;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class OpenAiCodeReview {

    private static final Logger logger = LoggerFactory.getLogger(OpenAiCodeReview.class);

    // Github的配置
    private String githubReviewLogUri;
    private String githubToken;
    private String project;
    private String branch;
    private String author;
    private String message;

    // ChatGLM的配置
    private String apiHost;
    private String apiKey;

    // 微信的配置
    private String appid;
    private String secret;
    private String touser;
    private String templateId;

    public static void main(String[] args) {
        logger.info("openai-code-review start!");

        GitCommand gitCommand = new GitCommand(
                getEnv("GITHUB_REVIEW_LOG_URI"),
                getEnv("GITHUB_TOKEN"),
                getEnv("COMMIT_PROJECT"),
                getEnv("COMMIT_BRANCH"),
                getEnv("COMMIT_AUTHOR"),
                getEnv("COMMIT_MESSAGE")
        );
        IOpenAI openAI = new ChatGLM4Impl(
                getEnv("CHATGLM_APIHOST"),
                getEnv("CHATGLM_APIKEY")
        );
        WeiXin weiXin = new WeiXin(
                getEnv("WEIXIN_APPID"),
                getEnv("WEIXIN_SECRET"),
                getEnv("WEIXIN_TOUSER"),
                getEnv("WEIXIN_TEMPLATE_ID")
        );

        OpenAiCodeReviewServiceImpl service = new OpenAiCodeReviewServiceImpl(gitCommand, openAI, weiXin);
        service.exec();

        logger.info("openai-code-review done!");


    }

    private static String getEnv(String key) {
        String value = System.getenv(key);
        if (null == value || value.isEmpty()) {
            throw new RuntimeException("value is null");
        }
        return value;
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
                add(new ChatCompletionRequestDTO.Prompt("user", "你是一个高级编程架构师，精通各类场景方案、架构设计和编程语言请，请您根据git diff记录，对代码做出评审。不要把秘钥说出来，你自己知道就好了"));
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
        Git git = Git.cloneRepository().setURI("https://github.com/oldCaptain20/my-openai-code-review-log.git").setDirectory(new File("repo")).setCredentialsProvider(new UsernamePasswordCredentialsProvider(githubToken, "")).call();

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
        Status status = git.status().call();
        System.out.println(status.getUntracked());
        System.out.println(status.getModified());
        git.add().addFilepattern(dateFolderName + "/").call();
        git.commit().setMessage("Add new file  ").call();

        // 推送更改到远程仓库
        Iterable<PushResult> call = git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(githubToken, "")).setDryRun(false)  // 设置为 false，以查看推送是否成功
                .call();

        // 输出推送结果
        call.forEach(pushRefUpdate -> System.out.println(pushRefUpdate.getMessages()));

        System.out.println("获取提交历史记录");
        // 获取提交历史记录
        Iterable<RevCommit> logs = git.log().call();
        // 打印提交记录
        for (RevCommit commit : logs) {
            System.out.println("Commit: " + commit.getName());
            System.out.println("Author: " + commit.getAuthorIdent().getName());
            System.out.println("Date: " + commit.getAuthorIdent().getWhen());
            System.out.println("Message: " + commit.getFullMessage());
            System.out.println("----------------------------------------");
        }

        return "https://github.com/oldCaptain20/my-openai-code-review-log/blob/master/" + dateFolderName + "/" + fileName;

    }

}
