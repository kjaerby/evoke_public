package moe.evoke.application.backend.hoster.mega;

import io.github.eliux.mega.Mega;
import io.github.eliux.mega.MegaSession;
import io.github.eliux.mega.cmd.FileInfo;
import io.github.eliux.mega.cmd.MegaCmdExport;
import io.github.eliux.mega.error.MegaResourceAlreadyExistsException;
import io.github.eliux.mega.error.MegaUnexpectedFailureException;
import moe.evoke.application.backend.Config;
import moe.evoke.application.backend.db.Anime;
import moe.evoke.application.backend.db.Database;
import moe.evoke.application.backend.db.Episode;
import moe.evoke.application.backend.db.HostedEpisode;
import moe.evoke.application.backend.hoster.HosterFile;
import moe.evoke.application.backend.mirror.distribution.DistributionTarget;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.io.CopyStreamAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

public class MegaNZ {

    private static final Logger logger = LoggerFactory.getLogger(MegaNZ.class);
    private static final ReentrantLock lock = new ReentrantLock();


    public static void uploadFile(File downloadedFile, String target) {
        lock.lock();

        logger.debug("uploadFile(" + downloadedFile + ", " + target + ")");

        try {
            MegaSession sessionMega = Mega.init();

            FTPClient ftpClient = new FTPClient();
            final int[] lastPercentage = {-1};
            CopyStreamAdapter streamListener = new CopyStreamAdapter() {
                @Override
                public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize) {
                    int percent = (int) (totalBytesTransferred * 100 / downloadedFile.length());
                    if (percent > lastPercentage[0]) {
                        logger.debug(downloadedFile.getName() + ": " + percent + "%");
                        lastPercentage[0] = percent;
                    }
                }
            };
            ftpClient.setCopyStreamListener(streamListener);

            ftpClient.connect(Config.getMegaFtpAddress(), Config.getMegaFtpPort());
            logger.debug(ftpClient.getReplyString());
            ftpClient.login("", "");
            logger.debug(ftpClient.getReplyString());
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();

            String serverPath = "oNM02TqR/Cloud Drive/" + target;
            if (serverPath.charAt(serverPath.length() - 1) == '/') {
                serverPath = serverPath.substring(0, serverPath.length() - 1);
            }

            ftpClient.makeDirectory(serverPath);
            logger.debug(ftpClient.getReplyString());

            ftpClient.changeWorkingDirectory(serverPath);
            logger.debug(ftpClient.getReplyString());


            boolean retry = false;
            int currentTry = 0;
            do {
                try {
                    if (!sessionMega.exists(target + downloadedFile)) {
                        try {
                            String name = downloadedFile.getName();
                            logger.debug("Upload '" + name + "' to '" + ftpClient.printWorkingDirectory() + "'");

                            ftpClient.storeFile(name, new FileInputStream(downloadedFile));
                            logger.debug(ftpClient.getReplyString());

                            //sessionMega.uploadFile(downloadedFile.getAbsolutePath(), target).waitToUpload().createRemotePathIfNotPresent().run();
                        } catch (MegaResourceAlreadyExistsException ex) {
                            logger.warn("MegaResourceAlreadyExistsException was thrown?! Trying to delete file, then retry...");

                            try {
                                Thread.sleep(30 * 1000);
                            } catch (InterruptedException e) {
                            }

                            retry = true;
                            continue;
                        } catch (Exception e) {
                            logger.error("Could not use FTP upload!", e);
                        }

                    } else {
                        logger.error("File already exists on MEGA!");
                    }
                } catch (MegaUnexpectedFailureException ex) {
                    logger.warn("Unexpected MEGA Exception! Retry! (try " + currentTry + " of 10)");
                    retry = true;
                    try {
                        if (sessionMega.exists(target + downloadedFile.getName())) {
                            logger.info("File '' already exists in MEGA! Removing...");
                            sessionMega.remove(target + downloadedFile.getName()).run();
                        }
                    } catch (Exception e) {
                        logger.error("Exception occurred:", e);
                    }
                }

                currentTry++;
            } while (retry && currentTry < 10);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public static String getEmbedURL(String file) {
        lock.lock();

        logger.debug("getEmbedURL(" + file + ")");

        String embedURL = "n/a";
        try {
            MegaSession sessionMega = Mega.init();
            MegaCmdExport export = sessionMega.export(file);
            embedURL = export.call().getPublicLink().replace("file", "embed");
        } finally {
            lock.unlock();
        }
        return embedURL;
    }

    public static void downloadFile(String source, String target) {
        logger.debug("downloadFile(" + source + ", " + target + ")");

        try {
            File resulFile = new File(target + FilenameUtils.getName(source));

            FTPClient ftpClient = new FTPClient();
            ftpClient.connect(Config.getMegaFtpAddress(), Config.getMegaFtpPort());
            logger.debug(ftpClient.getReplyString());
            ftpClient.login("", "");
            logger.debug(ftpClient.getReplyString());
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();

            String serverPath = "oNM02TqR/Cloud Drive/" + source;
            if (serverPath.charAt(serverPath.length() - 1) == '/') {
                serverPath = serverPath.substring(0, serverPath.length() - 1);
            }

            ftpClient.size(serverPath);
            String reply = ftpClient.getReplyString();
            reply = reply.substring(reply.indexOf(" ")).strip();
            long fileSize = Long.parseLong(reply);

            final int[] lastPercentage = {-1};
            String finalServerPath = serverPath;
            CopyStreamAdapter streamListener = new CopyStreamAdapter() {
                @Override
                public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize) {
                    int percent = (int) (totalBytesTransferred * 100 / fileSize);
                    if (percent > lastPercentage[0]) {
                        logger.debug(finalServerPath + ": " + percent + "%");
                        lastPercentage[0] = percent;
                    }
                }
            };
            ftpClient.setCopyStreamListener(streamListener);

            FileOutputStream fos = new FileOutputStream(resulFile.getAbsolutePath());
            boolean result = ftpClient.retrieveFile(serverPath, fos);
            logger.debug(ftpClient.getReplyString());

            fos.flush();
            fos.close();

            logger.info("Download finished! result: " + result);
            if (!result) {
                resulFile.delete();
            }

        } catch (Exception ex) {
            logger.error("Error while download!", ex);
        }
    }

