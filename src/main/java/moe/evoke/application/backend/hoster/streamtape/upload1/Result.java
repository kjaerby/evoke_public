package moe.evoke.application.backend.hoster.streamtape.upload1;

import com.google.gson.annotations.SerializedName;

public class Result {

    @SerializedName("valid_until")
    private String validUntil;

    @SerializedName("url")
    private String url;

    public String getValidUntil() {
        return validUntil;
    }

    public String getUrl() {
        return url;
    }
}