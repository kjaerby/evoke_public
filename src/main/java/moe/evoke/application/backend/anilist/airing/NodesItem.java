package moe.evoke.application.backend.anilist.airing;

import com.google.gson.annotations.SerializedName;

public class NodesItem {

    @SerializedName("siteUrl")
    private String siteUrl;

    @SerializedName("name")
    private String name;

    @SerializedName("id")
    private int id;

    public String getSiteUrl() {
        return siteUrl;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return
                "NodesItem{" +
                        "siteUrl = '" + siteUrl + '\'' +
                        ",name = '" + name + '\'' +
                        ",id = '" + id + '\'' +
                        "}";
    }
}