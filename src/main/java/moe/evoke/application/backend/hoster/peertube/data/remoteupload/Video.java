package moe.evoke.application.backend.hoster.peertube.data.remoteupload;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Video {

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
    private Object previewPath;

    @SerializedName("uuid")
    private String uuid;

    @SerializedName("duration")
    private int duration;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("originallyPublishedAt")
    private String originallyPublishedAt;

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

    @SerializedName("tags")
    private List<Object> tags;

    @SerializedName("name")
    private String name;

    @SerializedName("thumbnailPath")
    private Object thumbnailPath;

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

    public Object getPreviewPath() {
        return previewPath;
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

    public String getOriginallyPublishedAt() {
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

    public List<Object> getTags() {
        return tags;
    }

    public String getName() {
        return name;
    }

    public Object getThumbnailPath() {
        return thumbnailPath;
    }

    public Category getCategory() {
        return category;
    }

    public Account getAccount() {
        return account;
    }
}