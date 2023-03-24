package moe.evoke.application.backend.anilist.airing;

import com.google.gson.annotations.SerializedName;

public class Trailer {

    @SerializedName("site")
    private String site;

    @SerializedName("thumbnail")
    private String thumbnail;

    @SerializedName("id")
    private String id;

    public String getSite() {
        return site;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return
                "Trailer{" +
                        "site = '" + site + '\'' +
                        ",thumbnail = '" + thumbnail + '\'' +
                        ",id = '" + id + '\'' +
                        "}";
    }
}