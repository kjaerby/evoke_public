package moe.evoke.application.backend.anilist.airing;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Media {

    @SerializedName("siteUrl")
    private String siteUrl;

    @SerializedName("endDate")
    private EndDate endDate;

    @SerializedName("description")
    private String description;

    @SerializedName("idMal")
    private int idMal;

    @SerializedName("source")
    private String source;

    @SerializedName("title")
    private Title title;

    @SerializedName("averageScore")
    private int averageScore;

    @SerializedName("duration")
    private int duration;

    @SerializedName("trailer")
    private Trailer trailer;

    @SerializedName("bannerImage")
    private String bannerImage;

    @SerializedName("genres")
    private List<String> genres;

    @SerializedName("popularity")
    private int popularity;

    @SerializedName("coverImage")
    private CoverImage coverImage;

    @SerializedName("season")
    private String season;

    @SerializedName("externalLinks")
    private List<ExternalLinksItem> externalLinks;

    @SerializedName("id")
    private int id;

    @SerializedName("episodes")
    private Object episodes;

    @SerializedName("isAdult")
    private boolean isAdult;

    @SerializedName("hashtag")
    private String hashtag;

    @SerializedName("studios")
    private Studios studios;

    @SerializedName("synonyms")
    private List<Object> synonyms;

    @SerializedName("format")
    private String format;

    @SerializedName("rankings")
    private List<RankingsItem> rankings;

    @SerializedName("countryOfOrigin")
    private String countryOfOrigin;

    @SerializedName("relations")
    private Relations relations;

    @SerializedName("startDate")
    private StartDate startDate;

    @SerializedName("status")
    private String status;

    public String getSiteUrl() {
        return siteUrl;
    }

    public EndDate getEndDate() {
        return endDate;
    }

    public String getDescription() {
        return description;
    }

    public int getIdMal() {
        return idMal;
    }

    public String getSource() {
        return source;
    }

    public Title getTitle() {
        return title;
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

    public List<String> getGenres() {
        return genres;
    }

    public int getPopularity() {
        return popularity;
    }

    public CoverImage getCoverImage() {
        return coverImage;
    }

    public String getSeason() {
        return season;
    }

    public List<ExternalLinksItem> getExternalLinks() {
        return externalLinks;
    }

    public int getId() {
        return id;
    }

    public Object getEpisodes() {
        return episodes;
    }

    public boolean isIsAdult() {
        return isAdult;
    }

    public String getHashtag() {
        return hashtag;
    }

    public Studios getStudios() {
        return studios;
    }

    public List<Object> getSynonyms() {
        return synonyms;
    }

    public String getFormat() {
        return format;
    }

    public List<RankingsItem> getRankings() {
        return rankings;
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
                        "siteUrl = '" + siteUrl + '\'' +
                        ",endDate = '" + endDate + '\'' +
                        ",description = '" + description + '\'' +
                        ",idMal = '" + idMal + '\'' +
                        ",source = '" + source + '\'' +
                        ",title = '" + title + '\'' +
                        ",averageScore = '" + averageScore + '\'' +
                        ",duration = '" + duration + '\'' +
                        ",trailer = '" + trailer + '\'' +
                        ",bannerImage = '" + bannerImage + '\'' +
                        ",genres = '" + genres + '\'' +
                        ",popularity = '" + popularity + '\'' +
                        ",coverImage = '" + coverImage + '\'' +
                        ",season = '" + season + '\'' +
                        ",externalLinks = '" + externalLinks + '\'' +
                        ",id = '" + id + '\'' +
                        ",episodes = '" + episodes + '\'' +
                        ",isAdult = '" + isAdult + '\'' +
                        ",hashtag = '" + hashtag + '\'' +
                        ",studios = '" + studios + '\'' +
                        ",synonyms = '" + synonyms + '\'' +
                        ",format = '" + format + '\'' +
                        ",rankings = '" + rankings + '\'' +
                        ",countryOfOrigin = '" + countryOfOrigin + '\'' +
                        ",relations = '" + relations + '\'' +
                        ",startDate = '" + startDate + '\'' +
                        ",status = '" + status + '\'' +
                        "}";
    }
}