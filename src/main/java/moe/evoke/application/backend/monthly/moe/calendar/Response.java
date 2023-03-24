package moe.evoke.application.backend.monthly.moe.calendar;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Response {

    @SerializedName("max_date")
    private String maxDate;

    @SerializedName("min_date")
    private String minDate;

    @SerializedName("animes")
    private List<AnimesItem> animes;

    @SerializedName("episodes")
    private List<EpisodesItem> episodes;

    @SerializedName("releases")
    private List<ReleasesItem> releases;

    public String getMaxDate() {
        return maxDate;
    }

    public String getMinDate() {
        return minDate;
    }

    public List<AnimesItem> getAnimes() {
        return animes;
    }

    public List<EpisodesItem> getEpisodes() {
        return episodes;
    }

    public List<ReleasesItem> getReleases() {
        return releases;
    }

    @Override
    public String toString() {
        return
                "Response{" +
                        "max_date = '" + maxDate + '\'' +
                        ",min_date = '" + minDate + '\'' +
                        ",animes = '" + animes + '\'' +
                        ",episodes = '" + episodes + '\'' +
                        ",releases = '" + releases + '\'' +
                        "}";
    }
}