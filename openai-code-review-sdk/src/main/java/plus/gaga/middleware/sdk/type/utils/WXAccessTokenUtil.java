package plus.gaga.middleware.sdk.type.utils;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * 微信获取token的工具类
 */
public class WXAccessTokenUtil {
    private static final String URL_TEMPLATE = "https://api.weixin.qq.com/cgi-bin/token?grant_type=%s&appid=%s&secret=%s";
    private static final String GRANT_TYPE = "client_credential";
    private static final String APPID = "wx53bdca8dd86dd1df";
    private static final String APPSECRET = "b2df97eb82d48828b7a9114bbc67f399";

    public static String getAccessToken() {
        /*
        https请求方式: GET
        https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET
         */
        String urlString = String.format(URL_TEMPLATE, GRANT_TYPE, APPID, APPSECRET);
        try {
            URL url = new URL(urlString);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");

            int responseCode = httpURLConnection.getResponseCode();
            System.out.println("responseCode = " + responseCode);
            if (HttpURLConnection.HTTP_OK == responseCode) {
                // 返回成功根据流输出
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String result = "";
                while ((result = bufferedReader.readLine()) != null) {
                    response.append(result);
                }
                bufferedReader.close();
                System.out.println("response = " + response.toString());
                // 转换token并且返回
                WXToken wxToken = JSON.parseObject(response.toString(), WXToken.class);
                return wxToken.getAccessToken();
            } else {
                System.out.println("GET request failed");
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static class WXToken {
        @JsonProperty("access_token")
        private String accessToken;

        @JsonProperty("expires_in")
        private Integer expiresIn;

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public Integer getExpiresIn() {
            return expiresIn;
        }

        public void setExpiresIn(Integer expiresIn) {
            this.expiresIn = expiresIn;
        }
    }
}