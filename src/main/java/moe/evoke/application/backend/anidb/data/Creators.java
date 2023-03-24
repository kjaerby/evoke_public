package moe.evoke.application.backend.anidb.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Creators {

    @SerializedName("name")
    private List<NameItem> name;

    public List<NameItem> getName() {
        return name;
    }
}