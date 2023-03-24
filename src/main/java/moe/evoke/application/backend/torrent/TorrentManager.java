package moe.evoke.application.backend.torrent;

import com.apptastic.rssreader.Item;
import moe.evoke.application.backend.db.Anime;
import moe.evoke.application.backend.db.Episode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class TorrentManager {

    private static final Logger logger = LoggerFactory.getLogger(TorrentManager.class);

    private static final Set<String> BLACKLIST;

    static {
        BLACKLIST = new HashSet<>();
        BLACKLIST.add("(s1)");
        BLACKLIST.add("season");
        BLACKLIST.add(".srt");
        BLACKLIST.add(".ass");
    }

    public static void checkUpdates(Anime anime) {


    }

    public static boolean isEpisodeAvailable(Anime anime, Episode episode) {
        return getBestTorrentForAnime(anime, episode) != null;
    }

    public static String getBestTorrentForAnime(Anime anime, Episode episode) {
        List<Item> items = RssClient.searchNyaa(anime, episode, true);
        items = items.stream().map(item -> {
            Item newItem = new Item();

            String title = item.getTitle().get();
            title = title.replaceAll("\\s*\\[[^\\]]*\\]\\s*", "");

            newItem.setTitle(title);
            newItem.setLink(item.getLink().get());
            newItem.setGuid(item.getGuid().get());
            newItem.setCategory(item.getCategory().get());

            return newItem;
        }).collect(Collectors.toList());

        Optional<Item> q1080P = items.stream().filter(item ->
                item.getTitle().get().toLowerCase().contains("1080p")
                        && !item.getTitle().get().toLowerCase().contains("multiple subtitle")
                        && checkBlacklist(item.getTitle().get())
        ).findFirst();

        if (q1080P.isEmpty()) {
            logger.warn("No Torrent found with 1080p and without multiple subtitle");
            q1080P = items.stream().filter(item ->
                    item.getTitle().get().toLowerCase().contains("1080p")
                            && checkBlacklist(item.getTitle().get())
            ).findFirst();

            if (q1080P.isEmpty()) {
                logger.warn("No Torrent found with 1080p");
                q1080P = items.stream().filter(item ->
                        checkBlacklist(item.getTitle().get())
                ).findFirst();

            }
        }

        if (q1080P.isPresent()) {
            logger.debug("Got torrent for " + anime.getName() + " Episode " + episode.getNumber());
            logger.debug(q1080P.get().getTitle().get());
            logger.debug(q1080P.get().getLink().get());
            return q1080P.get().getLink().get();
        }

        logger.error("No torrent found!");

        return null;
    }

    private static boolean checkBlacklist(String value) {
        for (String s : BLACKLIST) {
            if (value.toLowerCase().contains(s)) {
                return false;
            }
        }

        return true;
    }
}
