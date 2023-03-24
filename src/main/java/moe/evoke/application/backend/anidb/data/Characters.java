package moe.evoke.application.backend.anidb.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Characters {

    @SerializedName("character")
    private List<CharacterItem> character;

    public List<CharacterItem> getCharacter() {
        return character;
    }
}