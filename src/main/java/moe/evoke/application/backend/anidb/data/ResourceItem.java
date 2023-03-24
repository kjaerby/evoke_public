package moe.evoke.application.backend.anidb.data;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

public class ResourceItem {

    @SerializedName("-type")
    private String type;

    @SerializedName("externalentity")
    private JsonElement externalentity;

    public String getType() {
        return type;
    }

    public JsonElement getExternalentity() {
        return externalentity;
    }
}