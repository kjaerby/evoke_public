package moe.evoke.application.backend.hoster.peertube.data.remoteupload;

import com.google.gson.annotations.SerializedName;

public class Language {

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