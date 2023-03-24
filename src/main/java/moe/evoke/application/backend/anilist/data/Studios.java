package moe.evoke.application.backend.anilist.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Studios {

    @SerializedName("edges")
    private List<EdgesItem> edges;

    public List<EdgesItem> getEdges() {
        return edges;
    }

    @Override
    public String toString() {
        return
                "Studios{" +
                        "edges = '" + edges + '\'' +
                        "}";
    }
}