package moe.evoke.application.backend.anidb.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Episodes {

    @SerializedName("episode")
    private List<EpisodeItem> episode;

    public List<EpisodeItem> getEpisode() {
        return episode;
    }
}