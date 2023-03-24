package moe.evoke.application.backend.anilist.airing;

import com.google.gson.annotations.SerializedName;

public class PageInfo {

    @SerializedName("total")
    private int total;

    @SerializedName("hasNextPage")
    private boolean hasNextPage;

    public int getTotal() {
        return total;
    }

    public boolean isHasNextPage() {
        return hasNextPage;
    }

    @Override
    public String toString() {
        return
                "PageInfo{" +
                        "total = '" + total + '\'' +
                        ",hasNextPage = '" + hasNextPage + '\'' +
                        "}";
    }
}