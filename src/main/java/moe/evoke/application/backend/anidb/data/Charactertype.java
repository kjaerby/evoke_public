package moe.evoke.application.backend.anidb.data;

import com.google.gson.annotations.SerializedName;

public class Charactertype {

    @SerializedName("#text")
    private String text;

    @SerializedName("-id")
    private String id;

    public String getText() {
        return text;
    }

    public String getId() {
        return id;
    }
}