package moe.evoke.application.backend.anilist.data;

import com.google.gson.annotations.SerializedName;

public class RankingsItem {

    @SerializedName("year")
    private Object year;

    @SerializedName("format")
    private String format;

    @SerializedName("context")
    private String context;

    @SerializedName("rank")
    private int rank;

    @SerializedName("season")
    private Object season;

    @SerializedName("id")
    private int id;

    @SerializedName("type")
    private String type;

    @SerializedName("allTime")
    private boolean allTime;

    public Object getYear() {
        return year;
    }

    public String getFormat() {
        return format;
    }

    public String getContext() {
        return context;
    }

    public int getRank() {
        return rank;
    }

    public Object getSeason() {
        return season;
    }

    public int getId() {
        return id;
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
                        "year = '" + year + '\'' +
                        ",format = '" + format + '\'' +
                        ",context = '" + context + '\'' +
                        ",rank = '" + rank + '\'' +
                        ",season = '" + season + '\'' +
                        ",id = '" + id + '\'' +
                        ",type = '" + type + '\'' +
                        ",allTime = '" + allTime + '\'' +
                        "}";
    }
}