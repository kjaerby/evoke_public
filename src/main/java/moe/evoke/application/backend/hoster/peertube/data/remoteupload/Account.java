package moe.evoke.application.backend.hoster.peertube.data.remoteupload;

import com.google.gson.annotations.SerializedName;

public class Account {

    @SerializedName("displayName")
    private String displayName;

    @SerializedName("name")
    private String name;

    @SerializedName("host")
    private String host;

    @SerializedName("id")
    private int id;

    @SerializedName("avatar")
    private Object avatar;

    @SerializedName("url")
    private String url;

    public String getDisplayName() {
        return displayName;
    }

    public String getName() {
        return name;
    }

    public String getHost() {
        return host;
    }

    public int getId() {
        return id;
    }

    public Object getAvatar() {
        return avatar;
    }

    public String getUrl() {
        return url;
    }
}