package moe.evoke.application.backend.hoster.peertube.data.remoteupload;

import com.google.gson.annotations.SerializedName;

public class State {

    @SerializedName("id")
    private int id;

    @SerializedName("label")
    private String label;

    public int getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }
}