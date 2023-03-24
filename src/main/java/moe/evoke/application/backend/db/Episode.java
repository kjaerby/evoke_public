package moe.evoke.application.backend.db;

import java.util.List;
import java.util.Objects;

public class Episode {

    private long ID;
    private long AnimeID;
    private long Number;

    private double progress;
    private boolean completed;

    @Override
    public String toString() {
        return Long.toString(getNumber());
    }

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public long getAnimeID() {
        return AnimeID;
    }

    public void setAnimeID(long animeID) {
        AnimeID = animeID;
    }

    public long getNumber() {
        return Number;
    }

    public void setNumber(long number) {
        Number = number;
    }

    public List<HostedEpisode> getHostedEpisodes() {
        return Database.instance().getHostedEpisodesForEpisode(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Episode episode = (Episode) o;
        return ID == episode.ID && AnimeID == episode.AnimeID && Number == episode.Number;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ID, AnimeID, Number);
    }

    public Anime getAnime() {
        return Database.instance().getAnimeByID(getAnimeID());
    }

    public double getProgress() {
        return this.progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
