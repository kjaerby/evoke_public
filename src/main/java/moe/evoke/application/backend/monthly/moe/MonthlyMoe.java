package moe.evoke.application.backend.monthly.moe;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.gson.Gson;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import moe.evoke.application.backend.Config;
import moe.evoke.application.backend.db.Anime;
import moe.evoke.application.backend.db.Database;
import moe.evoke.application.backend.monthly.moe.calendar.EpisodesItem;
import moe.evoke.application.backend.monthly.moe.calendar.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MonthlyMoe {

    private static final Logger logger = LoggerFactory.getLogger(MonthlyMoe.class);

    private static final int TOKEN_KEY = 1337;
    private static final LoadingCache<Integer, List<AiringAnime>> airingAnime;

    static {
        airingAnime = Caffeine.newBuilder()
                .maximumSize(1)
                .expireAfterWrite(12, TimeUnit.HOURS)
                .refreshAfterWrite(6, TimeUnit.HOURS)
                .build(key -> collectAiringAnime());
    }

    public static List<AiringAnime> getAiringAnime() {
        return airingAnime.get(TOKEN_KEY);
    }

    private static List<AiringAnime> collectAiringAnime() {
        HttpResponse<String> response = Unirest.get("https://www.monthly.moe/api/v1/calendar?token=" + Config.getMonthlyMoeAPIKey())
                .asString();

        List<AiringAnime> animes = new ArrayList<>();
        if (response.isSuccess()) {
            Gson gson = new Gson();
            Response calendarResponse = gson.fromJson(response.getBody(), Response.class);

            final List<EpisodesItem> episodes = calendarResponse.getEpisodes().stream().filter(episodesItem -> isInRange(episodesItem)).collect(Collectors.toList());

            Set<Integer> animeIDs = new HashSet<>();
            for (EpisodesItem episodesItem : episodes) {
                animeIDs.add(episodesItem.getAnimeId());
            }
            for (Integer animeID : animeIDs) {
                long anilistID = getAnilistIDFromMonthlyAnimeID(animeID);
                if (anilistID > -1) {
                    Anime anime = Database.instance().getAnimeByAnilistID(anilistID);
                    if (anime == null && anilistID > 0) {
                        logger.warn("Anime " + anilistID + " not found in database... Creating it!");

                        Database.instance().createAnime(anilistID);
                        anime = Database.instance().getAnimeByAnilistID(anilistID);
                        anime.getData(); // for cache storage
                    }

                    animes.add(new AiringAnime(anime, animeID, episodes));
                }
            }

        } else {
            logger.error("Could not get airing animes! (" + response.getBody() + ")");
        }

        return animes;
    }

    private static long getAnilistIDFromMonthlyAnimeID(long monthlyID) {

        moe.evoke.application.backend.monthly.moe.animes.Response animeResponse = Database.instance().getMonhtlyMoeCache(monthlyID);

        if (animeResponse == null) {
            HttpResponse<String> response = Unirest.get("https://www.monthly.moe/api/v1/animes/" + monthlyID + "?token=" + Config.getMonthlyMoeAPIKey())
                    .asString();

            if (response.isSuccess()) {
                Gson gson = new Gson();
                animeResponse = gson.fromJson(response.getBody(), moe.evoke.application.backend.monthly.moe.animes.Response.class);

                Database.instance().createMonhtlyMoeCache(monthlyID, animeResponse);
            }
        }

        if (animeResponse != null && animeResponse.getAnilistIds() != null && animeResponse.getAnilistIds().size() > 0)
            return animeResponse.getAnilistIds().get(0);

        return -1;
    }

    private static boolean isInRange(EpisodesItem episodesItem) {

        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DAY_OF_YEAR, -6);
        Date startDay = calendar.getTime();

        calendar.add(Calendar.DAY_OF_YEAR, 12);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        Date endDay = calendar.getTime();

        Date episodeDate = null;
        if (episodesItem.getDatetime().contains("Z")) {
            DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_DATE_TIME;
            OffsetDateTime offsetDateTime = OffsetDateTime.parse(episodesItem.getDatetime(), timeFormatter);
            episodeDate = Date.from(Instant.from(offsetDateTime));
        } else {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                episodeDate = dateFormat.parse(episodesItem.getDatetime());
            } catch (ParseException e) {
                logger.error("Could not parse '" + episodesItem.getDatetime() + "' to date!");
            }
        }

        return episodeDate != null && episodeDate.before(endDay) && episodeDate.after(startDay);
    }
}
