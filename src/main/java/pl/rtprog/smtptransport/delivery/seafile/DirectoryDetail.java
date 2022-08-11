package pl.rtprog.smtptransport.delivery.seafile;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DirectoryDetail {
    @JsonProperty("repo_id")
    public String repoId;

    public String name;

    public String mtime;

    public String path;
}
