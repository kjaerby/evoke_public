package moe.evoke.application.backend.anidb.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class EpisodeItem {

    @SerializedName("epno")
    private Epno epno;

    @SerializedName("airdate")
    private String airdate;

    @SerializedName("-id")
    private String id;

    @SerializedName("length")
    private String length;

    @SerializedName("title")
    private List<TitleItem> title;

    @SerializedName("-update")
    private String update;

    @SerializedName("rating")
    private Rating rating;

    public Epno getEpno() {
        return epno;
    }

    public String getAirdate() {
        return airdate;
    }

    public String getId() {
        return id;
    }

    public String getLength() {
        return length;
    }

    public List<TitleItem> getTitle() {
        return title;
    }

    public String getUpdate() {
        return update;
    }

    public Rating getRating() {
        return rating;
    }
}