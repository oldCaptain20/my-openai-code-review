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


}
