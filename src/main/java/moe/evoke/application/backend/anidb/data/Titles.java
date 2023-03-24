package moe.evoke.application.backend.anidb.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Titles {

    @SerializedName("title")
    private List<TitleItem> title;

    public List<TitleItem> getTitle() {
        return title;
    }
}