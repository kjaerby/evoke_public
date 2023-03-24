package moe.evoke.application.backend.anilist.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Recommendations {

    @SerializedName("nodes")
    private List<NodesItem> nodes;

    @SerializedName("pageInfo")
    private PageInfo pageInfo;

    public List<NodesItem> getNodes() {
        return nodes;
    }

    public PageInfo getPageInfo() {
        return pageInfo;
    }

    @Override
    public String toString() {
        return
                "Recommendations{" +
                        "nodes = '" + nodes + '\'' +
                        ",pageInfo = '" + pageInfo + '\'' +
                        "}";
    }
}