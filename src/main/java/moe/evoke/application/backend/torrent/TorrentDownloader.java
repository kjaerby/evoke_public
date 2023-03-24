package moe.evoke.application.backend.torrent;

import moe.evoke.application.backend.db.Anime;
import moe.evoke.application.backend.db.Episode;
import moe.evoke.application.backend.mirror.distribution.DistributionSource;
import moe.evoke.application.backend.util.Utils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

public class TorrentDownloader {

    private static final Logger logger = LoggerFactory.getLogger(TorrentDownloader.class);

    public static File downloadAnime(Map<String, String> sourceOptions, Anime anime, Episode episode, File targetFolder) {


        if (sourceOptions != null && sourceOptions.containsKey(DistributionSource.MAGNET_LINK)) {

            String magnetURL = sourceOptions.get(DistributionSource.MAGNET_LINK);
            File download = Utils.downloadFileWithAria(magnetURL);
            if (download == null) {
                logger.error("Download error!");
                return null;
            }

            File[] files = download.listFiles(pathname -> FilenameUtils.getExtension(pathname.getName()).equalsIgnoreCase("mkv"));
            if (files == null) {
                logger.error("No .mkv files in download folder.");
                logger.debug("Downloaded files:");
                for (File file : download.listFiles()) {
                    logger.debug(file.getName());
                }

                cleanup(download);
                return null;
            }

            if (files.length > 1) {
                logger.warn("More than one .mkv found! Only converting: " + files[0].getName());
            }

            for (File video : files) {
                File subtitle = Utils.extractSubtitle(video);
                File output = Utils.burninSubtitle(video, subtitle);

                File newFile = new File(targetFolder.getAbsolutePath() + "/" + anime.getAnilistID() + "-" + episode.getNumber() + ".mp4");
                output.renameTo(newFile);
                subtitle.delete();
                cleanup(download);
                return newFile;
            }

            cleanup(download);
        }

        return null;
    }

    private static void cleanup(File download) {
        for (File file : download.listFiles()) {
            file.delete();
        }

        download.delete();
    }
}
