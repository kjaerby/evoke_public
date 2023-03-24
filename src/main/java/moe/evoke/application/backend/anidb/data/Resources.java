package moe.evoke.application.backend.anidb.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Resources {

    @SerializedName("resource")
    private List<ResourceItem> resource;

    public List<ResourceItem> getResource() {
        return resource;
    }
}