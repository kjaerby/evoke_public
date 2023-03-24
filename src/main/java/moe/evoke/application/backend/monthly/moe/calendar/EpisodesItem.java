package moe.evoke.application.backend.monthly.moe.calendar;

import com.google.gson.annotations.SerializedName;

public class EpisodesItem {

    @SerializedName("anidb")
    private boolean anidb;

    @SerializedName("special")
    private boolean special;

    @SerializedName("number")
    private int number;

    @SerializedName("datetime")
    private String datetime;

    @SerializedName("is_last")
    private boolean isLast;

    @SerializedName("is_first")
    private boolean isFirst;

    @SerializedName("main")
    private boolean main;

    @SerializedName("id")
    private int id;

    @SerializedName("anime_id")
    private int animeId;

    public boolean isAnidb() {
        return anidb;
    }

    public boolean isSpecial() {
        return special;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getDatetime() {
        return datetime;
    }

    public boolean isIsLast() {
        return isLast;
    }

    public boolean isIsFirst() {
        return isFirst;
    }

    public boolean isMain() {
        return main;
    }

    public int getId() {
        return id;
    }

    public int getAnimeId() {
        return animeId;
    }

    @Override
    public String toString() {
        return
                "EpisodesItem{" +
                        "anidb = '" + anidb + '\'' +
                        ",special = '" + special + '\'' +
                        ",number = '" + number + '\'' +
                        ",datetime = '" + datetime + '\'' +
                        ",is_last = '" + isLast + '\'' +
                        ",is_first = '" + isFirst + '\'' +
                        ",main = '" + main + '\'' +
                        ",id = '" + id + '\'' +
                        ",anime_id = '" + animeId + '\'' +
                        "}";
    }
}