package moe.evoke.application.backend.db;

public class HostedEpisode {

    private long ID;
    private long HostID;
    private long EpisodeID;
    private String StreamURL;

    public Hoster getHoster() {
        return Database.instance().getHosterForHostedEpisode(this);
    }

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public long getHostID() {
        return HostID;
    }

    public void setHostID(long hostID) {
        HostID = hostID;
    }

    public long getEpisodeID() {
        return EpisodeID;
    }

    public void setEpisodeID(long episodeID) {
        EpisodeID = episodeID;
    }

    public String getStreamURL() {
        return StreamURL;
    }

    public void setStreamURL(String streamURL) {
        StreamURL = streamURL;
    }

    @Override
    public String toString() {
        return "HostedEpisode{" +
                "ID=" + ID +
                ", HostID=" + HostID +
                ", EpisodeID=" + EpisodeID +
                ", StreamURL='" + StreamURL + '\'' +
                '}';
    }
}
