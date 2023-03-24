package moe.evoke.application.backend.anidb.data;

import com.google.gson.annotations.SerializedName;

public class Anime {

    @SerializedName("episodecount")
    private String episodecount;

    @SerializedName("creators")
    private Creators creators;

    @SerializedName("-id")
    private String id;

    @SerializedName("-restricted")
    private String restricted;

    @SerializedName("description")
    private String description;

    @SerializedName("resources")
    private Resources resources;

    @SerializedName("titles")
    private Titles titles;

    @SerializedName("type")
    private String type;

    @SerializedName("startdate")
    private String startdate;

    @SerializedName("url")
    private String url;

    @SerializedName("picture")
    private String picture;

    @SerializedName("characters")
    private Characters characters;

    @SerializedName("ratings")
    private Ratings ratings;

    @SerializedName("episodes")
    private Episodes episodes;

    public String getEpisodecount() {
        return episodecount;
    }

    public Creators getCreators() {
        return creators;
    }

    public String getId() {
        return id;
    }

    public String getRestricted() {
        return restricted;
    }

    public String getDescription() {
        return description;
    }

    public Resources getResources() {
        return resources;
    }

    public Titles getTitles() {
        return titles;
    }

    public String getType() {
        return type;
    }

    public String getStartdate() {
        return startdate;
    }

    public String getUrl() {
        return url;
    }

    public String getPicture() {
        return picture;
    }

    public Characters getCharacters() {
        return characters;
    }

    public Ratings getRatings() {
        return ratings;
    }

    public Episodes getEpisodes() {
        return episodes;
    }
}