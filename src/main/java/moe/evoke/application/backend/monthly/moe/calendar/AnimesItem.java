package moe.evoke.application.backend.monthly.moe.calendar;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AnimesItem {

    @SerializedName("special")
    private boolean special;

    @SerializedName("end_date")
    private Object endDate;

    @SerializedName("movie")
    private boolean movie;

    @SerializedName("related_ids")
    private List<Object> relatedIds;

    @SerializedName("id")
    private int id;

    @SerializedName("anidb_id")
    private int anidbId;

    @SerializedName("anime_type")
    private String animeType;

    @SerializedName("main_title")
    private String mainTitle;

    @SerializedName("syoboi_id")
    private int syoboiId;

    @SerializedName("status")
    private String status;

    @SerializedName("start_date")
    private String startDate;

    @SerializedName("anison_release_ids")
    private List<Object> anisonReleaseIds;

    public boolean isSpecial() {
        return special;
    }

    public Object getEndDate() {
        return endDate;
    }

    public boolean isMovie() {
        return movie;
    }

    public List<Object> getRelatedIds() {
        return relatedIds;
    }

    public int getId() {
        return id;
    }

    public int getAnidbId() {
        return anidbId;
    }

    public String getAnimeType() {
        return animeType;
    }

    public String getMainTitle() {
        return mainTitle;
    }

    public int getSyoboiId() {
        return syoboiId;
    }

    public String getStatus() {
        return status;
    }

    public String getStartDate() {
        return startDate;
    }

    public List<Object> getAnisonReleaseIds() {
        return anisonReleaseIds;
    }

    @Override
    public String toString() {
        return
                "AnimesItem{" +
                        "special = '" + special + '\'' +
                        ",end_date = '" + endDate + '\'' +
                        ",movie = '" + movie + '\'' +
                        ",related_ids = '" + relatedIds + '\'' +
                        ",id = '" + id + '\'' +
                        ",anidb_id = '" + anidbId + '\'' +
                        ",anime_type = '" + animeType + '\'' +
                        ",main_title = '" + mainTitle + '\'' +
                        ",syoboi_id = '" + syoboiId + '\'' +
                        ",status = '" + status + '\'' +
                        ",start_date = '" + startDate + '\'' +
                        ",anison_release_ids = '" + anisonReleaseIds + '\'' +
                        "}";
    }
}