    private static void checkForDownloadTmpFile(String target) {
        logger.info("Checking for tmp MEGA download file...");

        File dir = new File(target);
        if (!dir.isDirectory())
            dir = dir.getParentFile();

        File[] files = null;
        do {
            FileFilter fileFilter = new WildcardFileFilter(".getxfer.*.mega");
            files = dir.listFiles(fileFilter);

            if (files.length > 0) {
                logger.info("Found tmp mega file! Waiting for finish...");
            }

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } while (files.length > 0);

    }


    public static List<HosterFile> getFiles(Anime anime) {
        lock.lock();

        logger.debug("getFiles(" + anime.getAnilistID() + ")");

        List<HosterFile> files = new ArrayList<>();
        try {
            MegaSession sessionMega = Mega.init();

            final String basePath = "/evoke/animes/" + anime.getAnilistID();
            if (!sessionMega.exists(basePath)) {
                sessionMega.makeDirectory(basePath);
            } else {
                List<FileInfo> episodes = sessionMega.ls(basePath).call();
                for (FileInfo episode : episodes) {
                    HosterFile hosterFile = new HosterFile();
                    hosterFile.name = episode.getName();
                    hosterFile.anime = anime;

                    MegaCmdExport export = sessionMega.export(basePath + "/" + episode.getName());
                    String publicLink = export.call().getPublicLink().replace("file", "embed");
                    hosterFile.embed = publicLink;

                    hosterFile.hostedEpisode = Database.instance().getHostedEpisodeForStreamURL(hosterFile.embed);

                    if (hosterFile.hostedEpisode == null) {
                        logger.warn("HostedEpisode missing for: '" + episode.getName() + "'");
                        String epNumStr = episode.getName().split("-")[1];
                        epNumStr = epNumStr.substring(0, epNumStr.indexOf('.'));
                        long epNum = Long.parseLong(epNumStr);

                        Optional<Episode> episodeOptional = hosterFile.getAnime().getEpisodes().stream().filter(episode1 -> episode1.getNumber() == epNum).findFirst();
                        if (episodeOptional.isPresent()) {
                            Episode episodeObj = episodeOptional.get();
                            Database.instance().createHostedEpisode(DistributionTarget.MEGA.getHoster(), episodeObj, publicLink);
                            hosterFile.hostedEpisode = Database.instance().getHostedEpisodeForStreamURL(hosterFile.embed);
                        } else {
                            logger.warn("Could not find episode '" + epNumStr + "' for anime '" + hosterFile.getAnime().getName() + "'");
                        }
                    }

                    if (hosterFile.hostedEpisode != null) {
                        hosterFile.episode = Database.instance().getEpisodeByID(hosterFile.hostedEpisode.getEpisodeID());
                    }


                    files.add(hosterFile);
                }

            }

        } finally {
            lock.unlock();
        }
        return files;
    }

