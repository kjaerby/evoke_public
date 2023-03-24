package moe.evoke.application.backend.anilist.data;

import com.google.gson.annotations.SerializedName;

public class AnimeData {

    @SerializedName("data")
    private Data data;

    public Data getData() {
        return data;
    }

    @Override
    public String toString() {
        return
                "AnimeData{" +
                        "data = '" + data + '\'' +
                        "}";
    }
}