package moe.evoke.application.backend.hoster.streamtape.listfolders;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Result {

    @SerializedName("folders")
    private List<FoldersItem> folders;

    @SerializedName("files")
    private List<FilesItem> files;

    public List<FoldersItem> getFolders() {
        return folders;
    }

    public List<FilesItem> getFiles() {
        return files;
    }
}