    public static List<HosterFile> getFiles() {
        lock.lock();

        logger.debug("getFiles()");

        List<HosterFile> files = new ArrayList<>();
        try {
            MegaSession sessionMega = Mega.init();

            final String basePath = "/evoke/animes";
            List<FileInfo> animes = sessionMega.ls(basePath).call();
            for (FileInfo animeFileInfo : animes) {
                if (!animeFileInfo.isDirectory()) {
                    continue;
                }

                final String animeFolder = basePath + "/" + animeFileInfo.getName();
                List<FileInfo> episodes = sessionMega.ls(animeFolder).call();
                for (FileInfo episode : episodes) {
                    HosterFile hosterFile = new HosterFile();
                    hosterFile.name = episode.getName();
                    hosterFile.anime = Database.instance().getAnimeByAnilistID(Long.parseLong(animeFileInfo.getName()));

                    MegaCmdExport export = sessionMega.export(animeFolder + "/" + episode.getName());
                    String publicLink = export.call().getPublicLink().replace("file", "embed");
                    hosterFile.embed = publicLink;

                    hosterFile.hostedEpisode = Database.instance().getHostedEpisodeForStreamURL(hosterFile.embed);

                    if (hosterFile.hostedEpisode == null) {
                        logger.warn("HostedEpisode missing for: '" + episode.getName() + "'");
                        String epNumStr = episode.getName().split("-")[1];
                        epNumStr = epNumStr.substring(0, epNumStr.indexOf('.'));
                        long epNum = Long.parseLong(epNumStr);

                        Optional<Episode> episodeOptional = hosterFile.getAnime().getEpisodes().stream().filter(episode1 -> episode1.getNumber() == epNum).findFirst();
                        if (episodeOptional.isPresent()) {
                            Episode episodeObj = episodeOptional.get();
                            Database.instance().createHostedEpisode(DistributionTarget.MEGA.getHoster(), episodeObj, publicLink);
                            hosterFile.hostedEpisode = Database.instance().getHostedEpisodeForStreamURL(hosterFile.embed);
                        } else {
                            logger.warn("Could not find episode '" + epNumStr + "' for anime '" + hosterFile.getAnime().getName() + "'");
                        }
                    }

                    if (hosterFile.hostedEpisode != null) {
                        hosterFile.episode = Database.instance().getEpisodeByID(hosterFile.hostedEpisode.getEpisodeID());
                    }


                    files.add(hosterFile);
                }
            }

        } finally {
            lock.unlock();
        }
        return files;
    }

    public static boolean checkHostedEpisode(HostedEpisode hostedEpisode) {

        final String basePath = "/evoke/animes";
        MegaSession mega = Mega.init();

        Episode episode = Database.instance().getEpisodeByID(hostedEpisode.getEpisodeID());
        Anime anime = episode.getAnime();
        String episodePath = basePath + "/" + anime.getAnilistID() + "/" + anime.getAnilistID() + "-" + episode.getNumber() + ".mp4";

        if (mega.exists(episodePath)) {
            String embedURL = getEmbedURL(episodePath);
            return embedURL.equals(hostedEpisode.getStreamURL());
        }

        return false;
    }
}
