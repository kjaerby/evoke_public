package moe.evoke.application.backend.anidb.data;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

public class CharacterItem {

    @SerializedName("gender")
    private String gender;

    @SerializedName("-type")
    private String type;

    @SerializedName("-id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("seiyuu")
    private JsonElement seiyuu;

    @SerializedName("charactertype")
    private Charactertype charactertype;

    @SerializedName("-update")
    private String update;

    @SerializedName("picture")
    private String picture;

    @SerializedName("rating")
    private Rating rating;

    public String getGender() {
        return gender;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public JsonElement getSeiyuu() {
        return seiyuu;
    }

    public Charactertype getCharactertype() {
        return charactertype;
    }

    public String getUpdate() {
        return update;
    }

    public String getPicture() {
        return picture;
    }

    public Rating getRating() {
        return rating;
    }
}