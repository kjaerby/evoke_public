package moe.evoke.application.backend.anilist.data;

import com.google.gson.annotations.SerializedName;

public class PageInfo {

    @SerializedName("total")
    private int total;

    public int getTotal() {
        return total;
    }

    @Override
    public String toString() {
        return
                "PageInfo{" +
                        "total = '" + total + '\'' +
                        "}";
    }
}