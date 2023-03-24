package moe.evoke.application.backend.anilist.malid;

import com.google.gson.annotations.SerializedName;

public class Data {

    @SerializedName("Media")
    private Media media;

    public Media getMedia() {
        return media;
    }
}