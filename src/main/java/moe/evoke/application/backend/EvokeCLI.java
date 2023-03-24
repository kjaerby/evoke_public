package moe.evoke.application.backend;

import io.github.eliux.mega.Mega;
import io.github.eliux.mega.MegaSession;
import io.github.eliux.mega.cmd.FileInfo;
import moe.evoke.application.backend.anilist.Anilist;
import moe.evoke.application.backend.db.*;
import moe.evoke.application.backend.hoster.ipfs.IPFS;
import moe.evoke.application.backend.hoster.mega.MegaNZ;
import moe.evoke.application.backend.malsync.MALSync;
import moe.evoke.application.backend.malsync.MALSyncProvider;
import moe.evoke.application.backend.mirror.distribution.DistributionJob;
import moe.evoke.application.backend.mirror.distribution.DistributionManager;
import moe.evoke.application.backend.mirror.distribution.DistributionSource;
import moe.evoke.application.backend.mirror.distribution.DistributionTarget;
import moe.evoke.application.backend.util.Utils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EvokeCLI {

    private static final Logger logger = LoggerFactory.getLogger(EvokeCLI.class);

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            logger.debug("No execution type specified! Closing...");
            System.exit(-1);
        }

        switch (args[0]) {
            case "import":
                importEpisodes();
                break;
            case "printAnimes":
                printAnimes();
                break;
            case "printHoster":
                printHoster();
                break;
            case "printEpisodes":
                break;
            case "printHostedEpisodes":
                break;
            case "createMissingEpisodes":
                createMissingEpisodes();
                break;
            case "migrateTwist":
                migrateTwist();
                break;
            case "checkMEGA":
                checkMEGA();
                break;
            case "migrateIPFS":
                migrateIPFS(args);
                break;
            case "generateMassDL":
                generateMassDL();
                break;
            case "gogoImport":
                gogoImport();
                break;
            case "ipfsUpload":
                ipfsUpload(args);
                break;
        }

    }

    private static void ipfsUpload(String[] args) {
        String inputDir = args[1];
        try {
            ExecutorService executor = Executors.newFixedThreadPool(4);

            List<File> files = Files.walk(Paths.get(inputDir))
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .filter(file -> FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("mp4")).collect(Collectors.toList());

            Hoster ipfsHost = DistributionTarget.IPFS.getHoster();
            int counter = 0;
            for (File file : files) {
                String[] split = FilenameUtils.getBaseName(file.getName()).split("-");
                String animeStr = split[0];
                String episodeStr = split[1];

                try {
                    long animeID = Long.parseLong(animeStr);
                    long episodeNum = Long.parseLong(episodeStr);

                    Anime anime = Database.instance().getAnimeByAnilistID(animeID);
                    List<Episode> episodes = anime.getEpisodes();
                    Optional<Episode> episodeOpt = episodes.stream().filter(episode -> episode.getNumber() == episodeNum).findFirst();
                    if (episodeOpt.isPresent()) {
                        Optional<HostedEpisode> hepOpt = episodeOpt.get().getHostedEpisodes().stream().filter(hep -> hep.getHoster().equals(ipfsHost)).findFirst();
                        if (hepOpt.isEmpty()) {
                            counter++;
                            executor.submit(() -> {
                                logger.info("IPFS missing for '" + anime.getName() + "' episode " + episodeNum);
                                try {
                                    String cid = IPFS.uploadFile(file, true);
                                    if (cid != null && !cid.isEmpty()) {
                                        Database.instance().createHostedEpisode(ipfsHost, episodeOpt.get(), cid);
                                    } else {
                                        logger.error("cid is empty?!");
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                    }

                    if (counter == 100) {
                        break;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            executor.shutdown();
            executor.awaitTermination(24, TimeUnit.HOURS);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void gogoImport() {
        logger.info("Collecting missing episodes...");
        List<Episode> missingEpisodes = Database.instance().findMissingEpisodesForAnimes(10000);
        Collections.shuffle(missingEpisodes);

        logger.info("Got " + missingEpisodes.size() + " missing ones...");
        int count = 0;
        for (int i = 0; count < 500 && i < missingEpisodes.size(); i++) {
            Episode episode = missingEpisodes.get(i);
            List<DistributionTarget> targets = new ArrayList<>();
            targets.add(DistributionTarget.IPFS);
            targets.add(DistributionTarget.MEGA);
            targets.add(DistributionTarget.STREAMTAPE);
            Anime anime = episode.getAnime();

            if (DistributionSource.GOGO.isAvailable(anime)) {
                logger.info("Submitted job for '" + anime.getName() + "'  episode " + episode.getNumber());
                DistributionJob job = new DistributionJob();
                job.anime = anime;
                job.episode = episode;
                job.source = DistributionSource.GOGO;
                job.targets = targets;

                DistributionManager.submitJob(job);
                count++;
            }
        }
    }

    private static void generateMassDL() throws IOException {
        List<String> lines = Files.readAllLines(Path.of("input.csv"));

        FileWriter gogoWriter = new FileWriter("gogo.txt");
        FileWriter twistWriter = new FileWriter("twist.txt");
        lines.parallelStream().forEach(line -> {
            try {
                long id = Long.parseLong(line.split(",")[0]);
                Anime anime = Database.instance().getAnimeByAnilistID(id);
                if (DistributionSource.GOGO.isAvailable(anime)) {
                    logger.info("Available on GoGo: " + anime.getName());
                    List<MALSyncProvider> providers = MALSync.getProviderForAnime(anime);
                    Optional<MALSyncProvider> provider = providers.stream().filter(malSyncProvider -> malSyncProvider.provider.equalsIgnoreCase("gogoanime")).findFirst();
                    if (provider.isPresent()) {
                        logger.info(provider.get().url);
                        gogoWriter.write(anime.getAnilistID() + ";" + provider.get().url + System.lineSeparator());
                    }

                } else if (DistributionSource.TWIST.isAvailable(anime)) {
                    logger.info("Available on Twist: " + anime.getName());
                    List<MALSyncProvider> providers = MALSync.getProviderForAnime(anime);
                    Optional<MALSyncProvider> provider = providers.stream().filter(malSyncProvider -> malSyncProvider.provider.equalsIgnoreCase("twist")).findFirst();
                    if (provider.isPresent()) {
                        twistWriter.write(anime.getAnilistID() + ";" + provider.get().identifier + System.lineSeparator());
                    }

                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        gogoWriter.close();
        twistWriter.close();
    }

    public static <T> Collector<T, ?, Stream<T>> reverse()
    {
        return Collectors.collectingAndThen(Collectors.toList(), list -> {
            Collections.reverse(list);
            return list.stream();
        });
    }

    private static void migrateIPFS(String[] args) {

        logger.info("Migrating MEGA -> IPFS");
        logger.info("Searching for episodes which are not on IPFS...");
        Hoster ipfsHost = DistributionTarget.IPFS.getHoster();

        List<Episode> tmpMissingEp = new ArrayList<>();
        if (args != null && args.length == 1) {
            logger.info("Getting anime...");
            List<Anime> animeList = Database.instance().getAnime();
            logger.info("Found " + animeList.size() + " anime!");

            tmpMissingEp = animeList.parallelStream()
                    .map(Anime::getEpisodes)
                    .flatMap(Collection::stream)
                    .collect(reverse())
                    .filter(episode ->
                    {
                        var hostedEpisodes = episode.getHostedEpisodes();
                        if (hostedEpisodes.size() == 0) {
                            return false;
                        }

                        boolean found = false;
                        for (HostedEpisode hostedEpisode : hostedEpisodes) {
                            if (hostedEpisode.getHoster().equals(ipfsHost)) {
                                found = true;
                            }
                        }
                        return !found;
                    })
                    .collect(Collectors.toList());
        } else {
            for (int i = 1; i < args.length; i++) {
                String anilistID = args[i];
                logger.info("Getting anime " + anilistID + "...");
                long id = Long.parseLong(anilistID);
                Anime anime = Database.instance().getAnimeByAnilistID(id);
                logger.info("Found: " + anime.getName());
                for (Episode episode : anime.getEpisodes()) {
                    Optional<HostedEpisode> hostedEpisode = episode.getHostedEpisodes().stream().filter(he -> he.getHoster().equals(ipfsHost)).findFirst();
                    if (hostedEpisode.isEmpty()) {
                        tmpMissingEp.add(episode);
                    }
                }
            }
        }


        List<Episode> missingEpisodes = tmpMissingEp.stream().distinct().collect(Collectors.toList());

        logger.info("Found " + missingEpisodes.size() + " missing episodes in IPFS");

        final long startTime = System.currentTimeMillis();
        var countRef = new Object() {
            int count = 0;
        };
        new Thread(() -> {
            while (countRef.count != missingEpisodes.size()) {
                long currentTime = System.currentTimeMillis();
                long delta = currentTime - startTime;
                long timePerElement = delta / (countRef.count > 0 ? countRef.count : 1);
                long timeToGo = timePerElement * (missingEpisodes.size() - countRef.count);
                logger.info("[" + countRef.count + "/" + missingEpisodes.size() + "] ETA: " + Utils.milliToETA(timeToGo));

                try {
                    Thread.sleep(60 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        for (Episode missingEpisode : missingEpisodes) {
            Anime anime = missingEpisode.getAnime();
            String filename = missingEpisode.getAnime().getAnilistID() + "-" + missingEpisode.getNumber() + ".mp4";
            File downloadedFile = new File(filename);

            logger.info("Current file: " + filename);

            try {
                MegaNZ.downloadFile("evoke/animes/" + anime.getAnilistID() + "/" + filename, "./");
                if (downloadedFile.exists()) {
                    logger.info("Uploading to IPFS...");
                    String cid = IPFS.uploadFile(downloadedFile, true);
                    if (cid != null && !cid.isEmpty()) {
                        Database.instance().createHostedEpisode(ipfsHost, missingEpisode, cid);
                    } else {
                        logger.error("cid is empty?!");
                    }
                    logger.info("Finished IPFS upload!");
                } else {
                    logger.warn("No downloded episode file?!");
                }
            } catch (Exception ex) {
                logger.error("Exception wile migrating '" + missingEpisode.getAnime().getName() + "' episode " + missingEpisode.getNumber(), ex);
            } finally {
                downloadedFile.delete();
            }
            countRef.count++;
        }

    }

    private static void checkMEGA() {
        Hoster megaHoster = DistributionTarget.MEGA.getHoster();
        List<HostedEpisode> megaEpisodes = Database.instance().getAnime().stream()
                .map(Anime::getEpisodes)
                .flatMap(List::stream)
                .map(Episode::getHostedEpisodes)
                .flatMap(List::stream)
                .filter(hostedEpisode -> hostedEpisode.getHoster().getID() == megaHoster.getID())
                .collect(Collectors.toList());
        logger.info("Found " + megaEpisodes.size() + " episodes in DB on mega");

        for (HostedEpisode hostedEpisode : megaEpisodes) {
            logger.debug("Checking: " + hostedEpisode.getStreamURL());
            if (!MegaNZ.checkHostedEpisode(hostedEpisode)) {
                logger.error("Not found!");
                Database.instance().removeHostedEpisode(hostedEpisode);
            }
        }

        MegaNZ.getFiles();
    }

    private static void migrateTwist() {

        logger.info("Collecting Animes from Database");
        Map<String, Anime> twistToAnime = new HashMap<>();
        List<Anime> animes = Database.instance().getAnime();

        logger.info("Building slug map for animes...");
        animes.parallelStream().forEach(anime -> {
            Optional<MALSyncProvider> provider = MALSync.getProviderForAnime(anime).stream()
                    .filter(malSyncProvider -> malSyncProvider.getProvider().toLowerCase().contains("twist"))
                    .findFirst();
            if (provider.isPresent()) {
                String slug = provider.get().getUrl().replace("https://twist.moe/a/", "").replace("/1", "");
                twistToAnime.put(slug, anime);
            }
        });

        final String twistFolder = "evoke/twist.moe/Anime/";
        final String evokeFolder = "evoke/animes/";

        logger.info("Processing files from twist.moe MEGA-folder");
        MegaSession session = Mega.init();
        List<FileInfo> megaFiles = session.ls("evoke/twist.moe/Anime").call();

        for (FileInfo animeFolder : megaFiles) {
            if (animeFolder.isDirectory()) {
                Anime anime = twistToAnime.get(animeFolder.getName());
                if (anime == null) {
                    continue;
                }

                List<Episode> episodes = anime.getEpisodes();

                logger.info("Current folder: " + animeFolder.getName());
                String targetFolder = evokeFolder + anime.getAnilistID();

                try {
                    if (!session.exists(targetFolder)) {
                        logger.info("Folder '" + targetFolder + "' does not exist. Creating it!");
                        session.makeDirectory(targetFolder).run();
                    }

                    String folder = "evoke/twist.moe/Anime/" + animeFolder.getName();
                    List<FileInfo> episodeFiles = session.ls(folder.trim()).call();
                    for (FileInfo episodeFile : episodeFiles) {
                        String episodeName = episodeFile.getName().trim();
                        int dashIndex = episodeName.lastIndexOf("-");
                        if (dashIndex > -1 && episodeName.endsWith(".mp4")) {
                            String episodeNumStr = episodeName.substring(dashIndex + 1).replace(".mp4", "");
                            int episodeNum = Integer.parseInt(episodeNumStr);
                            String targetName = anime.getAnilistID() + "-" + episodeNum + ".mp4";

                            final String sourcePath = twistFolder + animeFolder.getName().trim() + "/" + episodeName;
                            String targetPath = targetFolder + "/" + targetName;
                            if (session.exists(targetPath)) {
                                Optional<FileInfo> existingFile = session.ls(targetPath).call().stream().findFirst();
                                if (existingFile.isPresent()) {
                                    Optional<Long> targetSize = existingFile.get().getSize();
                                    Optional<Long> sourceSize = episodeFile.getSize();

                                    if (targetSize.isPresent() && sourceSize.isPresent()) {
                                        if (targetSize.get() < sourceSize.get()) {
                                            session.remove(targetPath).run();
                                            logger.info("copy: " + sourcePath + " to " + targetPath);
                                            session.copy(sourcePath, targetPath).run();
                                        } else {
                                            logger.info(targetPath + " already present. Skipping!");
                                            logger.info("removing: " + sourcePath);
                                            session.remove(sourcePath).run();
                                        }
                                    }
                                }
                            } else {
                                logger.info("copy: " + sourcePath + " to " + targetPath);
                                session.copy(sourcePath, targetPath).run();
                                if (session.exists(targetPath)) {
                                    logger.info("removing: " + sourcePath);
                                    session.remove(sourcePath).run();
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        }
    }

    private static void createMissingEpisodes() {
        List<Anime> animeList = Database.instance().getAnime();
        animeList.parallelStream().forEach(anime -> {
            List<Episode> episodes = anime.getEpisodes();
            if (episodes.size() < anime.getEpisodeCount()) {
                for (int i = 1; i <= anime.getEpisodeCount(); i++) {
                    int finalI = i;
                    Optional<Episode> episode = episodes.stream().filter(ep -> ep.getNumber() == finalI).findFirst();
                    if (!episode.isPresent()) {
                        Database.instance().createEpisode(anime, i);
                    }
                }
            }
        });

    }

    private static void printAnimes() {
        for (Anime anime : Database.instance().getAnime()) {
            Anilist.getInfoForAnime(anime.getAnilistID());
        }

        for (Anime anime : Database.instance().getAnime()) {
            logger.debug(anime.getID() + "=" + anime.getAnilistID() + "|"
                    + Anilist.getInfoForAnime(anime.getAnilistID()).getData().getMedia().getTitle().getUserPreferred());
        }
    }

    private static void printHoster() {
        for (Hoster hoster : Database.instance().getHoster()) {
            logger.debug(hoster.getID() + "=" + hoster.getName());
        }
    }

    private static void importEpisodes() {
        try {
            List<String> hostedEpisodes = Files.readAllLines(Path.of("import.csv"));

            for (String hostedEpisode : hostedEpisodes) {
                String[] data = hostedEpisode.split(";");
                long anilistID = Long.parseLong(data[0]);
                long episodeNum = Long.parseLong(data[1]);
                long hosterID = Long.parseLong(data[2]);
                String streamURL = data[3];

                Anime anime = Database.instance().getAnimeByAnilistID(anilistID);
                if (anime == null) {
                    Database.instance().createAnime(anilistID);
                    anime = Database.instance().getAnimeByAnilistID(anilistID);
                }

                Hoster hoster = Database.instance().getHosterByID(hosterID);
                Episode episode = null;
                for (Episode ep : anime.getEpisodes()) {
                    if (ep.getNumber() == episodeNum) {
                        episode = ep;
                    }
                }
                if (episode == null) {
                    Database.instance().createEpisode(anime, episodeNum);
                    episode = anime.getEpisodes().stream().filter(episode1 -> episode1.getNumber() == episodeNum)
                            .findFirst().get();
                }

                Database.instance().createHostedEpisode(hoster, episode, streamURL);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
