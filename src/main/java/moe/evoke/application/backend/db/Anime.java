package moe.evoke.application.backend.db;

import com.vaadin.flow.component.html.Image;
import moe.evoke.application.backend.anilist.Anilist;
import moe.evoke.application.backend.anilist.data.AnimeData;
import moe.evoke.application.backend.util.Utils;

import java.util.List;
import java.util.Objects;

public class Anime {

    private long ID;
    private long AnilistID;
    private String name;
    private String cover;
    private int episodeCount = -1;
    private String status;

    public List<Episode> getEpisodes() {
        return Database.instance().getEpisodesForAnime(this);
    }

    public AnimeData getData() {
        return Anilist.getInfoForAnime(this.getAnilistID());
    }

    public int getEpisodeCount() {
        if (episodeCount == -1) {
            episodeCount = getData().getData().getMedia().getEpisodes();

            if (episodeCount == 0) {
                Anilist.refreshCacheForAnime(this);
                episodeCount = getData().getData().getMedia().getEpisodes();
            }
        }
        return episodeCount;
    }

    public void setEpisodeCount(int episodeCount) {
        this.episodeCount = episodeCount;
    }

    public String getStatus() {
        if (this.status == null || this.status.isEmpty()) {
            return getData().getData().getMedia().getStatus();
        }

        return this.status;
    }

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public long getAnilistID() {
        return AnilistID;
    }

    public void setAnilistID(long anilistID) {
        AnilistID = anilistID;
    }

    public String getName() {
        if (this.name == null || this.name.isEmpty()) {
            return getData().getData().getMedia().getTitle().getUserPreferred();
        }

        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public Image getCoverAsImage() {
        return Utils.generateAnimeCoverImage(this);
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Anime anime = (Anime) o;
        return ID == anime.ID && AnilistID == anime.AnilistID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ID, AnilistID);
    }
}
