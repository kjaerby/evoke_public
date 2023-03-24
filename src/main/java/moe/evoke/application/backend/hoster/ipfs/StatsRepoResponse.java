package moe.evoke.application.backend.hoster.ipfs;

import com.google.gson.annotations.SerializedName;

public class StatsRepoResponse {

    @SerializedName("RepoPath")
    private String repoPath;

    @SerializedName("StorageMax")
    private long storageMax;

    @SerializedName("Version")
    private String version;

    @SerializedName("RepoSize")
    private long repoSize;

    @SerializedName("NumObjects")
    private int numObjects;

    public String getRepoPath() {
        return repoPath;
    }

    public long getStorageMax() {
        return storageMax;
    }

    public String getVersion() {
        return version;
    }

    public long getRepoSize() {
        return repoSize;
    }

    public int getNumObjects() {
        return numObjects;
    }

    @Override
    public String toString() {
        return
                "StatsRepoResponse{" +
                        "repoPath = '" + repoPath + '\'' +
                        ",storageMax = '" + storageMax + '\'' +
                        ",version = '" + version + '\'' +
                        ",repoSize = '" + repoSize + '\'' +
                        ",numObjects = '" + numObjects + '\'' +
                        "}";
    }
}