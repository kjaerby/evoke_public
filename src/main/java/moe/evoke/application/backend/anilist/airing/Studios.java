package moe.evoke.application.backend.anilist.airing;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Studios {

    @SerializedName("nodes")
    private List<NodesItem> nodes;

    public List<NodesItem> getNodes() {
        return nodes;
    }

    @Override
    public String toString() {
        return
                "Studios{" +
                        "nodes = '" + nodes + '\'' +
                        "}";
    }
}