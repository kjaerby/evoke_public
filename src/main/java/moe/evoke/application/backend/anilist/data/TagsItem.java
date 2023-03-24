package moe.evoke.application.backend.anilist.data;

import com.google.gson.annotations.SerializedName;

public class TagsItem {

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("rank")
    private int rank;

    @SerializedName("isMediaSpoiler")
    private boolean isMediaSpoiler;

    @SerializedName("id")
    private int id;

    @SerializedName("isGeneralSpoiler")
    private boolean isGeneralSpoiler;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getRank() {
        return rank;
    }

    public boolean isIsMediaSpoiler() {
        return isMediaSpoiler;
    }

    public int getId() {
        return id;
    }

    public boolean isIsGeneralSpoiler() {
        return isGeneralSpoiler;
    }

    @Override
    public String toString() {
        return
                "TagsItem{" +
                        "name = '" + name + '\'' +
                        ",description = '" + description + '\'' +
                        ",rank = '" + rank + '\'' +
                        ",isMediaSpoiler = '" + isMediaSpoiler + '\'' +
                        ",id = '" + id + '\'' +
                        ",isGeneralSpoiler = '" + isGeneralSpoiler + '\'' +
                        "}";
    }
}