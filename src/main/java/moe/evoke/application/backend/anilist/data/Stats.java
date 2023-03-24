package moe.evoke.application.backend.anilist.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Stats {

    @SerializedName("scoreDistribution")
    private List<ScoreDistributionItem> scoreDistribution;

    @SerializedName("statusDistribution")
    private List<StatusDistributionItem> statusDistribution;

    public List<ScoreDistributionItem> getScoreDistribution() {
        return scoreDistribution;
    }

    public List<StatusDistributionItem> getStatusDistribution() {
        return statusDistribution;
    }

    @Override
    public String toString() {
        return
                "Stats{" +
                        "scoreDistribution = '" + scoreDistribution + '\'' +
                        ",statusDistribution = '" + statusDistribution + '\'' +
                        "}";
    }
}