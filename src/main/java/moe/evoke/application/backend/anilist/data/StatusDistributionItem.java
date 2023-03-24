package moe.evoke.application.backend.anilist.data;

import com.google.gson.annotations.SerializedName;

public class StatusDistributionItem {

    @SerializedName("amount")
    private int amount;

    @SerializedName("status")
    private String status;

    public int getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return
                "StatusDistributionItem{" +
                        "amount = '" + amount + '\'' +
                        ",status = '" + status + '\'' +
                        "}";
    }
}