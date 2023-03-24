package moe.evoke.application.backend.torrent;


import com.apptastic.rssreader.Item;
import com.apptastic.rssreader.RssReader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import moe.evoke.application.backend.db.Anime;
import moe.evoke.application.backend.db.Database;
import moe.evoke.application.backend.db.Episode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RssClient {

    private static final Logger logger = LoggerFactory.getLogger(RssClient.class);

    private static final String NYAA_RSS = "https://nyaa.si/?page=rss&c=1_2&f=0";
    private static final int TOKEN_KEY_NYAA = 420;

    private static final String TOSHO_RSS = "https://feed.animetosho.org/rss2";
    private static final int TOKEN_KEY_TOSHO = 69;

    private static final LoadingCache<Integer, List<Item>> nyaaCache;
    private static final LoadingCache<Integer, List<Item>> toshoCache;

    static {
        nyaaCache = Caffeine.newBuilder()
                .maximumSize(1)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .refreshAfterWrite(15, TimeUnit.MINUTES)
                .build(key -> getNyaa());

        toshoCache = Caffeine.newBuilder()
                .maximumSize(1)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .refreshAfterWrite(15, TimeUnit.MINUTES)
                .build(key -> getTosho());
    }


    private static List<Item> loadRss(final String url) {
        try {
            RssReader reader = new RssReader();
            Stream<Item> rssFeed = reader.read(url);
            List<Item> articles = rssFeed.collect(Collectors.toList());

            return articles;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return new ArrayList<>();
    }

    public static List<Item> getNyaa() {
        loadRss(NYAA_RSS).parallelStream().forEach(nyaa -> Database.instance().createNyaaRss(nyaa));

        return Database.instance().getNyaaRss();
    }

    public static List<Item> getTosho() {
        return loadRss(TOSHO_RSS);
    }

    public static List<Item> searchNyaa(Anime anime, Episode episode, boolean filter) {
        String animeURLName = null;
        try {
            animeURLName = anime.getName() + (episode == null ? "" : " " + episode.getNumber());
            animeURLName = "&q=" + URLEncoder.encode(animeURLName, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        List<Item> result = loadRss(NYAA_RSS + (animeURLName == null ? "" : animeURLName));

        return result.stream().filter(item -> {
            boolean isResult = true;
            isResult &= (!filter || titleSearch(item, anime));
            isResult &= item.getCategory().get().equals("Anime - English-translated");

            if (episode != null) {
                String epStr = episode.getNumber() < 10 ? "0" + episode.getNumber() : String.valueOf(episode.getNumber());
                if (isResult) {
                    boolean tmp = item.getTitle().get().contains(" - " + epStr);
                    if (!tmp) {
                        epStr = episode.getNumber() < 100 ? "00" + episode.getNumber() : String.valueOf(episode.getNumber());
                        tmp = item.getTitle().get().contains(" - " + epStr);
                    }

                    isResult &= tmp;
                }

                if (isResult) {
                    String title = item.getTitle().get();
                    int idx = title.indexOf(" - " + epStr) + 3 + epStr.length();
                    if (title.charAt(idx) <= '9' && title.charAt(idx) >= '0') {
                        isResult = false;
                    }
                }
            }

            return isResult;

        }).collect(Collectors.toList());
    }

    private static boolean titleSearch(Item item, Anime anime) {
        String animeName = anime.getName().toLowerCase();
        String title = item.getTitle().get().toLowerCase();

        boolean result = title.contains(animeName);
        if (!result) {
            animeName = animeName.replace(":", " - ").replace("  ", " ");
            animeName = animeName.replace("☆", "");
            result = title.contains(animeName);
        }

        return result;
    }


}
