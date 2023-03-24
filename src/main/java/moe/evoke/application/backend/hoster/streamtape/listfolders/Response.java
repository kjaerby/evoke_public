package moe.evoke.application.backend.hoster.streamtape.listfolders;

import com.google.gson.annotations.SerializedName;

public class Response {

    @SerializedName("msg")
    private String msg;

    @SerializedName("result")
    private Result result;

    @SerializedName("status")
    private int status;

    public String getMsg() {
        return msg;
    }

    public Result getResult() {
        return result;
    }

    public int getStatus() {
        return status;
    }
}