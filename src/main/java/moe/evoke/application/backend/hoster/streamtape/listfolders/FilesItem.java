package moe.evoke.application.backend.hoster.streamtape.listfolders;

import com.google.gson.annotations.SerializedName;

public class FilesItem {

    @SerializedName("linkid")
    private String linkid;

    @SerializedName("size")
    private int size;

    @SerializedName("downloads")
    private int downloads;

    @SerializedName("name")
    private String name;

    @SerializedName("link")
    private String link;

    @SerializedName("created_at")
    private int createdAt;

    @SerializedName("convert")
    private String convert;

    public String getLinkid() {
        return linkid;
    }

    public int getSize() {
        return size;
    }

    public int getDownloads() {
        return downloads;
    }

    public String getName() {
        return name;
    }

    public String getLink() {
        return link;
    }

    public int getCreatedAt() {
        return createdAt;
    }

    public String getConvert() {
        return convert;
    }
}