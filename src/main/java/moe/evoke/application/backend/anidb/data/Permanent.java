package moe.evoke.application.backend.anidb.data;

import com.google.gson.annotations.SerializedName;

public class Permanent {

    @SerializedName("-count")
    private String count;

    @SerializedName("#text")
    private String text;

    public String getCount() {
        return count;
    }

    public String getText() {
        return text;
    }
}