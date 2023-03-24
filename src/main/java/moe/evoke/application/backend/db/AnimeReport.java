package moe.evoke.application.backend.db;

import java.util.Objects;

public class AnimeReport {

    private long id;
    private AnimeReportReason reportReason;
    private String description;
    private long userID;
    private long animeID;
    private long episodeID;

    @Override
    public String toString() {
        return "AnimeReport{" +
                "id=" + id +
                ", reportReason=" + reportReason +
                ", description='" + description + '\'' +
                ", userID=" + userID +
                ", animeID=" + animeID +
                ", episodeID=" + episodeID +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnimeReport that = (AnimeReport) o;
        return id == that.id && userID == that.userID && animeID == that.animeID && episodeID == that.episodeID && reportReason == that.reportReason && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, reportReason, description, userID, animeID, episodeID);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public AnimeReportReason getReportReason() {
        return reportReason;
    }

    public void setReportReason(AnimeReportReason reportReason) {
        this.reportReason = reportReason;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getUserID() {
        return userID;
    }

    public void setUserID(long userID) {
        this.userID = userID;
    }

    public long getAnimeID() {
        return animeID;
    }

    public void setAnimeID(long animeID) {
        this.animeID = animeID;
    }

    public long getEpisodeID() {
        return episodeID;
    }

    public void setEpisodeID(long episodeID) {
        this.episodeID = episodeID;
    }
}
