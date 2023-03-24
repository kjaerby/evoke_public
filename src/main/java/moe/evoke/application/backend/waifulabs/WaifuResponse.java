package moe.evoke.application.backend.waifulabs;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WaifuResponse {

    @SerializedName("newGirls")
    private List<NewGirlsItem> newGirls;

    public List<NewGirlsItem> getNewGirls() {
        return newGirls;
    }
}