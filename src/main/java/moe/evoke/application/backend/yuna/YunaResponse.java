package moe.evoke.application.backend.yuna;

import com.google.gson.annotations.SerializedName;

public class YunaResponse {

    @SerializedName("anidb")
    private int anidb;

    @SerializedName("myanimelist")
    private int myanimelist;

    @SerializedName("anilist")
    private int anilist;

    @SerializedName("kitsu")
    private int kitsu;

    public int getAnidb() {
        return anidb;
    }

    public int getMyanimelist() {
        return myanimelist;
    }

    public int getAnilist() {
        return anilist;
    }

    public int getKitsu() {
        return kitsu;
    }
}