package moe.evoke.application.backend.hoster.peertube.data.listvideos;

import com.google.gson.annotations.SerializedName;

public class Category {

    @SerializedName("id")
    private Object id;

    @SerializedName("label")
    private String label;

    public Object getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }
}