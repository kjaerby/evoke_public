package moe.evoke.application.backend.mirror.distribution;

import moe.evoke.application.backend.db.Anime;
import moe.evoke.application.backend.db.Database;
import moe.evoke.application.backend.db.Episode;
import moe.evoke.application.backend.db.Hoster;
import moe.evoke.application.backend.hoster.mega.MegaNZ;
import moe.evoke.application.backend.hoster.mp4upload.MP4Upload;
import moe.evoke.application.backend.hoster.peertube.PeerTube;
import moe.evoke.application.backend.hoster.streamtape.Streamtape;
import moe.evoke.application.backend.hoster.streamz.StreamZ;
import moe.evoke.application.backend.hoster.vivio.Vivo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public enum DistributionTarget {
    PEERTUBE(8, "PeerTube"),
    MEGA(9, "MEGA"),
    STREAMZ(6, "streamZ"),
    VIVO(11, "VIVO.sx"),
    STREAMTAPE(12, "Streamtape"),
    MP4UPLOAD(1, "mp4upload"),
    IPFS(13, "IPFS");

    private static final Logger logger = LoggerFactory.getLogger(DistributionTarget.class);

    private final long hosterID;
    private final String label;

    DistributionTarget(long hosterID, String label) {
        this.hosterID = hosterID;
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return this.label;
    }

    public Hoster getHoster() {
        return Database.instance().getHosterByID(hosterID);
    }

    public DistributionTargetRunner runner() {

        switch (this) {
            case MEGA:
                return (fileToUpload, anime, episode) -> mega(fileToUpload, anime, episode);
            case VIVO:
                return (fileToUpload, anime, episode) -> vivo(fileToUpload, anime, episode);
            case STREAMZ:
                return (fileToUpload, anime, episode) -> streamz(fileToUpload, anime, episode);
            case PEERTUBE:
                return (fileToUpload, anime, episode) -> peertube(fileToUpload, anime, episode);
            case STREAMTAPE:
                return (fileToUpload, anime, episode) -> streamtape(fileToUpload, anime, episode);
            case MP4UPLOAD:
                return (fileToUpload, anime, episode) -> mp4upload(fileToUpload, anime, episode);
            case IPFS:
                return (fileToUpload, anime, episode) -> ipfs(fileToUpload, episode);
        }

        return (fileToUpload, anime, episode) -> {
        };
    }

    private void mega(File fileToUpload, Anime anime, Episode episode) {
        try {
            MegaNZ.uploadFile(fileToUpload, "evoke/animes/" + anime.getAnilistID() + "/");
            String embedURL = MegaNZ.getEmbedURL("evoke/animes/" + anime.getAnilistID() + "/" + fileToUpload.getName());
            Hoster hoster = DistributionTarget.MEGA.getHoster();
            Database.instance().createHostedEpisode(hoster, episode, embedURL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void peertube(File fileToUpload, Anime anime, Episode episode) {
        try {
            String embedURL = PeerTube.uploadFile(fileToUpload);
            Hoster hoster = DistributionTarget.PEERTUBE.getHoster();
            Database.instance().createHostedEpisode(hoster, episode, embedURL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void streamz(File fileToUpload, Anime anime, Episode episode) {
        try {
            String embedURL = StreamZ.uploadFile(fileToUpload);
            Hoster hoster = DistributionTarget.STREAMZ.getHoster();
            Database.instance().createHostedEpisode(hoster, episode, embedURL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void vivo(File fileToUpload, Anime anime, Episode episode) {
        try {
            String embedURL = Vivo.uploadFile(fileToUpload);
            embedURL = embedURL.replace("https://vivo.sx/", "https://vivo.sx/embed/");
            Hoster hoster = DistributionTarget.VIVO.getHoster();
            Database.instance().createHostedEpisode(hoster, episode, embedURL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void streamtape(File fileToUpload, Anime anime, Episode episode) {
        try {
            String embedURL = Streamtape.uploadAnime(anime, fileToUpload);
            Hoster hoster = DistributionTarget.STREAMTAPE.getHoster();
            Database.instance().createHostedEpisode(hoster, episode, embedURL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mp4upload(File fileToUpload, Anime anime, Episode episode) {
        long fileSizeInBytes = fileToUpload.length();
        long fileSizeInKB = fileSizeInBytes / 1024;
        long fileSizeInMB = fileSizeInKB / 1024;

        if (fileSizeInMB < 500) {
            try {
                String embedURL = MP4Upload.uploadFile(fileToUpload);
                Hoster hoster = DistributionTarget.MP4UPLOAD.getHoster();
                Database.instance().createHostedEpisode(hoster, episode, embedURL);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            logger.warn("File is bigger then 500mb (" + fileSizeInMB + "). Cannot upload to " + DistributionTarget.MP4UPLOAD.getLabel() + "!");
        }
    }

    private void ipfs(File fileToUpload, Episode episode) {
        try {
            String embedURL = moe.evoke.application.backend.hoster.ipfs.IPFS.uploadFile(fileToUpload, true);
            Hoster hoster = DistributionTarget.IPFS.getHoster();
            Database.instance().createHostedEpisode(hoster, episode, embedURL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
