package moe.evoke.application.backend.hoster.peertube.data.remoteupload;

import com.google.gson.annotations.SerializedName;

public class Response {

    @SerializedName("torrentName")
    private Object torrentName;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("magnetUri")
    private Object magnetUri;

    @SerializedName("id")
    private int id;

    @SerializedName("state")
    private State state;

    @SerializedName("video")
    private Video video;

    @SerializedName("error")
    private Object error;

    @SerializedName("targetUrl")
    private String targetUrl;

    @SerializedName("updatedAt")
    private String updatedAt;

    public Object getTorrentName() {
        return torrentName;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public Object getMagnetUri() {
        return magnetUri;
    }

    public int getId() {
        return id;
    }

    public State getState() {
        return state;
    }

    public Video getVideo() {
        return video;
    }

    public Object getError() {
        return error;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}