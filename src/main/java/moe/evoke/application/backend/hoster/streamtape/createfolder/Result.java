package moe.evoke.application.backend.hoster.streamtape.createfolder;

import com.google.gson.annotations.SerializedName;

public class Result {

    @SerializedName("folderid")
    private String folderid;

    public String getFolderid() {
        return folderid;
    }
}