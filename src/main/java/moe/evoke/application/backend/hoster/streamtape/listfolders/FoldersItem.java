package moe.evoke.application.backend.hoster.streamtape.listfolders;

import com.google.gson.annotations.SerializedName;

public class FoldersItem {

    @SerializedName("name")
    private String name;

    @SerializedName("id")
    private String id;

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }
}