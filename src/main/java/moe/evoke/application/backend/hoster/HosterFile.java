package moe.evoke.application.backend.hoster;

import moe.evoke.application.backend.db.Anime;
import moe.evoke.application.backend.db.Episode;
import moe.evoke.application.backend.db.HostedEpisode;

import java.util.Objects;

public class HosterFile {

    public String name;
    public String embed;

    public Anime anime;
    public Episode episode;
    public HostedEpisode hostedEpisode;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmbed() {
        return embed;
    }

    public void setEmbed(String embed) {
        this.embed = embed;
    }

    public Anime getAnime() {
        return anime;
    }

    public void setAnime(Anime anime) {
        this.anime = anime;
    }

    public Episode getEpisode() {
        return episode;
    }

    public void setEpisode(Episode episode) {
        this.episode = episode;
    }

    public HostedEpisode getHostedEpisode() {
        return hostedEpisode;
    }

    public void setHostedEpisode(HostedEpisode hostedEpisode) {
        this.hostedEpisode = hostedEpisode;
    }

    @Override
    public String toString() {
        return "HosterFile{" +
                "name='" + name + '\'' +
                ", embed='" + embed + '\'' +
                ", anime=" + anime +
                ", episode=" + episode +
                ", hostedEpisode=" + hostedEpisode +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HosterFile that = (HosterFile) o;
        return Objects.equals(name, that.name) && Objects.equals(embed, that.embed) && Objects.equals(anime, that.anime) && Objects.equals(episode, that.episode) && Objects.equals(hostedEpisode, that.hostedEpisode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, embed, anime, episode, hostedEpisode);
    }
}
