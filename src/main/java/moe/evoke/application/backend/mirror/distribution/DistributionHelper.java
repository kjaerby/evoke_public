package moe.evoke.application.backend.mirror.distribution;

import moe.evoke.application.backend.Config;
import moe.evoke.application.backend.anilist.Anilist;
import moe.evoke.application.backend.crawler.Crunchyroll;
import moe.evoke.application.backend.crawler.GoGoAnime;
import moe.evoke.application.backend.crawler.TwistMoe;
import moe.evoke.application.backend.db.Anime;
import moe.evoke.application.backend.db.Database;
import moe.evoke.application.backend.db.Episode;
import moe.evoke.application.backend.hoster.mega.MegaNZ;
import moe.evoke.application.backend.malsync.MALSync;
import moe.evoke.application.backend.monthly.moe.AiringAnime;
import moe.evoke.application.backend.torrent.TorrentDownloader;
import moe.evoke.application.backend.torrent.TorrentManager;
import moe.evoke.application.backend.util.Utils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DistributionHelper {

    private static final Logger logger = LoggerFactory.getLogger(DistributionHelper.class);
    private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private static boolean scheduledJobStarted = false;


    public static File downloadFile(String target, String downloadURL) {
        File downloadedFile = new File(target);

        try {
            FileUtils.copyURLToFile(
                    new URL(downloadURL),
                    downloadedFile,
                    1000 * 10,
                    1000 * 10);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return downloadedFile;
    }

    public static void distributeEpisode(DistributionJob job) {

        DistributionManager.updateJobStatus(job, DistributionJobStatus.RUNNING);

        Anime anime = job.anime;
        Episode episode = job.episode;
        DistributionSource distributionSource = job.source;
        List<DistributionTarget> targets = job.targets;

        logger.info("Distributing '{}' episode {} from {} to {}", anime.getName(), episode.getNumber(), distributionSource, targets);

        String filename = anime.getAnilistID() + "-" + episode.getNumber() + ".mp4";
        File downloadedFile = new File(filename);
        try {
            if (distributionSource != DistributionSource.MANUAL) {

                if (distributionSource == DistributionSource.TORRENT) {
                    logger.info("Downloading file from torrent");
                    try {
                        downloadedFile = TorrentDownloader.downloadAnime(job.sourceOptions, anime, episode, new File("./"));
                    } catch (Throwable ex) {
                        logger.error("Exception occoured during torrent download!", ex);
                    }
                }

                if (distributionSource == DistributionSource.TWIST) {
                    logger.info("Downloading file from Twist.moe");
                    try {
                        downloadedFile = TwistMoe.downloadAnime(anime, episode);
                    } catch (Throwable ex) {
                        logger.error("Exception occoured during Twist download!", ex);
                    }
                    if (downloadedFile == null || !downloadedFile.exists()) {
                        logger.error("Could not download file!");
                        if (DistributionSource.GOGO.isAvailable(anime)) {
                            logger.warn("Using GoGo for'" + anime.getName() + "' since Twist failed!");
                            distributionSource = DistributionSource.GOGO;
                        }
                    }
                }

                if (distributionSource == DistributionSource.GOGO) {
                    logger.info("Downloading file from GoGo");
                    String downloadURL = GoGoAnime.getDownloadURL(anime, episode);
                    if (!downloadURL.equals("n/a") && GoGoAnime.downloadFileFromMP4Upload(downloadURL, filename)) {
                        downloadedFile = new File(filename);
                    } else {
                        logger.error("Could not download file!");
                        DistributionManager.updateJobStatus(job, DistributionJobStatus.ERROR);
                        return;
                    }
                } else if (distributionSource == DistributionSource.MEGA) {
                    logger.info("Downloading file from MEGA");
                    MegaNZ.downloadFile("evoke/animes/" + anime.getAnilistID() + "/" + filename, "./");
                    if (!downloadedFile.exists()) {
                        DistributionManager.updateJobStatus(job, DistributionJobStatus.ERROR);
                        return;
                    }
                } else if (distributionSource == DistributionSource.CRUNCHYROLL) {
                    logger.info("Downloading file from Cunrchyroll");
                    downloadedFile = Crunchyroll.downloadEpisode(job);
                }

            } else {
                logger.info("File was provided manually");
                downloadedFile = new File(filename);
            }

            if (downloadedFile != null && downloadedFile.exists()) {

                logger.info("Checking video file length...");

                int anilistDuration = anime.getData().getData().getMedia().getDuration();
                float videoDuration = Utils.getVideoLength(downloadedFile);
                if (videoDuration > 0) {
                    // we need minutes
                    videoDuration /= 60;
                }

                logger.debug("Checking if video length is near Anilist length");
                logger.debug("Anilist: " + anilistDuration);
                logger.debug("Video length: " + videoDuration);

                if (anilistDuration > 0 && !Utils.isWithinRange(anilistDuration, videoDuration, anilistDuration * 0.25) && anilistDuration > videoDuration) {
                    logger.error("Anilist duration has a too big difference to the downloded video! This must be a error. Distribution stopped");
                    downloadedFile.delete();
                    DistributionManager.updateJobStatus(job, DistributionJobStatus.ERROR);
                    return;
                }

                logger.debug("Distributing '" + downloadedFile.getName() + "' to:");
                for (DistributionTarget target : targets) {
                    logger.info("Distributing {}", target.getLabel());
                    target.runner().execute(downloadedFile, anime, episode);
                }

                logger.debug("Finished Distribution of " + downloadedFile.getName());
                DistributionManager.updateJobStatus(job, DistributionJobStatus.SUCCESS);
            } else {
                logger.error("Exception occurred during distribution!");
                DistributionManager.updateJobStatus(job, DistributionJobStatus.ERROR);
            }

        } catch (Exception ex) {
            logger.error("Exception occurred during distribution!");
            ex.printStackTrace();

            DistributionManager.updateJobStatus(job, DistributionJobStatus.ERROR);
        }

        if (downloadedFile != null) {
            downloadedFile.delete();
        }
    }

    public static DistributionSource getBestSourceForAnime(Anime anime, Episode episode) {
        List<DistributionSource> sources = DistributionSource.availableModes(anime);
        DistributionSource source = null;
        if (TorrentManager.isEpisodeAvailable(anime, episode)) {
            source = DistributionSource.TORRENT;
        } else if (sources.contains(DistributionSource.TWIST) && Config.isTwistEnabled()) {
            source = DistributionSource.TWIST;
        } else if (sources.contains(DistributionSource.GOGO) && Config.isGogoEnabled()) {
            source = DistributionSource.GOGO;
        } else {
            logger.warn("No source for '" + anime.getName() + "'");
        }

        return source;
    }

    public static void scheduleDistribution(Date runDate, DistributionJob job) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

        Runnable runnable = () -> DistributionManager.submitJob(job);

        Date now = new Date();
        long timeToStart = runDate.getTime() - now.getTime();
        executor.schedule(runnable, timeToStart, TimeUnit.MILLISECONDS);
    }

    public static boolean isScheduledJobStarted() {
        return scheduledJobStarted;
    }

    public static void watchAiringDistribution() {

        Runnable job = () -> {
            try {
                logger.info("Running automatic import...");

                Calendar nowCal = GregorianCalendar.getInstance();
                nowCal.setFirstDayOfWeek(Calendar.MONDAY);

                Calendar episodeCal = GregorianCalendar.getInstance();
                episodeCal.setFirstDayOfWeek(Calendar.MONDAY);

                List<AiringAnime.AiringEpisode> airingEpisodes = Anilist.getAiring();
                for (AiringAnime.AiringEpisode airingEpisode : airingEpisodes) {
                    try {
                        episodeCal.setTime(airingEpisode.getAiringDate());

                        if (airingEpisode.getEpisode().getAnime().getEpisodeCount() == 0)
                        {
                            Anilist.refreshCacheForAnime(airingEpisode.getEpisode().getAnime());
                        }

                        if (episodeCal.after(nowCal)) {
                            logger.debug("Airing of this episode is in the future! No distribution possible!");
                        } else {
                            episodeCal.add(Calendar.HOUR, 1);
                            if (!episodeCal.after(nowCal)) {
                                logger.info("Episode aired more than one hour ago. Trying import + distribution");

                                Episode airedEpisode = airingEpisode.getEpisode();
                                if (airedEpisode.getHostedEpisodes() != null && airedEpisode.getHostedEpisodes().size() > 0) {
                                    logger.debug("Episode already present. No work to do!");
                                } else {
                                    logger.info("Episode is missing. Picking best source...");

                                    DistributionSource source = DistributionHelper.getBestSourceForAnime(airingEpisode.getEpisode().getAnime(), airedEpisode);
                                    if (source == null) {
                                        logger.warn("No source found. Refreshing MALlSync cache...");
                                        MALSync.refreshCacheForAnime(airingEpisode.getEpisode().getAnime());
                                        source = DistributionHelper.getBestSourceForAnime(airingEpisode.getEpisode().getAnime(), airedEpisode);
                                        if (source == null) {
                                            logger.error("No source for anime found! Aborting.");
                                            continue;
                                        }
                                    }

                                    List<DistributionTarget> targets = new ArrayList<>();
                                    targets.add(DistributionTarget.MEGA);
                                    targets.add(DistributionTarget.STREAMTAPE);

                                    logger.info("Trying to distribute '" + airingEpisode.getEpisode().getAnime().getName() + "' episode " + airedEpisode.getNumber() + " from '" + source + "' to '" + targets + "'");

                                    DistributionJob distributionJob = new DistributionJob();
                                    distributionJob.anime = airingEpisode.getEpisode().getAnime();
                                    distributionJob.episode = airedEpisode;
                                    distributionJob.source = source;
                                    distributionJob.targets = targets;

                                    if (source == DistributionSource.TORRENT) {
                                        String torrentURL = TorrentManager.getBestTorrentForAnime(airingEpisode.getEpisode().getAnime(), airedEpisode);
                                        distributionJob.sourceOptions = new HashMap<>();
                                        distributionJob.sourceOptions.put(DistributionSource.MAGNET_LINK, torrentURL);
                                    }

                                    if (!checkExistingJob(distributionJob)) {
                                        DistributionManager.submitJob(distributionJob);
                                    } else {
                                        logger.warn("Job already open! No creation of new one!");
                                    }
                                }
                            }
                        }
                    } catch (Throwable ex) {
                        logger.error("Exception occurred: ", ex);
                    }
                }
            } catch (Throwable ex) {
                logger.error("Exception occurred: ", ex);
            }
        };

        logger.info("Initialize Background Airing Check");
        if (!scheduledJobStarted) {
            executor.scheduleAtFixedRate(job, 0, Config.getAiringImportInterval(), TimeUnit.MINUTES);
            scheduledJobStarted = true;
        } else {
            logger.warn("Background Airing Check is already running!");
        }
    }

    private static boolean checkExistingJob(DistributionJob distributionJob) {
        return Database.instance().getAllDistributionJobs(DistributionJobStatus.OPEN, DistributionJobStatus.RUNNING).stream().anyMatch(job ->
                job.anime.getAnilistID() == distributionJob.anime.getAnilistID()
                        && job.episode.getNumber() == job.episode.getNumber()
        );
    }
}
