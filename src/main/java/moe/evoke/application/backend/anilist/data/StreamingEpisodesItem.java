package moe.evoke.application.backend.anilist.data;

import com.google.gson.annotations.SerializedName;

public class StreamingEpisodesItem {

    @SerializedName("site")
    private String site;

    @SerializedName("thumbnail")
    private String thumbnail;

    @SerializedName("title")
    private String title;

    @SerializedName("url")
    private String url;

    public String getSite() {
        return site;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return
                "StreamingEpisodesItem{" +
                        "site = '" + site + '\'' +
                        ",thumbnail = '" + thumbnail + '\'' +
                        ",title = '" + title + '\'' +
                        ",url = '" + url + '\'' +
                        "}";
    }
}