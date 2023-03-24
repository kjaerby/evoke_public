package moe.evoke.application.backend.anilist.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class StaffPreview {

    @SerializedName("edges")
    private List<EdgesItem> edges;

    public List<EdgesItem> getEdges() {
        return edges;
    }

    @Override
    public String toString() {
        return
                "StaffPreview{" +
                        "edges = '" + edges + '\'' +
                        "}";
    }
}