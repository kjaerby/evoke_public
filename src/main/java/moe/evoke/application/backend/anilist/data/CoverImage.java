package moe.evoke.application.backend.anilist.data;

import com.google.gson.annotations.SerializedName;

public class CoverImage {

    @SerializedName("large")
    private String large;

    public String getLarge() {
        return large;
    }

    @Override
    public String toString() {
        return
                "CoverImage{" +
                        "large = '" + large + '\'' +
                        "}";
    }
}