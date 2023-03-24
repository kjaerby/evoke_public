package moe.evoke.application.backend.anilist.data;

import com.google.gson.annotations.SerializedName;

public class VoiceActorsItem {

    @SerializedName("image")
    private Image image;

    @SerializedName("name")
    private Name name;

    @SerializedName("language")
    private String language;

    @SerializedName("id")
    private int id;

    public Image getImage() {
        return image;
    }

    public Name getName() {
        return name;
    }

    public String getLanguage() {
        return language;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return
                "VoiceActorsItem{" +
                        "image = '" + image + '\'' +
                        ",name = '" + name + '\'' +
                        ",language = '" + language + '\'' +
                        ",id = '" + id + '\'' +
                        "}";
    }
}