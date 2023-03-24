package moe.evoke.application.backend.anidb.data;

import com.google.gson.annotations.SerializedName;

public class TitleItem {

    @SerializedName("#text")
    private String text;

    @SerializedName("-lang")
    private String lang;

    public String getText() {
        return text;
    }

    public String getLang() {
        return lang;
    }
}