package moe.evoke.application.backend.anidb.data;

import com.google.gson.annotations.SerializedName;

public class Epno {

    @SerializedName("#text")
    private String text;

    @SerializedName("-type")
    private String type;

    public String getText() {
        return text;
    }

    public String getType() {
        return type;
    }
}