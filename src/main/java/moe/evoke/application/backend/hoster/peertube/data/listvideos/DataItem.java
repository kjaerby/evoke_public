package moe.evoke.application.backend.hoster.peertube.data.listvideos;

import com.google.gson.annotations.SerializedName;

public class DataItem {

    @SerializedName("waitTranscoding")
    private boolean waitTranscoding;

    @SerializedName("channel")
    private Channel channel;

    @SerializedName("privacy")
    private Privacy privacy;

    @SerializedName("description")
    private Object description;

    @SerializedName("language")
    private Language language;

    @SerializedName("previewPath")
    private String previewPath;

    @SerializedName("blacklistedReason")
    private Object blacklistedReason;

    @SerializedName("uuid")
    private String uuid;

    @SerializedName("duration")
    private int duration;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("originallyPublishedAt")
    private Object originallyPublishedAt;

    @SerializedName("isLive")
    private boolean isLive;

    @SerializedName("id")
    private int id;

    @SerializedName("state")
    private State state;

    @SerializedName("embedPath")
    private String embedPath;

    @SerializedName("views")
    private int views;

    @SerializedName("likes")
    private int likes;

    @SerializedName("updatedAt")
    private String updatedAt;

    @SerializedName("licence")
    private Licence licence;

    @SerializedName("nsfw")
    private boolean nsfw;

    @SerializedName("publishedAt")
    private String publishedAt;

    @SerializedName("dislikes")
    private int dislikes;

    @SerializedName("isLocal")
    private boolean isLocal;

    @SerializedName("blacklisted")
    private boolean blacklisted;

    @SerializedName("name")
    private String name;

    @SerializedName("thumbnailPath")
    private String thumbnailPath;

    @SerializedName("category")
    private Category category;

    @SerializedName("account")
    private Account account;

    public boolean isWaitTranscoding() {
        return waitTranscoding;
    }

    public Channel getChannel() {
        return channel;
    }

    public Privacy getPrivacy() {
        return privacy;
    }

    public Object getDescription() {
        return description;
    }

    public Language getLanguage() {
        return language;
    }

    public String getPreviewPath() {
        return previewPath;
    }

    public Object getBlacklistedReason() {
        return blacklistedReason;
    }

    public String getUuid() {
        return uuid;
    }

    public int getDuration() {
        return duration;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public Object getOriginallyPublishedAt() {
        return originallyPublishedAt;
    }

    public boolean isIsLive() {
        return isLive;
    }

    public int getId() {
        return id;
    }

    public State getState() {
        return state;
    }

    public String getEmbedPath() {
        return embedPath;
    }

    public int getViews() {
        return views;
    }

    public int getLikes() {
        return likes;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public Licence getLicence() {
        return licence;
    }

    public boolean isNsfw() {
        return nsfw;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public int getDislikes() {
        return dislikes;
    }

    public boolean isIsLocal() {
        return isLocal;
    }

    public boolean isBlacklisted() {
        return blacklisted;
    }

    public String getName() {
        return name;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public Category getCategory() {
        return category;
    }

    public Account getAccount() {
        return account;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataItem dataItem = (DataItem) o;
        return this.id == dataItem.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}