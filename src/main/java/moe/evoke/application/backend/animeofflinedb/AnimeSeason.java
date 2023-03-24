package moe.evoke.application.backend.animeofflinedb;

import com.google.gson.annotations.SerializedName;

public class AnimeSeason {

    @SerializedName("year")
    private int year;

    @SerializedName("season")
    private String season;

    public int getYear() {
        return year;
    }

    public String getSeason() {
        return season;
    }
}