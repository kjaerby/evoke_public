package moe.evoke.application.backend.anidb.data;

import com.google.gson.annotations.SerializedName;

public class Rating {

    @SerializedName("-votes")
    private String votes;

    @SerializedName("#text")
    private String text;

    public String getVotes() {
        return votes;
    }

    public String getText() {
        return text;
    }
}