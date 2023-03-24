package moe.evoke.application.backend.anidb.data;

import com.google.gson.annotations.SerializedName;

public class NameItem {

    @SerializedName("#text")
    private String text;

    @SerializedName("-type")
    private String type;

    @SerializedName("-id")
    private String id;

    public String getText() {
        return text;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }
}