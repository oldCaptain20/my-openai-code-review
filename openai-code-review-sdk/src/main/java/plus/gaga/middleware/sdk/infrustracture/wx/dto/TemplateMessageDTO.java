package plus.gaga.middleware.sdk.infrustracture.wx.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

public class TemplateMessageDTO {
    /**
     * 发送给谁
     */
    private String touser = "oL9bm6-SHx8NMHalagxNWFYm0otA";

    /**
     * 消息模版id
     */
    @JsonProperty("template_id")
    private String templateId = "HbPeJURq4SEWuvinXLYhH0rltEqdxZ8oY29jmgUVaXM";

    /**
     * 设置要点击卡片跳转的url路径
     */
    @JsonProperty("url")
    private String jumpTargeUrl = "https://github.com/oldCaptain20/my-openai-code-review-log/blob/main/2025-01-11/2aaTe9ajGyFv.md";

    /**
     * 消息体
     */
    private Map<String, Map<String, String>> data = new HashMap<>();

    public static void put(Map<String, Map<String, String>> data, TemplateKeyEnum keyEnum, String value) {
        data.put(keyEnum.code, new HashMap<String, String>() {
            {
                // 给值赋值，key默认叫value，这是微信规定的
                put("value", value);
            }
        });
    }

    public TemplateMessageDTO(String touser, String templateId) {
        this.touser = touser;
        this.templateId = templateId;
    }

    /**
     * 添加方法
     *
     * @param key
     * @param value
     */
    public void put(String key, String value) {
        data.put(key, new HashMap<String, String>() {
            {
                put("value", value);
            }
        });
    }

    public String getTouser() {
        return touser;
    }

    public void setTouser(String touser) {
        this.touser = touser;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getJumpTargeUrl() {
        return jumpTargeUrl;
    }

    public void setJumpTargeUrl(String jumpTargeUrl) {
        this.jumpTargeUrl = jumpTargeUrl;
    }

    public Map<String, Map<String, String>> getData() {
        return data;
    }

    public void setData(Map<String, Map<String, String>> data) {
        this.data = data;
    }

    public enum TemplateKeyEnum {
        REPO_NAME("repo_name", "项目名称"), BRANCH_NAME("branch_name", "代码分支"), COMMIT_AUTHOR("commit_author", "开发作者"), COMMIT_MESSAGE("commit_message", "提交说明"),
        ;

        private String code;
        private String description;

        TemplateKeyEnum(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }
}
