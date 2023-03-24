package moe.evoke.application.backend.anilist.data;

import com.google.gson.annotations.SerializedName;

public class Data {

    @SerializedName("Media")
    private Media media;

    public Media getMedia() {
        return media;
    }
}