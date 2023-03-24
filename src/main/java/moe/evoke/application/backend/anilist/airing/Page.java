package moe.evoke.application.backend.anilist.airing;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Page {

    @SerializedName("pageInfo")
    private PageInfo pageInfo;

    @SerializedName("airingSchedules")
    private List<AiringSchedulesItem> airingSchedules;

    public PageInfo getPageInfo() {
        return pageInfo;
    }

    public List<AiringSchedulesItem> getAiringSchedules() {
        return airingSchedules;
    }

    @Override
    public String toString() {
        return
                "Page{" +
                        "pageInfo = '" + pageInfo + '\'' +
                        ",airingSchedules = '" + airingSchedules + '\'' +
                        "}";
    }
}