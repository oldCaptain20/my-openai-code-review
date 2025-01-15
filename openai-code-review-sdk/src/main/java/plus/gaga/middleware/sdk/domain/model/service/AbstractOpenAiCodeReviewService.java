package plus.gaga.middleware.sdk.domain.model.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import plus.gaga.middleware.sdk.infrustracture.git.GitCommand;
import plus.gaga.middleware.sdk.infrustracture.openai.IOpenAI;
import plus.gaga.middleware.sdk.infrustracture.wx.WeiXin;

import java.io.IOException;

/**
 * 抽象类实现接口，定义一些列抽象方法，使用模版方法设计模式
 */
public abstract class AbstractOpenAiCodeReviewService implements IOpenAiCodeReviewService {
    private final Logger logger = LoggerFactory.getLogger(AbstractOpenAiCodeReviewService.class);

    protected final GitCommand gitCommand;
    protected final IOpenAI openAI;
    protected final WeiXin weiXin;

    public AbstractOpenAiCodeReviewService(GitCommand gitCommand, IOpenAI openAI, WeiXin weiXin) {
        this.gitCommand = gitCommand;
        this.openAI = openAI;
        this.weiXin = weiXin;
    }

    @Override
    public void exec() {
        try {
            logger.info("1、获取提交代码的差异");
            String diffCode = getDiffCode();

            logger.info("2、交给ai评审代码");
            String recommend = openAiCodeReview(diffCode);

            logger.info("3、记录评审结果，返回日志地址");
            String logUrl = recordCodeView(recommend);

            logger.info("4、发送消息通知：日志地址，通知内容");
            pushMessage(logUrl);
        } catch (Exception e) {
            logger.error("openai-code-review error", e);
        }
    }

    /**
     * 获取版本提交差异
     *
     * @return 差异代码
     */
    protected abstract String getDiffCode() throws IOException, InterruptedException;

    /**
     * 代码评审
     *
     * @param diffCode 差异代码
     * @return 返回评审建议
     */
    protected abstract String openAiCodeReview(String diffCode) throws IOException;


    /**
     * 记录代码评审，写入到日志仓库
     *
     * @param recommend 评审意见
     * @return 返回日志仓库的url
     */
    protected abstract String recordCodeView(String recommend) throws Exception;

    /**
     * 推送消息给微信公众号
     *
     * @param logUrl 日志仓库的url
     */
    protected abstract void pushMessage(String logUrl);


}
