package moe.evoke.application.backend.anilist.airing;

import com.google.gson.annotations.SerializedName;

public class Data {

    @SerializedName("Page")
    private Page page;

    public Page getPage() {
        return page;
    }

    @Override
    public String toString() {
        return
                "Data{" +
                        "page = '" + page + '\'' +
                        "}";
    }
}