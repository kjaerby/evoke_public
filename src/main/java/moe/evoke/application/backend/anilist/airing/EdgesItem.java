package moe.evoke.application.backend.anilist.airing;

import com.google.gson.annotations.SerializedName;

public class EdgesItem {

    @SerializedName("relationType")
    private String relationType;

    @SerializedName("node")
    private Node node;

    public String getRelationType() {
        return relationType;
    }

    public Node getNode() {
        return node;
    }

    @Override
    public String toString() {
        return
                "EdgesItem{" +
                        "relationType = '" + relationType + '\'' +
                        ",node = '" + node + '\'' +
                        "}";
    }
}