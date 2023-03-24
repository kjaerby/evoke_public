package moe.evoke.application.backend.animeofflinedb;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class OfflineAnime {

    @SerializedName("data")
    private List<DataItem> data;

    public List<DataItem> getData() {
        return data;
    }
}