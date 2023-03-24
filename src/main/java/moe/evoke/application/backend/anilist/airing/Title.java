package moe.evoke.application.backend.anilist.airing;

import com.google.gson.annotations.SerializedName;

public class Title {

    @SerializedName("native")
    private String jsonMemberNative;

    @SerializedName("romaji")
    private String romaji;

    @SerializedName("english")
    private String english;

    public String getJsonMemberNative() {
        return jsonMemberNative;
    }

    public String getRomaji() {
        return romaji;
    }

    public String getEnglish() {
        return english;
    }

    @Override
    public String toString() {
        return
                "Title{" +
                        "native = '" + jsonMemberNative + '\'' +
                        ",romaji = '" + romaji + '\'' +
                        ",english = '" + english + '\'' +
                        "}";
    }
}