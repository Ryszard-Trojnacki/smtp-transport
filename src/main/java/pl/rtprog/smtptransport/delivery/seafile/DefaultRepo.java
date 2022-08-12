package pl.rtprog.smtptransport.delivery.seafile;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultRepo {
    @JsonProperty("repo_id")
    public String repoId;

    public boolean exists;

    @Override
    public String toString() {
        return "DefaultRepo{" +
                "repoId='" + repoId + '\'' +
                ", exists=" + exists +
                '}';
    }
}
