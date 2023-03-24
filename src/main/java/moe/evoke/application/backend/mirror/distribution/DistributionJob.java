package moe.evoke.application.backend.mirror.distribution;

import moe.evoke.application.backend.db.Anime;
import moe.evoke.application.backend.db.Episode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DistributionJob {

    public long ID;
    public Anime anime;
    public Episode episode;
    public DistributionSource source;
    public List<DistributionTarget> targets = new ArrayList<>();
    public Map<String, String> sourceOptions = new HashMap<>();

    private DistributionJobStatus status;

    public DistributionJobStatus getStatus() {
        return status;
    }

    public void setStatus(DistributionJobStatus status) {
        this.status = status;
    }

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
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

    public DistributionSource getSource() {
        return source;
    }

    public void setSource(DistributionSource source) {
        this.source = source;
    }

    public List<DistributionTarget> getTargets() {
        return targets;
    }

    public void setTargets(List<DistributionTarget> targets) {
        this.targets = targets;
    }

    public Map<String, String> getSourceOptions() {
        return sourceOptions;
    }

    public void setSourceOptions(Map<String, String> sourceOptions) {
        this.sourceOptions = sourceOptions;
    }
}
