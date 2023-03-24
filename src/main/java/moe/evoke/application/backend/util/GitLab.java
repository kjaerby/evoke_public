package moe.evoke.application.backend.util;

import moe.evoke.application.backend.Config;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.IssuesApi;
import org.gitlab4j.api.models.Issue;

public class GitLab {

    public static void createIssue(String title, String content, String username, String currentPage) {

        GitLabApi gitLabApi = new GitLabApi(Config.getGitLabServer(), Config.getGitLabToken());

        content += "<br>\n\n---\n<br>";
        content += "Page where feedback was created: <br>`" + currentPage + "`";
        content += "<br><br>Created by: `" + username + "`";

        try {
            IssuesApi issuesApi = gitLabApi.getIssuesApi();
            Issue issue = issuesApi.createIssue(Config.getGitLabProjectID(), title, content);


            issuesApi.updateIssue(issue.getProjectId(), issue.getIid(), null,
                    null, null, null,
                    null, "enhancement", null,
                    null, null);

        } catch (GitLabApiException e) {
            e.printStackTrace();
        }


    }
}
