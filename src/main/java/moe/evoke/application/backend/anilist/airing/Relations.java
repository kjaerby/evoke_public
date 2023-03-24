package moe.evoke.application.backend.anilist.airing;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Relations {

    @SerializedName("edges")
    private List<EdgesItem> edges;

    public List<EdgesItem> getEdges() {
        return edges;
    }

    @Override
    public String toString() {
        return
                "Relations{" +
                        "edges = '" + edges + '\'' +
                        "}";
    }
}