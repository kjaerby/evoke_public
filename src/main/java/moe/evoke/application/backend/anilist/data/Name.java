package moe.evoke.application.backend.anilist.data;

import com.google.gson.annotations.SerializedName;

public class Name {

    @SerializedName("full")
    private String full;

    public String getFull() {
        return full;
    }

    @Override
    public String toString() {
        return
                "Name{" +
                        "full = '" + full + '\'' +
                        "}";
    }
}