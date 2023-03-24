package moe.evoke.application.backend.hoster.streamtape.upload2;

import com.google.gson.annotations.SerializedName;

public class Result {

    @SerializedName("sha256")
    private String sha256;

    @SerializedName("size")
    private String size;

    @SerializedName("content_type")
    private String contentType;

    @SerializedName("name")
    private String name;

    @SerializedName("id")
    private String id;

    @SerializedName("url")
    private String url;

    public String getSha256() {
        return sha256;
    }

    public String getSize() {
        return size;
    }

    public String getContentType() {
        return contentType;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }
}