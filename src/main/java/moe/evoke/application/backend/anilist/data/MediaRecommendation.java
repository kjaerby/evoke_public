package moe.evoke.application.backend.anilist.data;

import com.google.gson.annotations.SerializedName;

public class MediaRecommendation {

    @SerializedName("bannerImage")
    private String bannerImage;

    @SerializedName("coverImage")
    private CoverImage coverImage;

    @SerializedName("format")
    private String format;

    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private Title title;

    @SerializedName("type")
    private String type;

    @SerializedName("status")
    private String status;

    public String getBannerImage() {
        return bannerImage;
    }

    public CoverImage getCoverImage() {
        return coverImage;
    }

    public String getFormat() {
        return format;
    }

    public int getId() {
        return id;
    }

    public Title getTitle() {
        return title;
    }

    public String getType() {
        return type;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return
                "MediaRecommendation{" +
                        "bannerImage = '" + bannerImage + '\'' +
                        ",coverImage = '" + coverImage + '\'' +
                        ",format = '" + format + '\'' +
                        ",id = '" + id + '\'' +
                        ",title = '" + title + '\'' +
                        ",type = '" + type + '\'' +
                        ",status = '" + status + '\'' +
                        "}";
    }
}