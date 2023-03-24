package moe.evoke.application.backend.anilist.data;

import com.google.gson.annotations.SerializedName;

public class ExternalLinksItem {

    @SerializedName("site")
    private String site;

    @SerializedName("url")
    private String url;

    public String getSite() {
        return site;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return
                "ExternalLinksItem{" +
                        "site = '" + site + '\'' +
                        ",url = '" + url + '\'' +
                        "}";
    }
}