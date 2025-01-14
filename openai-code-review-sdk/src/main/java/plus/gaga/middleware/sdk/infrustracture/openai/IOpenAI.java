package plus.gaga.middleware.sdk.infrustracture.openai;

import plus.gaga.middleware.sdk.infrustracture.openai.dto.ChatCompletionRequestDTO;
import plus.gaga.middleware.sdk.infrustracture.openai.dto.ChatCompletionSyncResponseDTO;

import java.io.IOException;

/**
 * 代码评审接口
 */
public interface IOpenAI {

    /**
     * 代码评审方法
     * @param chatCompletionRequestDTO
     * @return
     */
    ChatCompletionSyncResponseDTO completions(ChatCompletionRequestDTO chatCompletionRequestDTO) throws IOException;
}
