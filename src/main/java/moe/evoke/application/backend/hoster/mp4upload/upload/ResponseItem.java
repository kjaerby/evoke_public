package moe.evoke.application.backend.hoster.mp4upload.upload;

import com.google.gson.annotations.SerializedName;

public class ResponseItem {

    @SerializedName("file_code")
    private String fileCode;

    @SerializedName("file_status")
    private String fileStatus;

    public String getFileCode() {
        return fileCode;
    }

    public String getFileStatus() {
        return fileStatus;
    }
}