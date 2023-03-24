package moe.evoke.application.backend.crawler;

import moe.evoke.application.backend.db.Anime;
import moe.evoke.application.backend.db.Episode;
import moe.evoke.application.backend.malsync.MALSync;
import moe.evoke.application.backend.malsync.MALSyncProvider;
import moe.evoke.application.backend.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class TwistMoe {

    private static final Logger logger = LoggerFactory.getLogger(TwistMoe.class);

    public static File downloadAnime(Anime anime, Episode episode) {
        Optional<MALSyncProvider> provider = MALSync.getProviderForAnime(anime).stream().filter(malSyncProvider -> malSyncProvider.getProvider().toLowerCase().contains("twist")).findFirst();

        File result = null;
        if (provider.isPresent()) {
            List<String> episodes = getEpisodeURLs(provider.get().getUrl());
            String episodeURL = episodes.get((int) (episode.getNumber() - 1));

            String filename = anime.getAnilistID() + "-" + episode.getNumber() + ".mp4";

            if (Utils.downloadFileWithCurl(episodeURL, filename)) {
                result = new File(filename);
            }
        }

        return result;
    }

    private static List<String> getEpisodeURLs(String twistMoeUrl) {
        String slug = twistMoeUrl.replace("https://twist.moe/a/", "");
        if (slug.indexOf("/") > -1) {
            slug = slug.substring(0, slug.indexOf("/"));
        }

        String pathToDownloader = System.getProperty("user.dir") + "/misc/twist.moe/";
        pathToDownloader = new File(pathToDownloader).getAbsolutePath();
        String pathToApiSh = new File(pathToDownloader + "/api.sh").getAbsolutePath();
        String pathToData = new File(pathToDownloader + "/.data.txt").getAbsolutePath();

        ProcessBuilder pb = new ProcessBuilder(pathToApiSh, slug);
        pb.directory(new File(pathToDownloader));
        try {

            Process process = pb.inheritIO().start();
            process.waitFor(10, TimeUnit.MINUTES);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }


        List<String> episodeURLs = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(Path.of(pathToData));

            for (String url : lines) {
                episodeURLs.add("https://cdn.twist.moe" + url);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return episodeURLs;
    }


}
