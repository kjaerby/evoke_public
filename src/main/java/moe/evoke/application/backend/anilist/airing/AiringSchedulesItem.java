package moe.evoke.application.backend.anilist.airing;

import com.google.gson.annotations.SerializedName;

public class AiringSchedulesItem {

    @SerializedName("airingAt")
    private int airingAt;

    @SerializedName("episode")
    private int episode;

    @SerializedName("id")
    private int id;

    @SerializedName("media")
    private Media media;

    public int getAiringAt() {
        return airingAt;
    }

    public int getEpisode() {
        return episode;
    }

    public int getId() {
        return id;
    }

    public Media getMedia() {
        return media;
    }

    @Override
    public String toString() {
        return
                "AiringSchedulesItem{" +
                        "airingAt = '" + airingAt + '\'' +
                        ",episode = '" + episode + '\'' +
                        ",id = '" + id + '\'' +
                        ",media = '" + media + '\'' +
                        "}";
    }
}