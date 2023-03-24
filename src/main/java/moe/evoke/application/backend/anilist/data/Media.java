package moe.evoke.application.backend.anilist.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Media {

    @SerializedName("mediaListEntry")
    private Object mediaListEntry;

    @SerializedName("favourites")
    private int favourites;

    @SerializedName("endDate")
    private EndDate endDate;

    @SerializedName("chapters")
    private Object chapters;

    @SerializedName("description")
    private String description;

    @SerializedName("source")
    private String source;

    @SerializedName("title")
    private Title title;

    @SerializedName("type")
    private String type;

    @SerializedName("recommendations")
    private Recommendations recommendations;

    @SerializedName("averageScore")
    private int averageScore;

    @SerializedName("duration")
    private int duration;

    @SerializedName("trailer")
    private Trailer trailer;

    @SerializedName("bannerImage")
    private String bannerImage;

    @SerializedName("stats")
    private Stats stats;

    @SerializedName("genres")
    private List<String> genres;

    @SerializedName("coverImage")
    private CoverImage coverImage;

    @SerializedName("isLocked")
    private boolean isLocked;

    @SerializedName("popularity")
    private int popularity;

    @SerializedName("season")
    private String season;

    @SerializedName("isLicensed")
    private boolean isLicensed;

    @SerializedName("externalLinks")
    private List<ExternalLinksItem> externalLinks;

    @SerializedName("id")
    private int id;

    @SerializedName("isFavourite")
    private boolean isFavourite;

    @SerializedName("seasonYear")
    private int seasonYear;

    @SerializedName("episodes")
    private int episodes;

    @SerializedName("isAdult")
    private boolean isAdult;

    @SerializedName("hashtag")
    private String hashtag;

    @SerializedName("characterPreview")
    private CharacterPreview characterPreview;

    @SerializedName("studios")
    private Studios studios;

    @SerializedName("synonyms")
    private List<Object> synonyms;

    @SerializedName("meanScore")
    private int meanScore;

    @SerializedName("format")
    private String format;

    @SerializedName("volumes")
    private Object volumes;

    @SerializedName("tags")
    private List<TagsItem> tags;

    @SerializedName("streamingEpisodes")
    private List<StreamingEpisodesItem> streamingEpisodes;

    @SerializedName("rankings")
    private List<RankingsItem> rankings;

    @SerializedName("nextAiringEpisode")
    private Object nextAiringEpisode;

    @SerializedName("reviewPreview")
    private ReviewPreview reviewPreview;

    @SerializedName("staffPreview")
    private StaffPreview staffPreview;

    @SerializedName("isRecommendationBlocked")
    private boolean isRecommendationBlocked;

    @SerializedName("countryOfOrigin")
    private String countryOfOrigin;

    @SerializedName("relations")
    private Relations relations;

    @SerializedName("startDate")
    private StartDate startDate;

    @SerializedName("status")
    private String status;

    public Object getMediaListEntry() {
        return mediaListEntry;
    }

    public int getFavourites() {
        return favourites;
    }

    public EndDate getEndDate() {
        return endDate;
    }

    public Object getChapters() {
        return chapters;
    }

    public String getDescription() {
        return description;
    }

    public String getSource() {
        return source;
    }

    public Title getTitle() {
        return title;
    }

    public String getType() {
        return type;
    }

    public Recommendations getRecommendations() {
        return recommendations;
    }

    public int getAverageScore() {
        return averageScore;
    }

    public int getDuration() {
        return duration;
    }

    public Trailer getTrailer() {
        return trailer;
    }

    public String getBannerImage() {
        return bannerImage;
    }

    public Stats getStats() {
        return stats;
    }

    public List<String> getGenres() {
        return genres;
    }

    public CoverImage getCoverImage() {
        return coverImage;
    }

    public boolean isIsLocked() {
        return isLocked;
    }

    public int getPopularity() {
        return popularity;
    }

    public String getSeason() {
        return season;
    }

    public boolean isIsLicensed() {
        return isLicensed;
    }

    public List<ExternalLinksItem> getExternalLinks() {
        return externalLinks;
    }

    public int getId() {
        return id;
    }

    public boolean isIsFavourite() {
        return isFavourite;
    }

    public int getSeasonYear() {
        return seasonYear;
    }

    public int getEpisodes() {
        return episodes;
    }

    public boolean isIsAdult() {
        return isAdult;
    }

    public String getHashtag() {
        return hashtag;
    }

    public CharacterPreview getCharacterPreview() {
        return characterPreview;
    }

    public Studios getStudios() {
        return studios;
    }

    public List<Object> getSynonyms() {
        return synonyms;
    }

    public int getMeanScore() {
        return meanScore;
    }

    public String getFormat() {
        return format;
    }

    public Object getVolumes() {
        return volumes;
    }

    public List<TagsItem> getTags() {
        return tags;
    }

    public List<StreamingEpisodesItem> getStreamingEpisodes() {
        return streamingEpisodes;
    }

    public List<RankingsItem> getRankings() {
        return rankings;
    }

    public Object getNextAiringEpisode() {
        return nextAiringEpisode;
    }

    public ReviewPreview getReviewPreview() {
        return reviewPreview;
    }

    public StaffPreview getStaffPreview() {
        return staffPreview;
    }

    public boolean isIsRecommendationBlocked() {
        return isRecommendationBlocked;
    }

    public String getCountryOfOrigin() {
        return countryOfOrigin;
    }

    public Relations getRelations() {
        return relations;
    }

    public StartDate getStartDate() {
        return startDate;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return
                "Media{" +
                        "mediaListEntry = '" + mediaListEntry + '\'' +
                        ",favourites = '" + favourites + '\'' +
                        ",endDate = '" + endDate + '\'' +
                        ",chapters = '" + chapters + '\'' +
                        ",description = '" + description + '\'' +
                        ",source = '" + source + '\'' +
                        ",title = '" + title + '\'' +
                        ",type = '" + type + '\'' +
                        ",recommendations = '" + recommendations + '\'' +
                        ",averageScore = '" + averageScore + '\'' +
                        ",duration = '" + duration + '\'' +
                        ",trailer = '" + trailer + '\'' +
                        ",bannerImage = '" + bannerImage + '\'' +
                        ",stats = '" + stats + '\'' +
                        ",genres = '" + genres + '\'' +
                        ",coverImage = '" + coverImage + '\'' +
                        ",isLocked = '" + isLocked + '\'' +
                        ",popularity = '" + popularity + '\'' +
                        ",season = '" + season + '\'' +
                        ",isLicensed = '" + isLicensed + '\'' +
                        ",externalLinks = '" + externalLinks + '\'' +
                        ",id = '" + id + '\'' +
                        ",isFavourite = '" + isFavourite + '\'' +
                        ",seasonYear = '" + seasonYear + '\'' +
                        ",episodes = '" + episodes + '\'' +
                        ",isAdult = '" + isAdult + '\'' +
                        ",hashtag = '" + hashtag + '\'' +
                        ",characterPreview = '" + characterPreview + '\'' +
                        ",studios = '" + studios + '\'' +
                        ",synonyms = '" + synonyms + '\'' +
                        ",meanScore = '" + meanScore + '\'' +
                        ",format = '" + format + '\'' +
                        ",volumes = '" + volumes + '\'' +
                        ",tags = '" + tags + '\'' +
                        ",streamingEpisodes = '" + streamingEpisodes + '\'' +
                        ",rankings = '" + rankings + '\'' +
                        ",nextAiringEpisode = '" + nextAiringEpisode + '\'' +
                        ",reviewPreview = '" + reviewPreview + '\'' +
                        ",staffPreview = '" + staffPreview + '\'' +
                        ",isRecommendationBlocked = '" + isRecommendationBlocked + '\'' +
                        ",countryOfOrigin = '" + countryOfOrigin + '\'' +
                        ",relations = '" + relations + '\'' +
                        ",startDate = '" + startDate + '\'' +
                        ",status = '" + status + '\'' +
                        "}";
    }
}