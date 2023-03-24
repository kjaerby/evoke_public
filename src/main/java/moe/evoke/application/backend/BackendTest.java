package moe.evoke.application.backend;

import moe.evoke.application.backend.crawler.GoGoAnime;
import moe.evoke.application.backend.db.Anime;
import moe.evoke.application.backend.db.Database;
import moe.evoke.application.backend.db.Episode;
import moe.evoke.application.backend.hoster.ipfs.IPFS;
import moe.evoke.application.backend.hoster.ipfs.StatsRepoResponse;
import moe.evoke.application.backend.mirror.distribution.DistributionHelper;
import moe.evoke.application.backend.mirror.distribution.DistributionSource;
import moe.evoke.application.backend.torrent.TorrentManager;
import moe.evoke.application.backend.util.Utils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.UUID;

public class BackendTest {

    private static final Logger logger = LoggerFactory.getLogger(BackendTest.class);

    public static void main(String[] args) throws Exception {

        {
            Anime anime = Database.instance().getAnimeByAnilistID(129208);

            String downloadURL = GoGoAnime.getDownloadURL(anime, anime.getEpisodes().get(0));
            System.out.println(downloadURL);
//            GoGoAnime.downloadFileFromMP4Upload("fxv05oaxj04w", "a.mp4");

            System.exit(0);
        }

        {
            Anime anime = Database.instance().getAnimeByAnilistID(129277);

            logger.info("Torrent available: " + TorrentManager.isEpisodeAvailable(anime, anime.getEpisodes().get(0)));

            DistributionSource source = DistributionHelper.getBestSourceForAnime(anime, anime.getEpisodes().get(0));
            logger.info("DistributionSource: " + source);

            System.exit(0);
        }

        {
            moe.evoke.application.backend.hoster.ipfs.IPFS.getAllGateways().parallelStream().forEach(ipfsGateway -> {
                try {
                    while (ipfsGateway.getStatsRepoResponse() == null)
                    {
                        logger.debug("Waiting...");
                        Thread.sleep(60 * 1000);
                    }
                    StatsRepoResponse statsRepoResponse = ipfsGateway.getStatsRepoResponse();
                    logger.info(Long.toString(statsRepoResponse.getStorageMax() - statsRepoResponse.getRepoSize()));
                    logger.info(statsRepoResponse.toString());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            System.exit(0);
        }

        {
            File file = new File("/Volumes/data 5TB/download/[Mezashite] Aikatsu! 001-178 Batch/[Mezashite] Aikatsu! S2 Episodes 051-101/[Mezashite] Aikatsu! ‒ 101 [6936887B].mkv");
            File subtitle = Utils.extractSubtitle(file);
            File subtitle2 = new File(UUID.randomUUID() + ".ass");
            subtitle.renameTo(subtitle2);
            Utils.burninSubtitle(file, subtitle2);

            System.exit(0);
        }

        {
            String inputPath = "./";
            File inputDir = new File(inputPath);
            var files = inputDir.listFiles(pathname -> FilenameUtils.getExtension(pathname.getName()).equalsIgnoreCase("mkv"));
            for (File file : files) {
                logger.info(file.getAbsolutePath());
                File subtitle = Utils.extractSubtitle(file);
                Utils.burninSubtitle(file, subtitle);
            }

            System.exit(0);
        }

        {
            Anime anime = Database.instance().getAnimeByAnilistID(129386);
            Database.instance().createEpisode(anime, 6);
            anime = Database.instance().getAnimeByAnilistID(129386);

            Episode episode = anime.getEpisodes().stream().filter(ep -> ep.getNumber() == 6).findFirst().get();

            logger.debug(TorrentManager.getBestTorrentForAnime(anime, episode));

            System.exit(0);
        }
/*
        {
            long animeID = 257;

            MegaSession sessionMega = Mega.init();
            final String basePath = "/evoke/work";
            List<FileInfo> animes = sessionMega.ls(basePath).call();
            for (FileInfo anime : animes) {

                String name = anime.getName();
                logger.info(name);
                if (name.startsWith("[AOmundson] Ikkitousen - ") && name.endsWith(".mp4")) {

                    int episode = 0;
                    String episodeStr = name.replace("[AOmundson] Ikkitousen - ", "");
                    episodeStr = episodeStr.substring(0, episodeStr.indexOf("."));

                    try {
                        episode = Integer.parseInt(episodeStr);
                    } catch (Exception ex) {
                        logger.error(episodeStr);
                        episode = -1;
                    }

                    if (episode > -1) {
                        String newName = animeID + "-" + episode + ".mp4";
                        //logger.info(anime.getName() + " rename to: " + newName);

                        System.out.println("mega-mv \"" + basePath + "/" + anime.getName() + "\" \"" + basePath + "/" + newName + "\"");
                    }

                }
            }

            System.exit(0);
        }

*/


/*
        {
            MegaSession sessionMega = Mega.init();

            final String basePath = "/evoke/animes";
            List<FileInfo> animes = sessionMega.ls(basePath).call();
            for (FileInfo anime : animes) {

                try {
                    if (!anime.isDirectory()) {
                        continue;
                    }

                    final String animeFolder = basePath + "/" + anime.getName();
                    List<FileInfo> episodes = sessionMega.ls(animeFolder).call();
                    for (FileInfo episode : episodes) {
                        if (episode.getSize().isPresent()) {
                            long size = episode.getSize().get();
                            if (size < 1024) {
                                logger.info(episode.getName() + "=" + size);
                            }
                        }
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }


            System.exit(0);
        }
*/
        /*
        {
            Anime anime = Database.getAnimeByAnilistID(116589);
            logger.info("Searching for: " + anime.getName());

            for (Item item : RssClient.getTosho()) {
                String title = item.getTitle().get();

            }

            System.exit(0);
        }
*/
        /*
        {
            OfflineAnime offlineAnime = AnimeOfflineDatabase.loadOfflineAnimeData();
            Map<Anime, Map<AnimeIDSource, Long>> animeSourceMap = new HashMap<>();
            offlineAnime.getData().stream().forEach(dataItem -> {
                Anime anime = null;
                for (String source : dataItem.getSources()) {
                    if (source.contains("https://anilist.co/anime/"))
                    {
                        long anilistID = Long.parseLong(source.replace("https://anilist.co/anime/", ""));
                        anime = Database.getAnimeByAnilistID(anilistID);
                        if (anime != null && !animeSourceMap.containsKey(anime))
                        {
                            animeSourceMap.put(anime, new HashMap<>());
                        } else if (anime == null)
                        {
                            logger.error("Anime missing in DB: " + anilistID);
                            Database.createAnime(anilistID);
                            anime = Database.getAnimeByAnilistID(anilistID);
                            animeSourceMap.put(anime, new HashMap<>());
                        }
                    }
                }

                if (anime != null)
                {
                    Map<AnimeIDSource, Long> sourceMap = animeSourceMap.get(anime);
                    for (String source : dataItem.getSources()) {
                        if (source.contains("https://anilist.co/anime/"))
                        {
                            long sourceID = Long.parseLong(source.replace("https://anilist.co/anime/", ""));
                            sourceMap.put(AnimeIDSource.anilist, sourceID);
                        } else if (source.contains("https://anidb.net/anime/"))
                        {
                            long sourceID = Long.parseLong(source.replace("https://anidb.net/anime/", ""));
                            sourceMap.put(AnimeIDSource.anidb, sourceID);
                        } else if (source.contains("https://myanimelist.net/anime/"))
                        {
                            long sourceID = Long.parseLong(source.replace("https://myanimelist.net/anime/", ""));
                            sourceMap.put(AnimeIDSource.myanimelist, sourceID);
                        }
                    }
                }
            });

            List<Anime> animes = Database.getAnimes();

            animes.parallelStream().forEach(anime -> {
                long anilistID = anime.getAnilistID();
                long idFromDB = Database.getAniDBIDForAnilistID(anilistID);

                if (idFromDB == -1) {
                    Map<AnimeIDSource, Long> sourceMap = animeSourceMap.get(anime);
                    if (sourceMap != null && sourceMap.containsKey(AnimeIDSource.anidb)) {
                        long aniDBID = sourceMap.get(AnimeIDSource.anidb);
                        logger.info(anilistID + "=" + aniDBID);
                        if (aniDBID > -1) {
                            Database.createAnilistToAniDB(anilistID, aniDBID);
                        }
                    }
                }
            });
            animes.parallelStream().forEach(anime -> {
                long anilistID = anime.getAnilistID();
                long idFromDB = Database.getMALIDForAnilistID(anilistID);

                if (idFromDB == -1) {
                    Map<AnimeIDSource, Long> sourceMap = animeSourceMap.get(anime);
                    if (sourceMap != null && sourceMap.containsKey(AnimeIDSource.myanimelist)) {
                        long malID = sourceMap.get(AnimeIDSource.myanimelist);
                        logger.info(anilistID + "=" + malID);
                        if (malID > -1) {
                            Database.setMALIDForAnilistID(anilistID, malID);
                        }
                    }
                }
            });

            System.exit(0);
        }
*/

    }
}
