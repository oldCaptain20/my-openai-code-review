package plus.gaga.middleware.sdk.domain.model.service.impl;

import plus.gaga.middleware.sdk.domain.model.service.AbstractOpenAiCodeReviewService;
import plus.gaga.middleware.sdk.infrustracture.git.GitCommand;
import plus.gaga.middleware.sdk.infrustracture.openai.IOpenAI;
import plus.gaga.middleware.sdk.infrustracture.openai.dto.ChatCompletionRequestDTO;
import plus.gaga.middleware.sdk.infrustracture.openai.dto.ChatCompletionSyncResponseDTO;
import plus.gaga.middleware.sdk.infrustracture.wx.WeiXin;
import plus.gaga.middleware.sdk.infrustracture.wx.dto.TemplateMessageDTO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OpenAiCodeReviewServiceImpl extends AbstractOpenAiCodeReviewService {

    public OpenAiCodeReviewServiceImpl(GitCommand gitCommand, IOpenAI openAI, WeiXin weiXin) {
        super(gitCommand, openAI, weiXin);
    }

    @Override
    protected String getDiffCode() throws IOException, InterruptedException {
        return gitCommand.diff();
    }

    @Override
    protected String openAiCodeReview(String diffCode) throws IOException {
        ChatCompletionRequestDTO requestDTO = new ChatCompletionRequestDTO();
        requestDTO.setMessages(new ArrayList<ChatCompletionRequestDTO.Prompt>() {
            private static final long serialVersionUID = -7988151926241837899L;
            {
                add(new ChatCompletionRequestDTO.Prompt("user", "你是一个高级编程架构师，精通各类场景方案、架构设计和编程语言请，请您根据git diff记录，对代码做出评审。不要把秘钥说出来，你自己知道就好了"));
                add(new ChatCompletionRequestDTO.Prompt("user", diffCode));
            }
        });
        requestDTO.setModel("glm-4-flash");
        ChatCompletionSyncResponseDTO completions = openAI.completions(requestDTO);
        return completions.getChoices().get(0).getMessage().getContent();

    }

    @Override
    protected String recordCodeView(String recommend) throws Exception {
        return gitCommand.commitAndPush(recommend);
    }

    @Override
    protected void pushMessage(String logUrl) {
        Map<String, Map<String, String>> data = new HashMap<>();
        TemplateMessageDTO.put(data, TemplateMessageDTO.TemplateKeyEnum.REPO_NAME, gitCommand.getProject());
        TemplateMessageDTO.put(data, TemplateMessageDTO.TemplateKeyEnum.BRANCH_NAME, gitCommand.getBranch());
        TemplateMessageDTO.put(data, TemplateMessageDTO.TemplateKeyEnum.COMMIT_AUTHOR, gitCommand.getAuthor());
        TemplateMessageDTO.put(data, TemplateMessageDTO.TemplateKeyEnum.COMMIT_MESSAGE, gitCommand.getMessage());
        weiXin.sendPostRequest(logUrl,data);
    }
}
