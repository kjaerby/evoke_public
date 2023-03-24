package moe.evoke.application.backend.anilist.data;

import com.google.gson.annotations.SerializedName;

public class Avatar {

    @SerializedName("large")
    private String large;

    public String getLarge() {
        return large;
    }

    @Override
    public String toString() {
        return
                "Avatar{" +
                        "large = '" + large + '\'' +
                        "}";
    }
}