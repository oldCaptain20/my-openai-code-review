package plus.gaga.middleware.sdk.infrustracture.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import plus.gaga.middleware.sdk.type.utils.RandomUtil;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;


public class GitCommand {

    private final Logger logger = LoggerFactory.getLogger(GitCommand.class);

    /**
     * 日志url地址
     */
    private final String githubReviewLogUri;

    /**
     * 连接日志仓库的token
     */
    private final String githubToken;

    /**
     * 模版消息填充的四个属性
     */
    private final String project;

    private final String branch;

    private final String author;

    private final String message;

    public String getProject() {
        return project;
    }

    public String getBranch() {
        return branch;
    }

    public String getAuthor() {
        return author;
    }

    public String getMessage() {
        return message;
    }

    public GitCommand(String githubReviewLogUri, String githubToken, String project, String branch, String author, String message) {
        this.githubReviewLogUri = githubReviewLogUri;
        this.githubToken = githubToken;
        this.project = project;
        this.branch = branch;
        this.author = author;
        this.message = message;
    }

    /**
     * 获取差异 提交给openaii进行代码评审
     *
     * @return 评审日志
     * @throws IOException
     * @throws InterruptedException
     */
    public String diff() throws IOException, InterruptedException {
        // 获取最近一个提交版本的 hash值
        ProcessBuilder processBuilder = new ProcessBuilder("git", "log", "-1", "--pretty=format:%H");
        processBuilder.directory(new File("."));
        Process logResponse = processBuilder.start();
        // 解析结果
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(logResponse.getInputStream()));
        // 直接一行获取，本来返回的也是一行数据
        String lastCommitHash = bufferedReader.readLine();
        logger.info("上一个版本的hash：{}", lastCommitHash);

        ProcessBuilder diffProcessBuilder = new ProcessBuilder("git", "diff", lastCommitHash + "^ ", lastCommitHash);
        diffProcessBuilder.directory(new File("."));
        Process diffResponse = diffProcessBuilder.start();
        BufferedReader diffReader = new BufferedReader(new InputStreamReader(diffResponse.getInputStream()));
        StringBuilder diffContext = new StringBuilder();
        String line = "";
        int statusCode = diffResponse.waitFor();
        if (statusCode != 0) {
            throw new RuntimeException("Failed to get diff, exit code:" + statusCode);
        }

        while ((line = diffReader.readLine()) != null) {
            diffContext.append(line).append("\n");
        }
        diffReader.close();
        return diffContext.toString();
    }

    public String commitAndPush(String reviewLog) throws Exception {
        UsernamePasswordCredentialsProvider credentials = new UsernamePasswordCredentialsProvider(githubToken, "");
        Git git = Git.cloneRepository()
                .setURI(githubReviewLogUri + "git")
                .setDirectory(new File("repo"))
                .setCredentialsProvider(credentials)
                .call();

        String date = LocalDate.now().toString();
        File dateFolder = new File("repo/" + date);
        if (!dateFolder.exists()) {
            dateFolder.mkdirs();
        }
        // 日志文件名称 md文件由，项目-分支-作者-时间-5位流水号组成
        String logFileName = project + "-" + branch + "-" + author + LocalDateTime.now() + RandomUtil.generateRandomString(5) + ".md";
        // 在日期文件夹下创建一个新文件，将日志文件写入到新文件中
        File newFile = new File(dateFolder, logFileName);
        try(FileWriter fileWriter=new FileWriter(newFile)){
            fileWriter.write(reviewLog);
        }

        // 提交内容
        git.add().addFilepattern(dateFolder + "/").call();
        git.commit().setMessage("add code review new file" + logFileName).call();
        git.push().setCredentialsProvider(credentials).call();

        logger.info("openai-code-review git commit and push done!{}", dateFolder);
        // 注意这里必须要写 /blob/master/ github规定的
        return githubReviewLogUri + "/blob/master/" + date + "/" + logFileName;
    }


}
