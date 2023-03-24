package moe.evoke.application.backend.anilist.malid;

import com.google.gson.annotations.SerializedName;

public class AnilistMal {

    @SerializedName("data")
    private Data data;

    public Data getData() {
        return data;
    }
}