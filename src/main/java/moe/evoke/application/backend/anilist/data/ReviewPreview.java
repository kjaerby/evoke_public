package moe.evoke.application.backend.anilist.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ReviewPreview {

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
                "ReviewPreview{" +
                        "nodes = '" + nodes + '\'' +
                        ",pageInfo = '" + pageInfo + '\'' +
                        "}";
    }
}