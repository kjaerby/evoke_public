package moe.evoke.application.backend.anilist.data;

import com.google.gson.annotations.SerializedName;

public class Trailer {

    @SerializedName("site")
    private String site;

    @SerializedName("id")
    private String id;

    public String getSite() {
        return site;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return
                "Trailer{" +
                        "site = '" + site + '\'' +
                        ",id = '" + id + '\'' +
                        "}";
    }
}