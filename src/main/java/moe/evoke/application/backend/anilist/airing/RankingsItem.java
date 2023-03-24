package moe.evoke.application.backend.anilist.airing;

import com.google.gson.annotations.SerializedName;

public class RankingsItem {

    @SerializedName("rank")
    private int rank;

    @SerializedName("season")
    private Object season;

    @SerializedName("type")
    private String type;

    @SerializedName("allTime")
    private boolean allTime;

    public int getRank() {
        return rank;
    }

    public Object getSeason() {
        return season;
    }

    public String getType() {
        return type;
    }

    public boolean isAllTime() {
        return allTime;
    }

    @Override
    public String toString() {
        return
                "RankingsItem{" +
                        "rank = '" + rank + '\'' +
                        ",season = '" + season + '\'' +
                        ",type = '" + type + '\'' +
                        ",allTime = '" + allTime + '\'' +
                        "}";
    }
}