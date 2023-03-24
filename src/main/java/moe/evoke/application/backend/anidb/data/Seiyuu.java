package moe.evoke.application.backend.anidb.data;

import com.google.gson.annotations.SerializedName;

public class Seiyuu {

    @SerializedName("#text")
    private String text;

    @SerializedName("-picture")
    private String picture;

    @SerializedName("-id")
    private String id;

    public String getText() {
        return text;
    }

    public String getPicture() {
        return picture;
    }

    public String getId() {
        return id;
    }
}