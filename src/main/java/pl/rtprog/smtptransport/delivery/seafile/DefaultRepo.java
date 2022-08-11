package pl.rtprog.smtptransport.delivery.seafile;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DefaultRepo {
    @JsonProperty("repo_id")
    public String repoId;

    public boolean exists;
}
