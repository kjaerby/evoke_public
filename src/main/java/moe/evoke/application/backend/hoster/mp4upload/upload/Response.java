package moe.evoke.application.backend.hoster.mp4upload.upload;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Response {

    @SerializedName("Response")
    private List<ResponseItem> response;

    public List<ResponseItem> getResponse() {
        return response;
    }
}