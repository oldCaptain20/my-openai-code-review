package plus.gaga.middleware.sdk.infrustracture.wx;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import plus.gaga.middleware.sdk.infrustracture.wx.dto.TemplateMessageDTO;
import plus.gaga.middleware.sdk.type.utils.WXAccessTokenUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Scanner;

/**
 * 这个类用于发送模版消息
 */
public class WeiXin {

    private final Logger logger = LoggerFactory.getLogger(WeiXin.class);
    private final String appid;
    private final String secret;

    /**
     * 发送给谁
     */
    private final String touser;

    /**
     * 消息模版id
     */
    @JsonProperty("template_id")
    private String templateId = "";

    /**
     * 微信模版方法接口调用地址
     */
    private String templateInvokeUrl = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=%s";

    /**
     * 设置要点击卡片跳转的url路径
     */
    @JsonProperty("url")
    private String jumpTargetUrl = "";

    public WeiXin(String appid, String secret, String touser, String templateId) {
        this.appid = appid;
        this.secret = secret;
        this.touser = touser;
        this.templateId = templateId;
    }

    /**
     * POST请求
     * <p>
     *
     * @param
     * @throws IOException
     */
    public void sendPostRequest(String jumpTargetUrl, Map<String, Map<String, String>> data) {
        try {
            TemplateMessageDTO templateMessageDTO = new TemplateMessageDTO(touser, templateId);
            templateMessageDTO.setData(data);
            templateMessageDTO.setJumpTargeUrl(jumpTargetUrl);
            String accessToken = WXAccessTokenUtil.getAccessToken(appid, secret);
            logger.error("微信的接口访问accessToken: {}", accessToken);
            String url = String.format(templateInvokeUrl, accessToken);
            logger.error("url: {}", url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
            httpURLConnection.setRequestMethod("POST");
            // 必须设置为true 不然不能写入到请求体中
            httpURLConnection.setDoOutput(true);
            // 将body写入到请求中
            try (OutputStream outputStream = httpURLConnection.getOutputStream()) {
                byte[] bytes = JSON.toJSONString(templateMessageDTO).getBytes(StandardCharsets.UTF_8);
                outputStream.write(bytes, 0, bytes.length);
            }
            // 打印响应内容
            try (Scanner scanner = new Scanner(httpURLConnection.getInputStream(), StandardCharsets.UTF_8.name())) {
                String response = scanner.useDelimiter("\\A").next();
                System.out.println("response = " + response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
