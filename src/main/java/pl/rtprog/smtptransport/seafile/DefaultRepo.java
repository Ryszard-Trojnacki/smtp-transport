package pl.rtprog.smtptransport.seafile;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DefaultRepo {
    @JsonProperty("repo_id")
    public String repoId;

    public boolean exists;
}
