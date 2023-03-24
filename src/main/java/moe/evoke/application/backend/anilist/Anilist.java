package moe.evoke.application.backend.anilist;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.gson.Gson;
import moe.evoke.application.backend.anilist.airing.AiringSchedulesItem;
import moe.evoke.application.backend.anilist.airing.Response;
import moe.evoke.application.backend.anilist.data.AnimeData;
import moe.evoke.application.backend.anilist.malid.AnilistMal;
import moe.evoke.application.backend.db.Anime;
import moe.evoke.application.backend.db.Database;
import moe.evoke.application.backend.db.Episode;
import moe.evoke.application.backend.monthly.moe.AiringAnime;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Anilist {

    private static final Logger logger = LoggerFactory.getLogger(Anilist.class);
    private static final Map<Long, AnimeData> animeDataMap = new HashMap<>();

    private static final int TOKEN_KEY = 1337;
    private static final LoadingCache<Integer, List<AiringAnime.AiringEpisode>> airingAnime;

    static {
        airingAnime = Caffeine.newBuilder()
                .maximumSize(1)
                .expireAfterWrite(12, TimeUnit.HOURS)
                .refreshAfterWrite(6, TimeUnit.HOURS)
                .build(key -> collectAiringAnime());
    }

    public static AnimeData getInfoForAnime(long anilistID) {

        if (animeDataMap.containsKey(anilistID)) {
            return animeDataMap.get(anilistID);
        }

        AnimeData cache = Database.instance().getAnilistCache(anilistID);
        if (cache != null) {
            return cache;
        }


        try {
            String payload = "{\n" +
                    "    \"query\" : \"query media($id:Int,$type:MediaType,$isAdult:Boolean){Media(id:$id,type:$type,isAdult:$isAdult){id title{userPreferred romaji english native}coverImage{extraLarge large}bannerImage startDate{year month day}endDate{year month day}description season seasonYear type format status(version:2)episodes duration chapters volumes genres synonyms source(version:2)isAdult isLocked meanScore averageScore popularity favourites hashtag countryOfOrigin isLicensed isFavourite isRecommendationBlocked nextAiringEpisode{airingAt timeUntilAiring episode}relations{edges{id relationType(version:2)node{id title{userPreferred}format type status(version:2)bannerImage coverImage{large}}}}characterPreview:characters(perPage:6,sort:[ROLE,ID]){edges{id role voiceActors(language:JAPANESE){id name{full}language image{large}}node{id name{full}image{large}}}}staffPreview:staff(perPage:8){edges{id role node{id name{full}language image{large}}}}studios{edges{isMain node{id name}}}reviewPreview:reviews(perPage:2,sort:[RATING_DESC,ID]){pageInfo{total}nodes{id summary rating ratingAmount user{id name avatar{large}}}}recommendations(perPage:7,sort:[RATING_DESC,ID]){pageInfo{total}nodes{id rating userRating mediaRecommendation{id title{userPreferred}format type status(version:2)bannerImage coverImage{large}}user{id name avatar{large}}}}externalLinks{site url}streamingEpisodes{site title thumbnail url}trailer{id site}rankings{id rank type format year season allTime context}tags{id name description rank isMediaSpoiler isGeneralSpoiler}mediaListEntry{id status score}stats{statusDistribution{status amount}scoreDistribution{score amount}}}}\",\n" +
                    "    \"variables\" :{ \"id\": \"" + anilistID + "\", \"type\": \"ANIME\" }\n" +
                    "}";
            StringEntity entity = new StringEntity(payload,
                    ContentType.APPLICATION_JSON);

            logger.debug(payload);

            HttpClient httpClient = HttpClientBuilder.create()
                    .build();
            HttpPost request = new HttpPost("https://graphql.anilist.co");
            request.setEntity(entity);

            HttpResponse response = httpClient.execute(request);
            logger.debug(String.valueOf(response.getStatusLine().getStatusCode()));

            String result = EntityUtils.toString(response.getEntity(), "UTF-8");
            Gson gson = new Gson();

            AnimeData animeData = gson.fromJson(result, AnimeData.class);
            animeDataMap.put(anilistID, animeData);
            Database.instance().createAnilistCache(anilistID, animeData);

            return animeData;


        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static long getMalIDForAnime(Anime anime) {

        long malID = Database.instance().getMALIDForAnilistID(anime.getAnilistID());
        if (malID != -1) {
            return malID;
        }

        try {
            String payload = "{\n" +
                    "    \"query\" : \"query ($id: Int, $type: MediaType) { Media (id: $id, type: $type) { id idMal } }\"," +
                    "    \"variables\" :{ \"id\": \"" + anime.getAnilistID() + "\", \"type\": \"ANIME\" }\n" +
                    "}";

            StringEntity entity = new StringEntity(payload,
                    ContentType.APPLICATION_JSON);

            HttpClient httpClient = HttpClientBuilder.create()
                    .build();
            HttpPost request = new HttpPost("https://graphql.anilist.co");
            request.setEntity(entity);

            HttpResponse response = httpClient.execute(request);
            logger.debug(String.valueOf(response.getStatusLine().getStatusCode()));

            String result = EntityUtils.toString(response.getEntity(), "UTF-8");
            Gson gson = new Gson();

            AnilistMal anilistMal = gson.fromJson(result, AnilistMal.class);
            malID = anilistMal.getData().getMedia().getIdMal();
            Database.instance().setMALIDForAnilistID(anime.getAnilistID(), malID);

            return malID;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }


    public static void refreshCacheForAnime(Anime anime) {
        Calendar nowCal = GregorianCalendar.getInstance();
        nowCal.setFirstDayOfWeek(Calendar.MONDAY);
        nowCal.add(Calendar.DAY_OF_YEAR, -1);

        Date date = Database.instance().getAnilistCacheLastModified(anime.getAnilistID());

        Calendar cacheCal = GregorianCalendar.getInstance();
        cacheCal.setFirstDayOfWeek(Calendar.MONDAY);
        cacheCal.setTime(date);

        if (cacheCal.before(nowCal)) {
            Database.instance().removeAnilistCache(anime.getAnilistID());
            animeDataMap.remove(anime.getAnilistID());
            Anilist.getInfoForAnime(anime.getAnilistID());
        }
    }

    public static List<AiringAnime.AiringEpisode> getAiring() {
        return airingAnime.get(TOKEN_KEY);
    }

    private static List<AiringAnime.AiringEpisode> collectAiringAnime() {
        List<AiringAnime.AiringEpisode> result = new ArrayList<>();

        Calendar startCal = GregorianCalendar.getInstance();
        startCal.setFirstDayOfWeek(Calendar.MONDAY);
        startCal.add(Calendar.WEEK_OF_YEAR, -2);

        Calendar stopCal = GregorianCalendar.getInstance();
        stopCal.setFirstDayOfWeek(Calendar.MONDAY);
        stopCal.add(Calendar.WEEK_OF_YEAR, 1);

        try {
            long weekStart = startCal.getTimeInMillis() / 1000;
            long weekEnd = stopCal.getTimeInMillis() / 1000;

            final String queryString = "query ($weekStart: Int, $weekEnd: Int, $page: Int) { Page(page: $page) { pageInfo { hasNextPage total } airingSchedules(airingAt_greater: $weekStart, airingAt_lesser: $weekEnd) { id episode airingAt media { id idMal title { romaji native english } startDate { year month day } endDate { year month day } status season format genres synonyms duration popularity episodes source(version: 2) countryOfOrigin hashtag averageScore siteUrl description bannerImage isAdult coverImage { extraLarge color } trailer { id site thumbnail } externalLinks { site url } rankings { rank type season allTime } studios(isMain: true) { nodes { id name siteUrl } } relations { edges { relationType(version: 2) node { id title { romaji native english } siteUrl } } } } } } }";


            List<AiringSchedulesItem> airingSchedulesItems = new ArrayList<>();
            boolean hasNextPage = false;
            int currentPage = 1;
            do {

                String payload = "{\n" +
                        "    \"query\" : \"" + queryString + "\"," +
                        "    \"variables\" :{ \"weekStart\": " + weekStart + ", \"weekEnd\": " + weekEnd + ", \"page\": " + currentPage++ + " }\n" +
                        "}";

                System.out.println(payload);

                StringEntity entity = new StringEntity(payload,
                        ContentType.APPLICATION_JSON);

                HttpClient httpClient = HttpClientBuilder.create()
                        .build();
                HttpPost request = new HttpPost("https://graphql.anilist.co");
                request.setEntity(entity);

                HttpResponse response = httpClient.execute(request);
                logger.debug(String.valueOf(response.getStatusLine().getStatusCode()));

                String httpResult = EntityUtils.toString(response.getEntity(), "UTF-8");
                Gson gson = new Gson();

                Response airingResponse = gson.fromJson(httpResult, Response.class);
                airingSchedulesItems.addAll(airingResponse.getData().getPage().getAiringSchedules());

                hasNextPage = airingResponse.getData().getPage().getPageInfo().isHasNextPage();
            } while (hasNextPage);

            for (AiringSchedulesItem airingSchedule : airingSchedulesItems) {

                Anime anime = Database.instance().getAnimeByAnilistID(airingSchedule.getMedia().getId());
                if (anime == null)
                {
                    try {
                        Anilist.getInfoForAnime(airingSchedule.getMedia().getId());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        Database.instance().createAnime(airingSchedule.getMedia().getId());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    anime = Database.instance().getAnimeByAnilistID(airingSchedule.getMedia().getId());
                }

                Optional<Episode> episode = anime.getEpisodes().stream().filter(ep -> ep.getNumber() == airingSchedule.getEpisode()).findFirst();
                if (episode.isEmpty()) {
                    Database.instance().createEpisode(anime, airingSchedule.getEpisode());
                    episode = anime.getEpisodes().stream().filter(ep -> ep.getNumber() == airingSchedule.getEpisode()).findFirst();
                }

                if (episode.isPresent()) {
                    Date date = new Date();
                    date.setTime(airingSchedule.getAiringAt() * 1000L);
                    AiringAnime.AiringEpisode airingEpisode = new AiringAnime.AiringEpisode(episode.get(), date);
                    result.add(airingEpisode);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
