package plus.gaga.middleware.sdk;


import plus.gaga.middleware.sdk.type.utils.CodeReview;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class OpenAiCodeReview {

    public static void main(String[] args) throws Exception {
        System.out.println("\"测试执行\" === " + "测试执行");

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
        String log = CodeReview.codeReview(diffCode.toString());
        System.out.println("评审结果：\n" + log);
    }

}
