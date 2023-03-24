package moe.evoke.application.backend.anidb.data;

import com.google.gson.annotations.SerializedName;

public class Ratings {

    @SerializedName("temporary")
    private Temporary temporary;

    @SerializedName("permanent")
    private Permanent permanent;

    public Temporary getTemporary() {
        return temporary;
    }

    public Permanent getPermanent() {
        return permanent;
    }
}