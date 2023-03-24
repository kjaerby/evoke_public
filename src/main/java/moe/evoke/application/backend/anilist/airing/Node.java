package moe.evoke.application.backend.anilist.airing;

import com.google.gson.annotations.SerializedName;

public class Node {

    @SerializedName("siteUrl")
    private String siteUrl;

    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private Title title;

    public String getSiteUrl() {
        return siteUrl;
    }

    public int getId() {
        return id;
    }

    public Title getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return
                "Node{" +
                        "siteUrl = '" + siteUrl + '\'' +
                        ",id = '" + id + '\'' +
                        ",title = '" + title + '\'' +
                        "}";
    }
}