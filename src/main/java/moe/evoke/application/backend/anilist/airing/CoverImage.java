package moe.evoke.application.backend.anilist.airing;

import com.google.gson.annotations.SerializedName;

public class CoverImage {

    @SerializedName("extraLarge")
    private String extraLarge;

    @SerializedName("color")
    private String color;

    public String getExtraLarge() {
        return extraLarge;
    }

    public String getColor() {
        return color;
    }

    @Override
    public String toString() {
        return
                "CoverImage{" +
                        "extraLarge = '" + extraLarge + '\'' +
                        ",color = '" + color + '\'' +
                        "}";
    }
}