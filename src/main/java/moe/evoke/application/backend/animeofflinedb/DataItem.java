package moe.evoke.application.backend.animeofflinedb;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DataItem {

    @SerializedName("thumbnail")
    private String thumbnail;

    @SerializedName("sources")
    private List<String> sources;

    @SerializedName("synonyms")
    private List<Object> synonyms;

    @SerializedName("animeSeason")
    private AnimeSeason animeSeason;

    @SerializedName("title")
    private String title;

    @SerializedName("type")
    private String type;

    @SerializedName("relations")
    private List<String> relations;

    @SerializedName("episodes")
    private int episodes;

    @SerializedName("picture")
    private String picture;

    @SerializedName("status")
    private String status;

    @SerializedName("tags")
    private List<String> tags;

    public String getThumbnail() {
        return thumbnail;
    }

    public List<String> getSources() {
        return sources;
    }

    public List<Object> getSynonyms() {
        return synonyms;
    }

    public AnimeSeason getAnimeSeason() {
        return animeSeason;
    }

    public String getTitle() {
        return title;
    }

    public String getType() {
        return type;
    }

    public List<String> getRelations() {
        return relations;
    }

    public int getEpisodes() {
        return episodes;
    }

    public String getPicture() {
        return picture;
    }

    public String getStatus() {
        return status;
    }

    public List<String> getTags() {
        return tags;
    }
}