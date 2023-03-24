package moe.evoke.application.backend.hoster.peertube.data.listvideos;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Response {

    @SerializedName("total")
    private int total;

    @SerializedName("data")
    private List<DataItem> data;

    public int getTotal() {
        return total;
    }

    public List<DataItem> getData() {
        return data;
    }
}