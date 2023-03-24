package moe.evoke.application.backend.anilist.data;

import com.google.gson.annotations.SerializedName;

public class EdgesItem {

    @SerializedName("relationType")
    private String relationType;

    @SerializedName("node")
    private Node node;

    @SerializedName("id")
    private int id;

    public String getRelationType() {
        return relationType;
    }

    public Node getNode() {
        return node;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return
                "EdgesItem{" +
                        "relationType = '" + relationType + '\'' +
                        ",node = '" + node + '\'' +
                        ",id = '" + id + '\'' +
                        "}";
    }
}