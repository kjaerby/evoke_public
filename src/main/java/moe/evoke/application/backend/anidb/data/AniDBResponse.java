package moe.evoke.application.backend.anidb.data;

import com.google.gson.annotations.SerializedName;

public class AniDBResponse {

    @SerializedName("anime")
    private Anime anime;

    public Anime getAnime() {
        return anime;
    }
}