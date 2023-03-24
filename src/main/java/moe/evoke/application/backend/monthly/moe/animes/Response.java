package moe.evoke.application.backend.monthly.moe.animes;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Response {

    @SerializedName("end_date")
    private String endDate;

    @SerializedName("movie")
    private boolean movie;

    @SerializedName("anime_type")
    private String animeType;

    @SerializedName("syoboi_id")
    private int syoboiId;

    @SerializedName("special")
    private boolean special;

    @SerializedName("anilist_ids")
    private List<Integer> anilistIds;

    @SerializedName("related_ids")
    private List<Object> relatedIds;

    @SerializedName("id")
    private int id;

    @SerializedName("anidb_id")
    private int anidbId;

    @SerializedName("main_title")
    private String mainTitle;

    @SerializedName("status")
    private String status;

    @SerializedName("start_date")
    private String startDate;

    @SerializedName("anison_release_ids")
    private List<Integer> anisonReleaseIds;

    public String getEndDate() {
        return endDate;
    }

    public boolean isMovie() {
        return movie;
    }

    public String getAnimeType() {
        return animeType;
    }

    public int getSyoboiId() {
        return syoboiId;
    }

    public boolean isSpecial() {
        return special;
    }

    public List<Integer> getAnilistIds() {
        return anilistIds;
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

    public String getMainTitle() {
        return mainTitle;
    }

    public String getStatus() {
        return status;
    }

    public String getStartDate() {
        return startDate;
    }

    public List<Integer> getAnisonReleaseIds() {
        return anisonReleaseIds;
    }
}