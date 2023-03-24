package moe.evoke.application.backend.anilist.data;

import com.google.gson.annotations.SerializedName;

public class ScoreDistributionItem {

    @SerializedName("score")
    private int score;

    @SerializedName("amount")
    private int amount;

    public int getScore() {
        return score;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return
                "ScoreDistributionItem{" +
                        "score = '" + score + '\'' +
                        ",amount = '" + amount + '\'' +
                        "}";
    }
}