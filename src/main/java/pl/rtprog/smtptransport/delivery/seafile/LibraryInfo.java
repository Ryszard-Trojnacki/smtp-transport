package pl.rtprog.smtptransport.delivery.seafile;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LibraryInfo {
    @JsonProperty("owner_contact_email")
    public String ownerContactEmail;

    @JsonProperty("owner_name")
    public String ownerName;

    public String owner;

    public String name;

    public String id;

    public String permission;

    public boolean virtual;

    public long mtime;

    public boolean encrypted;

    public int version;

    public String root;

    /**
     * 'repo': libraries owned by myself.
     * 'srepo': libraries shared to me.
     * 'grepo': group libraries or shared-with-all libraries.
     */
    public String type;

    public String salt;

    public long size;

    @Override
    public String toString() {
        return "LibraryInfo{" +
                "ownerContactEmail='" + ownerContactEmail + '\'' +
                ", ownerName='" + ownerName + '\'' +
                ", owner='" + owner + '\'' +
                ", name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", permission='" + permission + '\'' +
                ", virtual=" + virtual +
                ", mtime=" + mtime +
                ", encrypted=" + encrypted +
                ", version=" + version +
                ", root='" + root + '\'' +
                ", type='" + type + '\'' +
                ", salt='" + salt + '\'' +
                ", size=" + size +
                '}';
    }
}
